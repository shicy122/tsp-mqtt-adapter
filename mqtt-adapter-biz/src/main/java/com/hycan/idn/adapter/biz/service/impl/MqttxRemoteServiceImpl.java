package com.hycan.idn.adapter.biz.service.impl;

import com.hycan.idn.adapter.biz.config.AdapterConfig;
import com.hycan.idn.adapter.biz.constant.CommonConstants;
import com.hycan.idn.adapter.biz.constant.ConnectStatusConstants;
import com.hycan.idn.adapter.biz.constant.RedisKeyConstants;
import com.hycan.idn.adapter.biz.mqtt.MqttGateway;
import com.hycan.idn.adapter.biz.pojo.OkHttpResponse;
import com.hycan.idn.adapter.biz.pojo.dto.PubMsgDTO;
import com.hycan.idn.adapter.biz.service.IConnectStatusService;
import com.hycan.idn.adapter.biz.service.IMqttxRemoteService;
import com.hycan.idn.adapter.biz.util.MqttTopicUtil;
import com.hycan.idn.adapter.biz.util.OkHttpUtil;
import com.hycan.idn.common.core.util.JsonUtil;
import com.hycan.idn.tsp.common.core.util.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author shichongying
 * @datetime 2023年 03月 04日 11:13
 */
@Slf4j
@Service
public class MqttxRemoteServiceImpl implements IMqttxRemoteService {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final String PWD_URI = "/api/v1/mqtt/encrypt/%s";
    private static final String OFFLINE_URI = "/api/v1/mqtt/disconnect";

    private final OkHttpUtil okHttpUtil;

    private final MqttGateway mqttGateway;

    private final RedisTemplate<String, Object> redisTemplate;
    private final IConnectStatusService connectStatusService;

    private final String endpoint;

    public MqttxRemoteServiceImpl(OkHttpUtil okHttpUtil, AdapterConfig adapterConfig,
                                  MqttGateway mqttGateway, RedisTemplate<String, Object> redisTemplate,
                                  IConnectStatusService connectStatusService) {
        this.okHttpUtil = okHttpUtil;
        this.mqttGateway = mqttGateway;
        this.endpoint = adapterConfig.getMqttx().getEndpoint();
        this.redisTemplate = redisTemplate;
        this.connectStatusService = connectStatusService;
    }

    /**
     * 调用MQTTX接口获取密码，直到获取成功
     *
     * @return 密码
     */
    @Override
    public char[] searchEncryptPwd(String clientId) {
        while (true) {
            try {
                String uri = String.format(PWD_URI, clientId);
                Request request = new Request.Builder()
                        .get()
                        .url(endpoint + uri)
                        .build();
                OkHttpResponse response = okHttpUtil.request(request);
                log.debug("search encrypt pwd response is {}", response.isSuccess());
                String pwd = response.getBody();
                if (StringUtils.isNotEmpty(pwd)) {
                    return pwd.toCharArray();
                }
            } catch (Exception e) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * 发送客户端下线事件（心跳超时强制下线场景）
     */
    @Override
    public void sendOfflineEvent(List<String> clientIds, String bizType) {
        if (CollectionUtils.isEmpty(clientIds)) {
            return;
        }

        RequestBody body = RequestBody.create(JSON, JsonUtil.writeValueAsString(clientIds));
        Request request = new Request.Builder()
                .post(body)
                .url(endpoint + OFFLINE_URI)
                .build();

        boolean isSuccess = false;
        try {
            OkHttpResponse response = okHttpUtil.request(request);
            if (Objects.nonNull(response)) {
                log.debug("send offline event response is {}", response.isSuccess());
                isSuccess = response.isSuccess();
            }
        } catch (Exception e) {
            log.error("发送客户端下线请求异常, 异常原因:=[{}]", ExceptionUtil.getBriefStackTrace(e));
        }

        if (isSuccess) {
            sendVehicleOffline(clientIds);
        } else {
            retryOfflineEvent(clientIds, bizType);
        }
    }

    /**
     * 发送车辆离线状态
     *
     * @param clientIds 客户端ID列表
     */
    private void sendVehicleOffline(List<String> clientIds) {
        for (String clientId : clientIds) {
            String vin = MqttTopicUtil.getVinByClientId(clientId);
            if (StringUtils.isEmpty(vin)) {
                continue;
            }
            connectStatusService.sendVehicleStatus(vin, ConnectStatusConstants.OFFLINE, System.currentTimeMillis());
        }
    }

    /**
     * 发送下线请求失败时，重新放入到队列中
     *
     * @param clientIds 客户端ID列表
     * @param bizType 业务类型(FORCE_OFFLINE: 在线超过24小时强制下线, HEARTBEAT: 心跳上报超时)
     */
    private void retryOfflineEvent(List<String> clientIds, String bizType) {
        for (String clientId : clientIds) {
            String vin = MqttTopicUtil.getVinByClientId(clientId);
            if (StringUtils.isEmpty(vin)) {
                continue;
            }

            if (CommonConstants.FORCE_OFFLINE.equals(bizType)) {
                redisTemplate.opsForZSet().add(RedisKeyConstants.FORCE_OFFLINE_TIME, vin, Math.addExact(System.currentTimeMillis(), 5000));
            } else if (CommonConstants.HEARTBEAT.equals(bizType)) {
                redisTemplate.opsForZSet().add(RedisKeyConstants.HEART_BEATER_TIMEOUT, vin, Math.addExact(System.currentTimeMillis(), 5000));
            }
        }
    }

    /**
     * 向客户端发送消息
     */
    @Override
    public void publishMessage(PubMsgDTO pubMsg, boolean isRetryMsg) {
        try {
            mqttGateway.send(pubMsg.getTopic(), pubMsg.getQos(), pubMsg.getPayload());
        } catch (Exception e) {
            log.error("发送MQTT消息失败，100ms后重新发送，异常原因=[{}]", ExceptionUtil.getBriefStackTrace(e));

            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            if (!isRetryMsg) {
                publishMessage(pubMsg, true);
            }
        }
    }
}
