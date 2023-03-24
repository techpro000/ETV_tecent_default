package com.etv.util.apwifi;

public class EthManager {


    //打开和关闭以太网
    // Intent intent = new Intent("com.xbh.action.ENABLE_ETHERNET");
    // intent.putExtra("flag", true);//true则打开，false则关闭
    // context.sendBroadcast(intent);
    // 设置以太网为动态获取IP模式
    // Intent intent = new Intent("com.xbh.action.SET_ETHERNET_MODE");
    // intent.putExtra("mode", "auto");
    // context.sendBroadcast(intent);
    // 设置以太网静态IP
    // Intent ipIntent = new Intent("com.xbh.action.SET_ETHERNET_MODE");
    // ipIntent.putExtra("mode", "static");
//    ipIntent.putExtra("ip", "192.168.1.100");
//    ipIntent.putExtra("gateway", "192.168.1.1");
//    ipIntent.putExtra("netMask", "255.255.255.0");
//    ipIntent.putExtra("dns1", "8.8.8.8");
//    ipIntent.putExtra("dns2", "4.4.4.4");
//    context.sendBroadcast(ipIntent);


    //获取网路哦信息
    //首先发送以下广播:
    // Intent intent = new Intent("com.xbh.action.GET_NET_INFO");
    // context.sendBroadcast(intent);
    // 系统返回以下广播，请在APP中接收"com.xbh.action.NET_INFO"
    // 注意,如网络未链接,返回type为"null"，并且其他信息为空
    // if (intent.getAction().equals("com.xbh.action.NET_INFO")) {
    // String type = intent.getStringExtra("type");
    // wifi,mobile,eth
    // String mode = intent.getStringExtra("mode");
    // static, dhcp
    // String ip = intent.getStringExtra("ip");
    // String gateway = intent.getStringExtra("gateway");
    // String netmask = intent.getStringExtra("netmask");
    // String dns1 = intent.getStringExtra("dns1");
    // String dns2 = intent.getStringExtra("dns2");
    // }


}
