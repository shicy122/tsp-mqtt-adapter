package com.hycan.idn.adapter.biz.internal.handler;

import com.hycan.idn.adapter.biz.enums.InternalMessageEnum;
import com.hycan.idn.adapter.biz.internal.Watcher;
import com.hycan.idn.adapter.biz.pojo.dto.ConnectStatusDTO;
import com.hycan.idn.adapter.biz.pojo.dto.InternalMessageDTO;
import com.hycan.idn.adapter.biz.service.IConnectStatusService;
import org.springframework.stereotype.Component;

/**
 * 连接状态集群消息处理器
 *
 * @author shichongying
 * @datetime 2023年 03月 02日 17:34
 */
@Component
public class ConnectStatusHandler implements Watcher {

    private final IConnectStatusService connectStatusService;

    public ConnectStatusHandler(IConnectStatusService connectStatusService) {
        this.connectStatusService = connectStatusService;
    }

    @Override
    public <T> void action(InternalMessageDTO<T> im) {
        ConnectStatusDTO dto = (ConnectStatusDTO) im.getData();
        connectStatusService.setConnectStatus(dto.getVin(), dto.getPeriod(), true);
    }

    @Override
    public boolean support(String channel) {
        return InternalMessageEnum.CONNECT_STATUS.getChannel().equals(channel);
    }
}
