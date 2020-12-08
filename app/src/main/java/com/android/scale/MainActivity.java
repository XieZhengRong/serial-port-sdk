package com.android.scale;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.serialport.SerialPortFinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.android.scale.databinding.ActivityMainBinding;
import com.android.serialportlibrary.SerialManager;
import com.android.serialportlibrary.comn.Device;
import com.android.serialportlibrary.comn.message.IMessage;
import com.android.serialportlibrary.comn.message.RecvMessage;
import com.android.serialportlibrary.util.Cmd;
import com.android.serialportlibrary.util.StringUtils;
import com.android.serialportlibrary.util.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "MainActivity";
    String[] modes = new String[]{"连续发送", "稳定后连续发送", "稳定后单次发送", "归零后稳定单次发送", "按键发送"};
    ActivityMainBinding binding;
    private Device mDevice;
    private int mDeviceIndex;
    private String[] mDevices;
    // 连续发送（主动发送）*默认*
    // 稳定后连续发送（主动发送）
    // 稳定后单次发送（被动发送）
    // 归零后稳定单次发送（被动发送）
    // 按键发送（被动发送，一键上传）
    private final MutableLiveData<Boolean> isOpen = new MutableLiveData<>();
    SharedPreferences sharedPreferences;

    private boolean canOption = true;
    private Timer timer;
    private TimerTask timerTask;
    private boolean isSwitch = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        sharedPreferences = getSharedPreferences("message", MODE_PRIVATE);
        initDevice();
        initSpinners();

        isOpen.observe(this, b -> {
            if (b) {
                binding.halfKgRadio.setEnabled(true);
                binding.kgRadio.setEnabled(true);
                binding.skin.setEnabled(true);
                binding.clearZero.setEnabled(true);
                binding.switchWeight.setEnabled(true);
                binding.modeSpinner.setEnabled(true);
                binding.weightResult.setAlpha(1);
                binding.optionLayout.setAlpha(1);
            } else {
                binding.halfKgRadio.setEnabled(false);
                binding.kgRadio.setEnabled(false);
                binding.skin.setEnabled(false);
                binding.clearZero.setEnabled(false);
                binding.getWeight.setEnabled(false);
                binding.getWeight.setAlpha(0.3f);
                binding.switchWeight.setEnabled(false);
                binding.modeSpinner.setEnabled(false);
                binding.weightResult.setAlpha(0.5f);
                binding.optionLayout.setAlpha(0.5f);
            }
        });
        isOpen.setValue(false);
        initEvent();
    }

    private void initEvent() {
        binding.halfKgRadio.setOnCheckedChangeListener(this);
        binding.kgRadio.setOnCheckedChangeListener(this);
        binding.clearZero.setOnClickListener(v -> SerialManager.getInstance().zeroClear());
        binding.getWeight.setOnClickListener(v -> SerialManager.getInstance().getWeight());
        binding.switchWeight.setOnClickListener(v -> {
            String str = binding.switchWeight.getText().toString();
            if (str.equals("净重")) {
                binding.switchWeight.setText("毛重");
                isSwitch = true;
                SerialManager.getInstance().getWeight();
            } else if (str.equals("毛重")) {
                binding.switchWeight.setText("净重");
                SerialManager.getInstance().backHair();
            }
        });
        binding.skin.setOnClickListener(v -> {
            if (binding.skin.getText().toString().equals("去皮")) {
                SerialManager.getInstance().clearTare();
            } else if (binding.skin.getText().toString().equals("回皮")) {
                SerialManager.getInstance().backSkin();
            }
        });

    }

    /**
     * 初始化设备列表
     */
    private void initDevice() {
        SerialPortFinder serialPortFinder = new SerialPortFinder();
        // 设备
        mDevices = serialPortFinder.getAllDevicesPath();
        if (mDevices.length == 0) {
            mDevices = new String[]{
                    getString(R.string.no_serial_device)
            };
            return;
        }
        //筛选具有读写权限的串口
        List<String> deviceList = new ArrayList<>();
        for (String device : mDevices) {
            File file = new File(device);
            boolean canRead = file.canRead();
            boolean canWrite = file.canWrite();
            boolean canExecute = file.canExecute();
            String path = file.getAbsolutePath();
            if (canRead && canWrite) {
                deviceList.add(device);
            }
        }
        deviceList.add(0, "请选择串口设备");
        mDevices = new String[deviceList.size()];
        for (int i = 0; i < mDevices.length; i++) {
            mDevices[i] = deviceList.get(i);
        }
        mDevice = new Device();
        //固定波特率
        mDevice.setBaudrate("115200");
    }


    /**
     * 初始化下拉选项
     */
    private void initSpinners() {

        ArrayAdapter<String> deviceAdapter =
                new ArrayAdapter<String>(this, R.layout.spinner_default_item, mDevices);
        deviceAdapter.setDropDownViewResource(R.layout.spinner_item);
        binding.devicesSpinner.setAdapter(deviceAdapter);
        binding.devicesSpinner.setOnItemSelectedListener(this);
        binding.devicesSpinner.setSelection(mDeviceIndex);

        initModeSpinner();

    }

    private void initModeSpinner() {


        ArrayAdapter<String> modeAdapter =
                new ArrayAdapter<String>(this, R.layout.spinner_default_item, modes);
        modeAdapter.setDropDownViewResource(R.layout.spinner_item);
        binding.modeSpinner.setAdapter(modeAdapter);
        binding.modeSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Spinner 选择监听
        if (parent.getId() == R.id.devicesSpinner) {
            if (position != 0) {
                if (position != mDeviceIndex) {
                    if (SerialManager.getInstance().isOpenPort()) {
                        //若打开了串口，则先关闭
                        SerialManager.getInstance().close();
                        //重置发送模式
                        initModeSpinner();
                        //重置选中kg
                        binding.kgRadio.setChecked(true);
                        binding.halfKgRadio.setChecked(false);
                        //重置重量显示
                        binding.weightResult.setText("0.00kg");
                        //显示去皮按钮
                        binding.skin.setText("去皮");
                        //重置皮重
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove("skinWeight");
                        editor.apply();
                        //显示净重按钮
                        binding.switchWeight.setText("净重");
                        //清空操作日志
                        binding.log.setText("");
                    }

                    mDevice.setPath(mDevices[position]);
                    //打开串口
                    boolean open = SerialManager.getInstance().openSerialPort(mDevice.getPath(), mDevice.getBaudrate());
                    if (open) {
                        ToastUtil.show(this, "打开串口成功");
                        binding.log.setText(String.format("%s\n%s", "打开串口成功", binding.log.getText().toString()));
                        //连接表头
                        SerialManager.getInstance().connect();
                    } else {
                        ToastUtil.show(this, "打开串口失败");
                        binding.log.setText(String.format("%s\n%s", "打开串口失败", binding.log.getText().toString()));
                    }
                }
            } else {
                if (SerialManager.getInstance().isOpenPort()) {
                    SerialManager.getInstance().close();
                    ToastUtil.show(this, "关闭串口成功");
                    binding.log.setText(String.format("%s\n%s", "关闭串口成功", binding.log.getText().toString()));
                } else {
                    ToastUtil.show(this, "未打开串口");
                }
                isOpen.setValue(false);
            }
            mDeviceIndex = position;
        } else if (parent.getId() == R.id.mode_spinner) {
            if (SerialManager.getInstance().isOpenPort()) {
                String mode = modes[position];
                switch (mode) {
                    case "连续发送": {
                        SerialManager.getInstance().setMode(Cmd.MODE_CONTINUE_TRIGGER);
                    }
                    break;
                    case "稳定后连续发送": {
                        SerialManager.getInstance().setMode(Cmd.MODE_STABLE_CONTINUE_TRIGGER);
                    }
                    break;
                    case "稳定后单次发送": {
                        SerialManager.getInstance().setMode(Cmd.MODE_STABLE_SINGLE_TRIGGER);
                    }
                    break;
                    case "归零后稳定单次发送": {
                        SerialManager.getInstance().setMode(Cmd.MODE_ZERO_STABLE_SINGLE_TRIGGER);
                    }
                    break;
                    case "按键发送": {
                        SerialManager.getInstance().setMode(Cmd.MODE_SINGLE_TRIGGER);
                    }
                    break;
                }
                binding.getWeight.setEnabled(mode.equals("稳定后单次发送") || mode.equals("归零后稳定单次发送") || mode.equals("按键发送"));
                if (binding.getWeight.isEnabled()) {
                    binding.getWeight.setAlpha(1);
                } else {
                    binding.getWeight.setAlpha(0.3f);
                }
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == binding.halfKgRadio.getId()) {
            if (isChecked) {
                binding.kgRadio.setChecked(false);
                //市斤
                SerialManager.getInstance().setUnit(this, Cmd.SET_HALF_KG);
            }
        } else if (buttonView.getId() == binding.kgRadio.getId()) {
            if (isChecked) {
                binding.halfKgRadio.setChecked(false);
                //公斤
                SerialManager.getInstance().setUnit(this, Cmd.SET_KG);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(IMessage message) {
        // 收发消息，刷新界面
//        if (message instanceof SendMessage) {
//            binding.log.setText(binding.log.getText().toString()+"\n"+message.getMessage());
//            ToastUtil.show(this, message.getMessage());

//        binding.log.setText(String.format("%s\n%s", message.getMessage(), binding.log.getText().toString()));
//        }
        if (message instanceof RecvMessage) {
            Log.d(TAG, "onMessageEvent: " + message.getMessage());
            //处理接收串口数据后逻辑
            String messageStr = message.getMessage().split("：")[1];
            //偶数数据不符合、不解析
            if (messageStr.length() % 2 != 0) return;
            //每两个字符分割成功一个命令数组
            String[] strings = StringUtils.split(messageStr);
            //按照协议检验开头和结尾
            if (strings[0].equals(Cmd.RECEIVE_START) && strings[strings.length - 1].equals(Cmd.END)) {
                //记录操作日志
                String isSuccessStr = "";
                //判断返回操作性类型
                switch (strings[2]) {
                    //连接表头
                    case Cmd.CONNECT: {
                        if (strings[3].equalsIgnoreCase(Cmd.SUCCESS)) {
                            ToastUtil.showOne(this, "连接表头成功");
                            isSuccessStr = "连接表头成功";
                            isOpen.setValue(true);
                        } else {
                            isOpen.setValue(false);
                            ToastUtil.showOne(this, "连接表头失败");
                            isSuccessStr = "连接表头失败";
                        }
                    }
                    break;
                    //去皮
                    case Cmd.REMOVE_SKIN: {
                        if (strings[3].equalsIgnoreCase(Cmd.SUCCESS)) {
                            ToastUtil.showOne(this, "去皮成功");
                            isSuccessStr = "去皮成功";
                            binding.skin.setText("回皮");
                        } else {
                            ToastUtil.showOne(this, "去皮失败");
                            isSuccessStr = "去皮失败";
                        }
                    }
                    break;
                    //回皮（获取皮重量）
                    case Cmd.BACK_SKIN: {
                        if (!strings[1].equalsIgnoreCase("0a")) {
                            ToastUtil.show(this, "回皮失败");
                            isSuccessStr = "回皮失败";
                            break;
                        }
                        isSuccessStr = "回皮成功（显示皮重的十秒内不可操作）";
                        binding.skin.setText("去皮");

                        float result = SerialManager.getInstance().parseWeight(this, strings);

                        String unit = SerialManager.getInstance().getUnit(this);
                        //记录皮重
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        if (unit.equalsIgnoreCase("kg")) {
                            unit = "kg";
                            editor.putFloat("skinWeight", (result * 1000));
                        } else if (unit.equalsIgnoreCase("half_kg")) {
                            unit = "斤";
                            editor.putFloat("skinWeight", (result * 500));
                        }
                        editor.apply();
                        binding.weightResult.setText(String.format("%s%s", result, unit));

                        timer = null;
                        timerTask = null;
                        timer = new Timer();
                        timerTask = new TimerTask() {
                            @Override
                            public void run() {
                                SerialManager.getInstance().getWeight();
                                canOption = true;
                                runOnUiThread(() -> binding.log.setText(String.format("%s\n%s", "皮重显示完成，将恢复称重显示状态", binding.log.getText().toString())));
                            }
                        };
                        canOption = false;
                        timer.schedule(timerTask, 10 * 1000);

                    }
                    break;
                    //回毛（获取皮+净重）
                    case Cmd.BACK_HAIR: {
                        if (!strings[1].equalsIgnoreCase("0a")) {
                            ToastUtil.show(this, "回毛失败");
                            isSuccessStr = "回毛失败";
                            break;
                        }
                        isSuccessStr = "回毛成功";
                        float result = SerialManager.getInstance().parseWeight(this, strings);
                        String unit = SerialManager.getInstance().getUnit(this);
                        unit = getUnit(unit);
                        binding.weightResult.setText(String.format("%s%s", result, unit));
                    }
                    break;
                    //获取净重量
                    case Cmd.GET_WEIGHT: {
                        if (!strings[1].equalsIgnoreCase("0a")) {
                            isSuccessStr = "获取重量失败";
                            ToastUtil.show(this, "获取重量失败");
                            break;
                        }
                        isSuccessStr = "获取重量成功";
                        float result = SerialManager.getInstance().parseWeight(this, strings);
                        String unit = SerialManager.getInstance().getUnit(this);
                        unit = getUnit(unit);
                        //检查是否需要显示毛重
                        if (binding.switchWeight.getText().equals("毛重") && !isSwitch) {
                            //获取皮重(g)
                            float skinWeight = sharedPreferences.getFloat("skinWeight", 0f);
                            if (unit.equalsIgnoreCase("kg")) {
                                result = result + skinWeight / 1000;
                            } else if (unit.equals("斤")) {
                                result = result + skinWeight / 500;
                            }
                        }
                        isSwitch = false;
                        binding.weightResult.setText(String.format("%s%s", result, unit));
                    }
                    break;
                    //置零
                    case Cmd.RESET: {
                        if (strings[3].equalsIgnoreCase(Cmd.SUCCESS)) {
                            ToastUtil.showOne(this, "置零成功");
                            binding.weightResult.setText("0.00kg");
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.remove("skinWeight");
                            editor.apply();
                            isSuccessStr = "置零成功";
                        } else {
                            isSuccessStr = "置零失败";
                            ToastUtil.showOne(this, "置零失败");
                        }
                    }
                    break;
                    //设置单位
                    case Cmd.SET_UNIT: {
                        if (strings[3].equalsIgnoreCase(Cmd.SUCCESS)) {
                            isSuccessStr = "单位设置成功";
                            ToastUtil.showOne(this, "单位设置成功");
                            SerialManager.getInstance().getWeight();
                        } else {
                            isSuccessStr = "单位设置失败";
                            ToastUtil.showOne(this, "单位设置失败");
                        }
                    }
                    break;
                    //设置模式
                    case Cmd.SET_MODE: {
                        if (strings[3].equalsIgnoreCase(Cmd.SUCCESS)) {
                            isSuccessStr = "发送模式设置成功";
                            ToastUtil.showOne(this, "发送模式设置成功");
                            SerialManager.getInstance().getWeight();
                        } else {
                            isSuccessStr = "发送模式设置失败";
                            ToastUtil.showOne(this, "发送模式设置失败");
                        }
                    }
                    break;
                }
                binding.log.setText(String.format("%s\n%s", isSuccessStr, binding.log.getText().toString()));

            }
        }
    }

    private String getUnit(String unit) {
        if (unit.equalsIgnoreCase("kg")) {
            unit = "kg";
        } else if (unit.equalsIgnoreCase("half_kg")) {
            unit = "斤";
        }
        return unit;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (canOption) {
            return super.dispatchTouchEvent(ev);
        } else {
            binding.log.setText(String.format("%s\n%s", binding.log.getText().toString(), "皮重显示的10秒内不可操作，请稍后"));
            return true;
        }

    }
}
