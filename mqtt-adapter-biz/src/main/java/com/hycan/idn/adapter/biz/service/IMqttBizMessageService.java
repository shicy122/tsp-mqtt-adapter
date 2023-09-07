package com.hycan.idn.adapter.biz.service;

import com.hycan.idn.tsp.engine.command.AcpRawMessage;

/**
 * 协议业务消息Service
 *
 * @author shichongying
 * @datetime 2023年 02月 26日 14:12
 */
public interface IMqttBizMessageService {

    /**
     * 发送上行消息
     *
     * @param vin        VIN号
     * @param appId      协议标识
     * @param acpMessage 消息RawData部分
     * @param isAckMsg   是否为ACK消息
     */
    void sendUpMessage(String vin, int appId, AcpRawMessage acpMessage, boolean isAckMsg);

    /**
     * 发送下行消息
     *
     * @param appId   协议标识
     * @param serial  车系
     * @param vin     VIN号
     * @param rawData 消息RawData部分
     */
    void sendDownMessage(int appId, String serial, String vin, byte[] rawData);

    /**
     * 需要TBOX回复应答消息
     *
     * @param appId 协议标识
     * @return true/false
     */
    boolean isAckMessage(int appId);
}
