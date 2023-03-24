package com.etv.udp;

/**
 * Created by jsjm on 2018/4/17.
 */

public interface UdpMessageListener {
    void receiveMessageState(boolean isTrue, String messageReceive, String sendIp);

}
