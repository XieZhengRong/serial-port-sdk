package com.android.serialportlibrary.util;

public class Cmd {
    //接收开头
    public static final String RECEIVE_START = "68";
    //发送开头
    public static final String SEND_START = "28";
    //结尾
    public static final String END = "78";
    //连接表头
    public static final String CONNECT = "01";
    //获取重量
    public static final String GET_WEIGHT = "03";
    //表头置零
    public static final String RESET = "20";
    //去皮
    public static final String REMOVE_SKIN = "21";
    //回皮
    public static final String BACK_SKIN = "11";
    //回毛
    public static final String BACK_HAIR = "12";
    /**
     * 设置模式 指令:61
     * 01:连续发送（主动发送）*默认*
     * 02:稳定后连续发送（主动发送）
     * 03:稳定后单次发送（被动发送）
     * 04:归零后稳定单次发送（被动发送）
     * 05:按键发送（被动发送，一键上传）
     */
    public static final String SET_MODE = "61";
    public static final String MODE_CONTINUE_TRIGGER = "01";
    public static final String MODE_STABLE_CONTINUE_TRIGGER = "02";
    public static final String MODE_STABLE_SINGLE_TRIGGER = "03";
    public static final String MODE_ZERO_STABLE_SINGLE_TRIGGER = "04";
    public static final String MODE_SINGLE_TRIGGER = "05";
    //设定单位为公斤
    public static final String SET_KG = "0a";
    //设定单位为市斤
    public static final String SET_HALF_KG = "1a";
    //设定单位单位
    public static final String SET_UNIT = "3A";
    //设定单位克
    public static final String SET_G = "2a";
    //指令格式错误
    public static final String FORMAT_ERROR = "00";
    //成功
    public static final String SUCCESS = "1a";
    //失败
    public static final String FAIL = "5a";
    //正数
    public static final String POSITIVE = "20";
    //负数
    public static final String NEGATIVE = "2d";

    public static String generateCmd(String cmd) {
        //0x68 0x0a 0x03 0xaa 0xaa 0xaa 0xaa 0xaa 0xff 0xf8
        //包头 帧长 指令 数据 检验 包尾
        String length = "04";
        return SEND_START + length + cmd + END;
    }

    public static String generateCmdWithData(String cmd, String data) {
        //0x68 0x0a 0x03 0xaa 0xaa 0xaa 0xaa 0xaa 0xff 0xf8
        //包头 帧长 指令 数据 检验 包尾
        String length = "05";
        return SEND_START + length + cmd + data + END;
    }
    public static String generateCmdWithWeight(String cmd, String data) {
        //0x68 0x0a 0x03 0xaa 0xaa 0xaa 0xaa 0xaa 0xff 0xf8
        //包头 帧长 指令 数据 检验 包尾
        String length = "09";
        return SEND_START + length + cmd + data + END;
    }
}
