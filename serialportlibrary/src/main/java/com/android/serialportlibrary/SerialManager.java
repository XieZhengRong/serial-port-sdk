package com.android.serialportlibrary;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.serialportlibrary.comn.Device;
import com.android.serialportlibrary.comn.SerialPortManager;
import com.android.serialportlibrary.util.Cmd;
import com.licheedev.hwutils.ByteUtil;

public class SerialManager {
    private static SerialManager instance;
    private final static String MESSAGE = "MESSAGE";
    private final static String UNIT = "UNIT";
    private boolean isOpen = false;//是否打开了串口

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

    /**
     * 是否打开了串口
     * @return
     */
    public boolean isOpenPort(){
        return isOpen;
    }

    /**
     * @param name      串口名称
     * @param bauterate 波特率
     * @return 是否成功打开串口
     */
    public boolean openSerialPort(String name, String bauterate) {
        Device mDevice = new Device( name, bauterate);
        isOpen = SerialPortManager.instance().open(mDevice) != null;
        return isOpen;
    }

    /**
     * 连接表头
     */
    public void connect() {
        SerialPortManager.instance().sendCommand(Cmd.generateCmd(Cmd.CONNECT));
    }

    /**
     * 置零
     */
    public void zeroClear() {
        SerialPortManager.instance().sendCommand(Cmd.generateCmd(Cmd.RESET));
    }

    /**
     * 回皮
     */
    public void backSkin() {
        SerialPortManager.instance().sendCommand(Cmd.generateCmd(Cmd.BACK_SKIN));
    }

    /**
     * 回毛
     */
    public void backHair() {
        SerialPortManager.instance().sendCommand(Cmd.generateCmd(Cmd.BACK_HAIR));
    }

    /**
     * 去皮
     */
    public void clearTare() {


        SerialPortManager.instance().sendCommand(Cmd.generateCmd(Cmd.REMOVE_SKIN));


    }

    /**
     * 设置单位
     *
     * @param context 上下文
     * @param unit    单位 Cmd.SET_KG(千克)s Cmd.SET_HALF_KG（市斤） Cmd.SET_G（克）
     */
    public void setUnit(Context context, String unit) {
        if (isOpen) {
            SharedPreferences.Editor editor = context.getSharedPreferences(MESSAGE, Context.MODE_PRIVATE).edit();
            //记住当前单位
            switch (unit) {
                case Cmd.SET_KG: {
                    editor.putString(UNIT, "KG");
                }
                break;
                case Cmd.SET_HALF_KG: {
                    editor.putString(UNIT, "HALF_KG");
                }
                break;
                case Cmd.SET_G: {
                    editor.putString(UNIT, "G");
                }
                break;
            }
            editor.apply();
            SerialPortManager.instance().sendCommand(Cmd.generateCmdWithData(Cmd.SET_UNIT, unit));
        }

    }

    /**
     * 关闭串口
     */
    public void close() {
        if (null != SerialPortManager.instance()) {
            SerialPortManager.instance().close();
        }
    }
    /**
     * 设置发送模式
     * @param mode 发送模式
     */
    public void setMode(String mode) {
        SerialPortManager.instance().sendCommand(Cmd.generateCmdWithData(Cmd.SET_MODE, mode));
    }

    /**
     * 获取净重量
     */
    public void getWeight() {
        SerialPortManager.instance().sendCommand(Cmd.generateCmd(Cmd.GET_WEIGHT));
    }

    public String getUnit(Context context) {
        return context.getSharedPreferences(MESSAGE, Context.MODE_PRIVATE).getString(UNIT, "KG");
    }

    public float parseWeight(Context context, String[] strings) {
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
        if (strings[3].equalsIgnoreCase(Cmd.NEGATIVE)) {
            result = "-" + result;
        }
        float result2 = Float.parseFloat(result);
        String unit = getUnit(context);
        switch (unit) {
            case "KG": {
                result2 /= 1000;
            }
            break;
            case "HALF_KG": {
                result2 /= 500;
            }
            break;
            case "G": {
            }
            break;
        }
        return result2;
    }
}
