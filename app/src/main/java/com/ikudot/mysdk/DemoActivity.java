package com.ikudot.mysdk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ikudot.mylibrary.Device;
import com.ikudot.mylibrary.SerialPortManager;
import com.ikudot.mylibrary.listener.OnOpenSerialPortListener;
import com.ikudot.mylibrary.listener.OnSerialPortDataListener;
import com.ikudot.mylibrary.util.ByteUtil;
import com.ikudot.mylibrary.util.Cmd;
import com.ikudot.mysdk.databinding.DemoBinding;

import java.io.File;
import java.util.Arrays;

public class DemoActivity extends AppCompatActivity implements OnOpenSerialPortListener {
    private static final String TAG = "DemoActivity";
    DemoBinding binding;
    private SerialPortManager mSerialPortManager;

    public static void actionStart(Context context) {
        context.startActivity(new Intent(context, DemoActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DemoBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        mSerialPortManager = new SerialPortManager();
    }

    public void openSerialPort(View view) {
//        String name = binding.edit.getText().toString();
        String name = "ttyMT2";
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "串口名称不可为空", Toast.LENGTH_SHORT).show();
            return;
        }
        Device device = new Device(name, "MT", new File("/dev/" + name));
        if (!device.getFile().exists()) {
            Toast.makeText(this, "串口不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        // 打开串口
        boolean openSerialPort = mSerialPortManager.setOnOpenSerialPortListener(this)
                .setOnSerialPortDataListener(new OnSerialPortDataListener() {
                    @Override
                    public void onDataReceived(byte[] bytes) {
                        Log.i(TAG, "onDataReceived [ byte[] ]: " + ByteUtil.bytesToHexString(bytes));
                        runOnUiThread(() -> {
                            //判断是否为电子秤串口一帧数据
                            if (bytes[0] == Cmd.RECEIVE_START && bytes[bytes.length - 1] == Cmd.END) {
                                switch (bytes[2]) {
                                    case Cmd.CONNECT: {
                                        tips(bytes, "连接表头");
                                    }
                                    break;
                                    case Cmd.RESET: {
                                        tips(bytes, "置零");
                                    }
                                    break;
                                    case Cmd.REMOVE_SKIN: {
                                        tips(bytes, "去皮");
                                    }
                                    break;
                                    case Cmd.BACK_SKIN: {
                                        tips(bytes, "回皮");
                                    }
                                    break;
                                    case Cmd.BACK_HAIR: {
                                        tips(bytes, "回毛");
                                    }
                                    break;
                                    case Cmd.SET_UNIT: {
                                        tips(bytes, "设置单位");
                                    }
                                    break;

                                }
                            }
                        });
                    }

                    @Override
                    public void onDataSent(byte[] bytes) {
                        Log.i(TAG, "onDataSent [ byte[] ]: " + ByteUtil.bytesToHexString(bytes));
                        runOnUiThread(() -> {
                            Toast.makeText(DemoActivity.this, "发送成功：" + ByteUtil.bytesToHexString(bytes), Toast.LENGTH_SHORT).show();
                        });
                    }
                })
                .openSerialPort(device.getFile(), 115200);
    }

    private void tips(byte[] bytes, String tips) {
        //包头 帧长 指令 数据 检验 包尾
        if (bytes[3] == Cmd.SUCCESS) {
            Toast.makeText(DemoActivity.this, tips + "成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(DemoActivity.this, tips + "头失败", Toast.LENGTH_SHORT).show();
        }
    }

    public void closeSerialPort(View view) {
        mSerialPortManager.closeSerialPort();
        Toast.makeText(DemoActivity.this, "关闭串口成功", Toast.LENGTH_SHORT).show();
    }

    public void connectTable(View view) {
        mSerialPortManager.sendBytes(Cmd.generateCmd(Cmd.CONNECT));
    }

    public void reset(View view) {
        mSerialPortManager.sendBytes(Cmd.generateCmd(Cmd.RESET));
    }

    public void removeSkin(View view) {
        mSerialPortManager.sendBytes(Cmd.generateCmd(Cmd.REMOVE_SKIN));
    }

    public void backSkin(View view) {
        mSerialPortManager.sendBytes(Cmd.generateCmd(Cmd.BACK_SKIN));
    }

    public void backHair(View view) {
        mSerialPortManager.sendBytes(Cmd.generateCmd(Cmd.BACK_HAIR));
    }

    public void getWeight(View view) {
        mSerialPortManager.sendBytes(Cmd.generateCmd(Cmd.GET_WEIGHT));
    }

    @Override
    protected void onDestroy() {
        if (null != mSerialPortManager) {
            mSerialPortManager.closeSerialPort();
            mSerialPortManager = null;
        }
        super.onDestroy();
    }

    @Override
    public void onSuccess(File device) {
        Toast.makeText(getApplicationContext(), String.format("串口 [%s] 打开成功", device.getPath()), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFail(File device, Status status) {
        Toast.makeText(getApplicationContext(), String.format("串口 [%s] 打开失败", device.getPath()), Toast.LENGTH_SHORT).show();
    }

    public void setUnit(View view) {
        if (binding.kgRadio.isChecked()) {
            mSerialPortManager.sendBytes(Cmd.generateCmdWithData(Cmd.SET_UNIT,Cmd.SET_KG));
        }else if (binding.halfKgRadio.isChecked()){
            mSerialPortManager.sendBytes(Cmd.generateCmdWithData(Cmd.SET_UNIT,Cmd.SET_HALF_KG));
        }

    }

    public void checkRadio(View view) {
        if (view.getId() == R.id.kg_radio) {
            binding.kgRadio.setChecked(true);
            binding.halfKgRadio.setChecked(false);
        } else if (view.getId() == R.id.half_kg_radio){
            binding.kgRadio.setChecked(false);
            binding.halfKgRadio.setChecked(true);
        }
    }
}
