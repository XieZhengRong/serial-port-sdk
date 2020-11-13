package com.ikudot.mysdk;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;


import androidx.appcompat.app.AppCompatActivity;

import com.ikudot.mylibrary.Device;
import com.ikudot.mylibrary.SerialPortFinder;
import com.ikudot.mysdk.adapter.DeviceAdapter;

import java.io.DataOutputStream;
import java.util.ArrayList;

public class SelectSerialPortActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = SelectSerialPortActivity.class.getSimpleName();
    private DeviceAdapter mDeviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_serial_port);
        ListView listView = (ListView) findViewById(R.id.lv_devices);


        SerialPortFinder serialPortFinder = new SerialPortFinder();

        ArrayList<Device> devices = serialPortFinder.getDevices();

        if (listView != null) {
            listView.setEmptyView(findViewById(R.id.tv_empty));
            mDeviceAdapter = new DeviceAdapter(getApplicationContext(), devices);
            listView.setAdapter(mDeviceAdapter);
            listView.setOnItemClickListener(this);
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Device device = mDeviceAdapter.getItem(position);

        Intent intent = new Intent(this, SerialPortActivity.class);
        intent.putExtra(SerialPortActivity.DEVICE, device);
        startActivity(intent);
    }

    /**
     * Description: 这种方式会弹窗，如果用户点击拒绝授权那么判断依然是没有root
     */
    private static boolean checkRootExecutable() {

        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("exit\n");
            os.flush();
            int exitValue = process.waitFor();
            if (exitValue == 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.d("*** DEBUG ***", "Unexpected error - Here is what I know: " + e.getMessage());
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
