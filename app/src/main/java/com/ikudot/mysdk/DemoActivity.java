package com.ikudot.mysdk;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ikudot.mysdk.databinding.DemoBinding;
import com.ikudot.serialportlibrary.SerialManager;
import com.ikudot.serialportlibrary.comn.message.IMessage;
import com.ikudot.serialportlibrary.comn.message.RecvMessage;
import com.ikudot.serialportlibrary.util.Cmd;
import com.ikudot.serialportlibrary.util.StringUtils;
import com.ikudot.serialportlibrary.util.ToastUtil;
import com.licheedev.hwutils.ByteUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class DemoActivity extends AppCompatActivity {
    private static final String TAG = "DemoActivity";
    DemoBinding binding;
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
            SerialManager.getInstance().setUnit(Cmd.SET_KG);
        } else if (binding.halfKgRadio.isChecked()) {
            //市斤
            SerialManager.getInstance().setUnit(Cmd.SET_HALF_KG);
        }
    }

    public void checkRadio(View view) {
        if (view.getId() == R.id.kg_radio) {
            binding.kgRadio.setChecked(true);
            binding.halfKgRadio.setChecked(false);
        } else if (view.getId() == R.id.half_kg_radio) {
            binding.kgRadio.setChecked(false);
            binding.halfKgRadio.setChecked(true);
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
        ToastUtil.show(this, message.getMessage());
        if (message instanceof RecvMessage) {
            Log.d(TAG, "onMessageEvent: " + message.getMessage());
            //处理接收串口数据后逻辑
            String messageStr = message.getMessage().split("：")[1];
            if (messageStr.length() % 2 != 0) return;
            String[] strings = StringUtils.split(messageStr);
            if (strings[0].equals(Cmd.RECEIVE_START) && strings[strings.length - 1].equals(Cmd.END)) {
                //判断返回操作性类型
                switch (strings[2]) {
                    case Cmd.GET_WEIGHT: {
                        //万位
                        String w = ByteUtil.hexStr2decimal(strings[4]) + "";
                        //千位
                        String k = ByteUtil.hexStr2decimal(strings[5]) + "";
                        //百位
                        String h = ByteUtil.hexStr2decimal(strings[6]) + "";
                        //十位
                        String t = ByteUtil.hexStr2decimal(strings[7]) + "";
                        //个位
                        String l = ByteUtil.hexStr2decimal(strings[8]) + "";

                        String result = w + k + h + t + l;
                        //判断数值正负
                        if (strings[3].equals(Cmd.NEGATIVE)) {
                            result = "-" + result;
                        }
                        float result2 = Float.parseFloat(result)/1000;
                        binding.weight.setText(result2+"KG");
                    }
                    break;
                }
            }
        }
    }

    public void openSerialPort(View view) {
//        String path = binding.edit.getText().toString();
        String path = "ttyS3";

        mOpened = SerialManager.getInstance().openSerialPort(path, "115200");
        if (mOpened) {
            ToastUtil.showOne(this, "成功打开串口");
        } else {
            ToastUtil.showOne(this, "打开串口失败");
        }
        updateViewState(mOpened);
    }
}
