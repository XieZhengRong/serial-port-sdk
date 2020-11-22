package com.ikudot.serialportlibrary.util;

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
    //设定单位为公斤
    public static final String SET_KG = "0a";
    //设定单位为市斤
    public static final String SET_HALF_KG = "1a";
    //设定单位单位
    public static final String SET_UNIT = "3a";
    //设定单位为港斤
    public static final String SET_SPECIAL_KG = "2a";
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
