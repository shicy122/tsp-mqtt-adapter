package com.hycan.idn.adapter.biz.mqtt.event.listener;

import com.hycan.idn.adapter.biz.config.AdapterConfig;
import com.hycan.idn.adapter.biz.enums.ClientConnectEnum;
import com.hycan.idn.adapter.biz.mqtt.event.SysMessageEvent;
import com.hycan.idn.adapter.biz.service.IConnectStatusService;
import com.hycan.idn.adapter.biz.service.IHeartbeatService;
import com.hycan.idn.adapter.biz.service.ISysMessageService;
import com.hycan.idn.adapter.biz.util.MqttTopicUtil;
import com.hycan.idn.adapter.biz.util.ThreadPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 客户端下线系统消息监听器
 *
 * @author shichongying
 * @datetime 2023年 02月 26日 9:45
 */
@Slf4j
@Component
public class ClientOfflineListener {

    private final IConnectStatusService connectStatusService;

    private final IHeartbeatService heartbeatService;

    private final ISysMessageService sysMessageService;

    private final boolean enableLog;

    public ClientOfflineListener(IConnectStatusService connectStatusService,
                                 IHeartbeatService heartbeatService,
                                 ISysMessageService sysMessageService,
                                 AdapterConfig config) {
        this.connectStatusService = connectStatusService;
        this.heartbeatService = heartbeatService;
        this.sysMessageService = sysMessageService;
        this.enableLog = config.getLog().getEnableOffline();
    }

    /**
     * TBOX心跳报文上传处理
     */
    @Async(ThreadPoolUtil.SYS_MSG_THREAD)
    @EventListener(condition = "T(com.hycan.idn.adapter.biz.enums.ClientConnectEnum).OFFLINE.type.equals(#event.type)")
    public void handleEvent(SysMessageEvent event) {
        String clientId = event.getClientId();
        String vin = MqttTopicUtil.getVinByClientId(clientId);
        if (enableLog) {
            log.info("MQTT下线事件处理, VIN码=[{}]", vin);
        }

        heartbeatService.removeHeartbeatExpire(vin);

        sysMessageService.removeOfflineExpire(vin);

        connectStatusService.sendVehicleStatus(vin, ClientConnectEnum.OFFLINE.getValue(), System.currentTimeMillis());
    }
}
