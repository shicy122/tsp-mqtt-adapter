package com.hycan.idn.adapter.biz.internal;

import com.hycan.idn.adapter.biz.enums.InternalMessageEnum;
import com.hycan.idn.adapter.biz.pojo.dto.InternalMessageDTO;
import com.hycan.idn.adapter.biz.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 集群消息抽象类
 *
 * @author shichongying
 * @datetime 2023年 03月 02日 17:34
 */
@Slf4j
public abstract class AbstractInnerChannel {

    private final List<Watcher> watchers;
    private final String hostName;

    protected AbstractInnerChannel(List<Watcher> watchers) {
        Assert.notNull(watchers, "watchers can't be null");

        this.watchers = watchers;
        this.hostName = IpUtil.getHostIp();
    }

    /**
     * 分发集群消息，当前处理类别：
     * <ol>
     *     <li>连接状态上报 {@link InternalMessageEnum#CONNECT_STATUS}</li>
     * </ol>
     *
     * @param im      消息内容
     * @param channel 订阅频道
     */
    public <T> void dispatch(InternalMessageDTO<T> im, String channel) {
        // 屏蔽自己发的消息
        if (hostName.equals(im.getHostAddress())) {
            return;
        }

        for (Watcher watcher : watchers) {
            if (watcher.support(channel)) {
                watcher.action(im);
            }
        }
    }
}
