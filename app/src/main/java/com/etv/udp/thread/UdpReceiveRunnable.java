package com.etv.udp.thread;

import android.os.Handler;
import android.util.Log;

import com.etv.udp.UDPConfig;
import com.etv.udp.UdpMessageListener;
import com.etv.util.MyLog;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UdpReceiveRunnable implements Runnable {

    private DatagramSocket receiveSocket;
    private boolean listenStatus = true;  //接收线程的循环标识
    UdpMessageListener listener;
    private Handler handler = new Handler();

    public UdpReceiveRunnable(UdpMessageListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            Log.e("receiver", "==准备接收数据===0000000");
            receiveSocket = new DatagramSocket(UDPConfig.UDP_PORT);
            while (listenStatus) {
                byte[] inBuf = new byte[1024];
                DatagramPacket inPacket = new DatagramPacket(inBuf, inBuf.length);
                receiveSocket.receive(inPacket);
                String senderIp = inPacket.getAddress().getHostAddress();
                String messageBack = new String(inPacket.getData(), 0, inPacket.getLength());
                backMessage(true, messageBack, senderIp);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void backMessage(boolean isTrue, String messageBack, String senderIp) {
        if (listener == null) {
            return;
        }
//        String messageBack = messageBJson.substring(0, messageBJson.lastIndexOf("}") + 1);
        MyLog.message("=======接受到数据=原声==" + messageBack);
        handler.post(new Runnable() {
            @Override
            public void run() {
                listener.receiveMessageState(isTrue, messageBack, senderIp);
            }
        });
    }

    public void stopReceiveMessage() {
        try {
            listenStatus = false;
            receiveSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
