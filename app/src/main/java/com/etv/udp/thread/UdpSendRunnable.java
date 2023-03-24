package com.etv.udp.thread;

import android.os.Handler;


import com.etv.udp.UDPConfig;
import com.etv.udp.UdpSendMessageListener;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpSendRunnable implements Runnable {

    private DatagramSocket sendSocket;
    private InetAddress serverAddr;
    UdpSendMessageListener listener;
    private Handler handler = new Handler();
    String sendIp;
    byte[] byteSend = null;

    public UdpSendRunnable(String jsonSend, String sendIp, UdpSendMessageListener listener) {
        byte[] buf = jsonSend.getBytes();
        setMessageInfo(buf, sendIp, listener);
    }

    public UdpSendRunnable(byte[] byteSend, String sendIp, UdpSendMessageListener listener) {
        setMessageInfo(byteSend, sendIp, listener);
    }

    private void setMessageInfo(byte[] byteSend, String sendIp, UdpSendMessageListener listener) {
        this.listener = listener;
        this.sendIp = sendIp;
        this.byteSend = byteSend;
    }

    @Override
    public void run() {
        try {
            sendSocket = new DatagramSocket();
            serverAddr = InetAddress.getByName(sendIp);
            DatagramPacket outPacket = new DatagramPacket(byteSend, byteSend.length, serverAddr, UDPConfig.UDP_PORT);
            sendSocket.send(outPacket);
            sendSocket.close();
            backMessageState(true);
        } catch (Exception e) {
            backMessageState(false);
            e.printStackTrace();
        }
    }

    private void backMessageState(final boolean b) {
        if (listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.sendMessageState(b);
                }
            });
        }
    }
}