package com.hycan.idn.adapter.biz.service;

import com.hycan.idn.adapter.biz.pojo.dto.PubMsgDTO;

import java.util.List;

/**
 * MQTTX Rest接口Service
 *
 * @author shichongying
 * @datetime 2023年 02月 26日 14:12
 */
public interface IMqttxRemoteService {

    /**
     * 获取连接MQTT所需的密码
     *
     * @param clientId 客户端ID
     * @return 密码
     */
    char[] searchEncryptPwd(String clientId);

    /**
     * 发送客户端下线事件（心跳超时强制下线场景）
     *
     * @param clientIds 客户端ID列表
     */
    void sendOfflineEvent(List<String> clientIds, String bizType);

    /**
     * 向客户端发送消息
     *
     * @param pubMsg 消息结构体
     * @param isRetryMsg 是否为重试消息
     */
    void publishMessage(PubMsgDTO pubMsg, boolean isRetryMsg);
}
