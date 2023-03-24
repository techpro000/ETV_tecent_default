package com.etv.udp;

import android.content.Context;
import android.content.Intent;

import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.service.EtvService;
import com.etv.service.TcpService;
import com.etv.service.TcpSocketService;
import com.etv.udp.thread.UdpReceiveRunnable;
import com.etv.udp.thread.UdpSendRunnable;
import com.etv.util.CodeUtil;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.etv.util.SharedPerUtil;
import com.etv.util.system.SystemManagerUtil;
import com.ys.model.dialog.OridinryDialog;

import org.json.JSONObject;

public class UdpParnsener {

    Context context;

    public UdpParnsener(Context context) {
        this.context = context;
    }

    UdpReceiveRunnable receiverunnable;

    public void receiveUdpMessage() {
        receiverunnable = new UdpReceiveRunnable(new UdpMessageListener() {
            @Override
            public void receiveMessageState(boolean isTrue, String messageReceive, String sendIP) {
                MyLog.udp("========parsenerJsonInfo== isTrue1" + messageReceive);

                if (!isTrue) {
                    MyLog.udp("========parsenerJsonInfo== isTrue2" + messageReceive);
                    return;
                }
                MyLog.udp("========parsenerJsonInfo== isFalse3" + messageReceive);
                parsenerJsonInfo(messageReceive);
            }
        });
        EtvService.getInstance().executor(receiverunnable);
    }


    public void stopReceiveMessage() {
        if (receiverunnable != null) {
            receiverunnable.stopReceiveMessage();
        }
    }

    /***
     * 发送消息
     * @param message
     * @param ip
     */
    public void sendUdpMessage(String message, String ip) {
        MyLog.udp("消息发送====" + message + "  ;" + ip);
        Runnable runnable = new UdpSendRunnable(message, ip, new UdpSendMessageListener() {
            @Override
            public void sendMessageState(boolean isSuccess) {
                MyLog.udp("消息发送成功====" + isSuccess);
            }
        });
        EtvService.getInstance().executor(runnable);
    }

    private void parsenerJsonInfo(String messageReceive) {
        MyLog.udp("========parsenerJsonInfo==" + messageReceive);
        try {
            JSONObject jsonObject = new JSONObject(messageReceive);
            String type = jsonObject.getString("type");
            String sendIp = jsonObject.getString("ipaddress").trim();
            String localIp = CodeUtil.getIpAddress(context, "UDP parsener调用000").trim();
            if (sendIp.equals(localIp)) {
                MyLog.udp("========频闭自己发的消息=============");
                return;
            }
            MyLog.udp("========解析消息==" + type);
            if (type.startsWith("getLocalDev")) {
                //服务器下发UDP指令
                MyLog.udp("========服务器下发的指令==" + type + " / " + sendIp);
                String ipAddress_mine = CodeUtil.getIpAddress(context, "UDP parsener调用111");
                String deviceId = CodeUtil.getUniquePsuedoID();
                String sendMessage = "{\"type\":\"submitDev\",\"ipaddress\":\"" + ipAddress_mine + "\",\"dev_id\":\"" + deviceId + "\"}";
                sendUdpMessage(sendMessage, sendIp);
            } else if (type.startsWith("linkServer")) {
                // 终端这边确认注册连接服务器
                SharedPerManager.setWorkModel(AppInfo.WORK_MODEL_NET, "UDP局域网连接服务器");
                String port = jsonObject.getString("port");
                String username = jsonObject.getString("admin");
                SharedPerManager.setWebHost(sendIp);
                SharedPerManager.setWebPort(port);
                SharedPerManager.setUserName(username, "UDP-解析，保存用户名");
                context.sendBroadcast(new Intent(AppInfo.UDP_SERVER_SEND_IP_PORT));
                if (SharedPerUtil.SOCKEY_TYPE() == AppConfig.SOCKEY_TYPE_WEBSOCKET) {
                    TcpService.getInstance().dealDisOnlineDev("UDP检索设备，这里注册设备,先离线服务器", true);
                    TcpService.getInstance().registerDevToWeb("UDP检测到设备，注册");
                } else {
                    SharedPerManager.setSocketType(AppConfig.SOCKEY_TYPE_WEBSOCKET);
                    SystemManagerUtil.rebootApp(context);
                    //TcpSocketService.getInstance().dealDisOnlineDev("UDP检索设备，这里注册设备,先离线服务器", true);
                    //TcpSocketService.getInstance().registerDevToWeb("UDP检测到设备，注册");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showRebootDialog(String content) {
        /*OridinryDialog oridinryDialog = new OridinryDialog(getActivity());
        oridinryDialog.setCancelable(false);
        oridinryDialog.show(content, false, false);
        handler.postDelayed(()-> SystemManagerUtil.rebootApp(getActivity()), 2000);*/

        /*oridinryDialog.setOnDialogClickListener(new OridinryDialogClick() {
            @Override
            public void sure() {
                SystemManagerUtil.rebootApp(getActivity());
                getActivity().finish();
            }

            @Override
            public void noSure() {

            }
        });*/
    }


}
