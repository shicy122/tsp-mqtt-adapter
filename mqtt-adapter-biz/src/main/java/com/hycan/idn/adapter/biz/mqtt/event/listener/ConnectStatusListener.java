package com.hycan.idn.adapter.biz.mqtt.event.listener;

import com.hycan.idn.adapter.biz.config.AdapterConfig;
import com.hycan.idn.adapter.biz.constant.ConnectStatusConstants;
import com.hycan.idn.adapter.biz.mqtt.event.UpMessageEvent;
import com.hycan.idn.adapter.biz.service.IConnectStatusService;
import com.hycan.idn.adapter.biz.service.IHeartbeatService;
import com.hycan.idn.adapter.biz.util.DecodeUtil;
import com.hycan.idn.adapter.biz.util.ThreadPoolUtil;
import com.hycan.idn.common.core.dto.HeartConfig;
import com.hycan.idn.common.core.util.BytesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 连接状态上报监听器
 *
 * @author shichongying
 * @datetime 2023年 02月 26日 9:38
 */
@Slf4j
@Component
public class ConnectStatusListener {

    private final IConnectStatusService connectStatusService;

    private final IHeartbeatService heartbeatService;

    private final boolean enableLog;

    public ConnectStatusListener(IConnectStatusService connectStatusService,
                                 IHeartbeatService heartbeatService,
                                 AdapterConfig config) {
        this.connectStatusService = connectStatusService;
        this.heartbeatService = heartbeatService;
        this.enableLog = config.getLog().getEnableConnectStatus();
    }

    @Async(ThreadPoolUtil.OTHER_MSG_THREAD)
    @EventListener(condition = "T(com.hycan.idn.adapter.biz.constant.EventTypeConstants).CONNECT_STATUS.equals(#event.type)")
    public void handleEvent(UpMessageEvent event) {
        String vin = event.getVin();
        HeartConfig heartConfig = DecodeUtil.decodeConnectStatus(event.getPayload());

        int tBoxStatus = heartConfig.getTBoxStatus();
        int heartValue = heartConfig.getHeartValue();
        if (enableLog) {
            log.info("连接状态变化: VIN=[{}], TBOX状态=[{}](1:工作状态 2:低功耗), 心跳时长=[{}]", vin, tBoxStatus, heartValue);
        }
        if (!validHeartConfig(heartConfig)) {
            log.error("连接状态报文校验失败! VIN=[{}], TBOX状态=[{}], 心跳时长=[{}], Payload=[{}]",
                    vin, tBoxStatus, heartValue, BytesUtil.bytesToHexString(event.getPayload()));
            return;
        }
        // 设置心跳周期时间
        connectStatusService.setConnectStatus(vin, heartConfig.getHeartValue(), false);
        // 设置心跳过期时间
        heartbeatService.setHeartbeatExpire(vin, heartConfig.getTBoxStatus(), heartConfig.getHeartValue());
        // 向车况服务发送车辆在线状态(0--不在线 1--正常在线 2--睡眠)
        connectStatusService.sendVehicleStatus(vin, heartConfig.getTBoxStatus(), System.currentTimeMillis());
    }

    private boolean validHeartConfig(HeartConfig heartConfig) {
        int heartValue = heartConfig.getHeartValue();
        int tBoxStatus = heartConfig.getTBoxStatus();

        if (heartValue <= 0) {
            return false;
        }

        return ConnectStatusConstants.WORK_MODE == tBoxStatus || ConnectStatusConstants.HIBERNATE_MODE == tBoxStatus;
    }
}
