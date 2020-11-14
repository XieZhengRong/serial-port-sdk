package com.ikudot.mylibrary.util;

public class Cmd {
    //接收开头
    public static final  byte RECEIVE_START = 0x68;
    //发送开头
    public static final byte SEND_START = 0x28;
    //结尾
    public static final byte END = 0x78;
    //连接表头
    public static final byte CONNECT = 0x01;
    //获取重量
    public static final byte GET_WEIGHT = 0x03;
    //表头置零
    public static final byte RESET = 0x20;
    //去皮
    public static final byte REMOVE_SKIN = 0x21;
    //回皮
    public static final byte BACK_SKIN = 0x11;
    //回毛
    public static final byte BACK_HAIR = 0x12;
    //设定单位为公斤
    public static final byte SET_KG = 0x0a;
    //设定单位为市斤
    public static final byte SET_HALF_KG = 0x1a;
    //设定单位单位
    public static final byte SET_UNIT = 0x3a;
    //设定单位为港斤
    public static final byte SET_SPECIAL_KG = 0x2a;
    //指令格式错误
    public static final byte FORMAT_ERROR = 0x00;
    //成功
    public static final byte SUCCESS = 0x1a;
    //失败
    public static final byte FAIL = 0x5a;

    public static byte[] generateCmd(byte cmd) {
        //0x68 0x0a 0x03 0xaa 0xaa 0xaa 0xaa 0xaa 0xff 0xf8
        //包头 帧长 指令 数据 检验 包尾
        byte length = 0x04;
        return new byte[]{SEND_START,length,cmd,END};
    }
    public static byte[] generateCmdWithData(byte cmd,byte data) {
        //0x68 0x0a 0x03 0xaa 0xaa 0xaa 0xaa 0xaa 0xff 0xf8
        //包头 帧长 指令 数据 检验 包尾
        byte length = 0x05;
        return new byte[]{SEND_START,length,cmd,data,END};
    }

}
