package com.hycan.idn.adapter.biz.mqtt.event.listener;

import com.hycan.idn.adapter.biz.config.AdapterConfig;
import com.hycan.idn.adapter.biz.mqtt.event.UpMessageEvent;
import com.hycan.idn.adapter.biz.service.IConnectStatusService;
import com.hycan.idn.adapter.biz.service.IHeartbeatService;
import com.hycan.idn.adapter.biz.util.ThreadPoolUtil;
import com.hycan.idn.common.core.util.BytesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 心跳监听器
 *
 * @author shichongying
 * @datetime 2023年 02月 26日 9:45
 */
@Slf4j
@Component
public class HeartbeatListener {

    private final IHeartbeatService heartbeatService;

    private final IConnectStatusService connectStatusService;

    private final boolean enableLog;

    public HeartbeatListener(IHeartbeatService heartbeatService,
                             IConnectStatusService connectStatusService,
                             AdapterConfig config) {
        this.heartbeatService = heartbeatService;
        this.connectStatusService = connectStatusService;
        this.enableLog = config.getLog().getEnableHeartbeat();
    }

    /**
     * TBOX心跳报文上传处理 (1: 工作心跳  2: 休眠心跳)
     */
    @Async(ThreadPoolUtil.OTHER_MSG_THREAD)
    @EventListener(condition = "T(com.hycan.idn.adapter.biz.constant.EventTypeConstants).HEARTBEAT.equals(#event.type)")
    public void handleEvent(UpMessageEvent event) {
        // 心跳报文总长度18个字节，其中Header长度17个字节，rawData长度1个字节
        int tBoxStatus = BytesUtil.getByte(17, event.getPayload());
        int heartbeatPeriod = connectStatusService.getConnectStatus(event.getVin(), tBoxStatus);

        if (enableLog) {
            log.info("心跳上报: VIN码=[{}], TBOX状态=[{}](1:工作状态 2:低功耗), 心跳周期=[{}]", event.getVin(), tBoxStatus, heartbeatPeriod);
        }

        heartbeatService.setHeartbeatExpire(event.getVin(), tBoxStatus, heartbeatPeriod);
    }
}
