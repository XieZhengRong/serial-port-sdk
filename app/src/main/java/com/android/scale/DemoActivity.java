package com.android.scale;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.scale.databinding.DemoBinding;
import com.android.serialportlibrary.SerialManager;
import com.android.serialportlibrary.comn.message.IMessage;
import com.android.serialportlibrary.comn.message.RecvMessage;
import com.android.serialportlibrary.comn.message.SendMessage;
import com.android.serialportlibrary.util.Cmd;
import com.android.serialportlibrary.util.StringUtils;
import com.android.serialportlibrary.util.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class DemoActivity extends AppCompatActivity {
    private static final String TAG = "DemoActivity";
    private DemoBinding binding;
    private boolean mOpened = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DemoBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
    }

    private void updateViewState(boolean mOpened) {
        if (mOpened) {
            binding.portStatus.setText("串口状态：打开");
        } else {
            binding.portStatus.setText("串口状态：关闭");
        }
    }
    public void closeSerialPort(View view) {
        SerialManager.getInstance().close();
        mOpened = false;
        updateViewState(mOpened);
        Toast.makeText(DemoActivity.this, "关闭串口成功", Toast.LENGTH_SHORT).show();
    }

    public void connectTable(View view) {
        SerialManager.getInstance().connect();
    }

    public void reset(View view) {
        SerialManager.getInstance().zeroClear();
    }

    public void removeSkin(View view) {

        SerialManager.getInstance().clearTare();

    }

    public void backSkin(View view) {


        SerialManager.getInstance().backSkin();


    }

    public void backHair(View view) {

        SerialManager.getInstance().backHair();

    }

    public void getWeight(View view) {


        SerialManager.getInstance().getWeight();


    }

    @Override
    protected void onDestroy() {
        SerialManager.getInstance().close();
        super.onDestroy();
    }

    /**
     * 设置单位
     *
     * @param view
     */
    public void setUnit(View view) {
        if (binding.kgRadio.isChecked()) {
            //公斤
            SerialManager.getInstance().setUnit(this, Cmd.SET_KG);
        } else if (binding.halfKgRadio.isChecked()) {
            //市斤
            SerialManager.getInstance().setUnit(this, Cmd.SET_HALF_KG);
        } else if (binding.gRadio.isChecked()) {
            //克
            SerialManager.getInstance().setUnit(this, Cmd.SET_G);
        }
    }

    public void checkRadio(View view) {
        if (view.getId() == R.id.kg_radio) {
            binding.kgRadio.setChecked(true);
            binding.halfKgRadio.setChecked(false);
            binding.gRadio.setChecked(false);
        } else if (view.getId() == R.id.half_kg_radio) {
            binding.kgRadio.setChecked(false);
            binding.halfKgRadio.setChecked(true);
            binding.gRadio.setChecked(false);
        } else if (view.getId() == R.id.g_radio) {
            binding.kgRadio.setChecked(false);
            binding.halfKgRadio.setChecked(false);
            binding.gRadio.setChecked(true);
        }
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(IMessage message) {
        // 收到时间，刷新界面
        if (message instanceof SendMessage) {
            ToastUtil.show(this, message.getMessage());
        }
        if (message instanceof RecvMessage) {
            Log.d(TAG, "onMessageEvent: " + message.getMessage());
            //处理接收串口数据后逻辑
            String messageStr = message.getMessage().split("：")[1];

            if (messageStr.length() % 2 != 0) return;
            String[] strings = StringUtils.split(messageStr);
            if (strings[0].equals(Cmd.RECEIVE_START) && strings[strings.length - 1].equals(Cmd.END)) {
                String isSuccessStr = "";
                //判断返回操作性类型
                switch (strings[2]) {
                    //连接表头
                    case Cmd.CONNECT: {
                        if (strings[3].equalsIgnoreCase(Cmd.SUCCESS)) {
                            ToastUtil.showOne(this, "连接表头成功");
                            isSuccessStr = "连接表头成功";
                        } else {
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
                            return;
                        }
                        isSuccessStr = "回皮成功";
                        float result = SerialManager.getInstance().parseWeight(this, strings);
                        binding.skinWeight.setText(String.format("%s%s%s", "皮重：", result, SerialManager.getInstance().getUnit(this)));
                    }
                    break;
                    //回毛（获取皮+净重）
                    case Cmd.BACK_HAIR: {
                        if (!strings[1].equalsIgnoreCase("0a")) {
                            ToastUtil.show(this, "回毛失败");
                            isSuccessStr = "回皮失败";
                            return;
                        }
                        isSuccessStr = "回皮成功";
                        float result = SerialManager.getInstance().parseWeight(this, strings);
                        binding.hairWeight.setText(String.format("%s%s%s", "毛重：", result, SerialManager.getInstance().getUnit(this)));
                    }
                    break;
                    //获取净重量
                    case Cmd.GET_WEIGHT: {
                        if (!strings[1].equalsIgnoreCase("0a")) {
                            isSuccessStr = "获取重量失败";
                            ToastUtil.show(this, "获取重量失败");
                            return;
                        }
                        isSuccessStr = "获取重量成功";
                        float result = SerialManager.getInstance().parseWeight(this, strings);
                        binding.weight.setText(String.format("%s%s%s", "净重：", result, SerialManager.getInstance().getUnit(this)));
                    }
                    break;
                    //置零
                    case Cmd.RESET: {
                        if (strings[3].equalsIgnoreCase(Cmd.SUCCESS)) {
                            ToastUtil.showOne(this, "置零成功");
                            binding.weight.setText("0.00");
                            binding.skinWeight.setText("0.00");
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
                        } else {
                            isSuccessStr = "单位设置失败";
                            ToastUtil.showOne(this, "单位设置失败");
                        }
                    }
                    break;
                }
                binding.receiptOrder.setText(String.format("%s\n%s(%s)", binding.receiptOrder.getText().toString(), messageStr,isSuccessStr));

            }
        }
    }

    public void openSerialPort(View view) {
        String path = binding.edit.getText().toString();
//        String path = "ttyS3";
//        String path = "ttyMT0";

        mOpened = SerialManager.getInstance().openSerialPort(path, "115200");
        if (mOpened) {
            ToastUtil.showOne(this, "成功打开串口");
        } else {
            ToastUtil.showOne(this, "打开串口失败");
        }
        updateViewState(mOpened);
    }
}
