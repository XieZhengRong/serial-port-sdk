package com.ikudot.serialportlibrary;

import com.ikudot.serialportlibrary.comn.Device;
import com.ikudot.serialportlibrary.comn.SerialPortManager;
import com.ikudot.serialportlibrary.util.Cmd;
import com.ikudot.serialportlibrary.util.ToastUtil;
import com.licheedev.hwutils.ByteUtil;

public class SerialManager {
    private static SerialManager instance;

    private SerialManager() {
    }

    public static SerialManager getInstance() {
        if (instance == null) {
            synchronized (SerialManager.class) {
                if (instance == null) {
                    instance = new SerialManager();
                }
            }
        }
        return instance;
    }

    public boolean openSerialPort(String path, String bauterate) {
        Device mDevice = new Device("/dev/" + path, "115200");
        return SerialPortManager.instance().open(mDevice) != null;
    }

    /**
     * 连接
     */
    public void connect() {
        SerialPortManager.instance().sendCommand(Cmd.generateCmd(Cmd.CONNECT));
    }

    /**
     * 置零
     */
    public void zeroClear(){
        SerialPortManager.instance().sendCommand(Cmd.generateCmd(Cmd.RESET));
    }
    /**
     * 回皮
     */
    public void backSkin(){
        SerialPortManager.instance().sendCommand(Cmd.generateCmd(Cmd.BACK_SKIN));
    }
    /**
     * 回毛
     */
    public void backHair(){
        SerialPortManager.instance().sendCommand(Cmd.generateCmd(Cmd.BACK_HAIR));
    }
    /**
     * 去皮
     * @param weight 皮重,单位克（g）
     */
    public void clearTare(String weight){
        SerialPortManager.instance().sendCommand(Cmd.generateCmdWithWeight(Cmd.REMOVE_SKIN,weight));
    }
    public void setUnit(String unit) {
        SerialPortManager.instance().sendCommand(Cmd.generateCmdWithData(Cmd.SET_UNIT, unit));
    }

    public void close() {
        if (null != SerialPortManager.instance()) {
            SerialPortManager.instance().close();
        }
    }
    public void getWeight(){
        SerialPortManager.instance().sendCommand(Cmd.generateCmd(Cmd.GET_WEIGHT));
    }


}
