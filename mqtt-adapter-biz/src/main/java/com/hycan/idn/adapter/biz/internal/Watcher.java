package com.hycan.idn.adapter.biz.internal;

import com.hycan.idn.adapter.biz.enums.InternalMessageEnum;
import com.hycan.idn.adapter.biz.pojo.dto.InternalMessageDTO;

/**
 * 集群消息观察者接口
 *
 * @author shichongying
 * @datetime 2023年 03月 02日 17:34
 */
public interface Watcher {

    /**
     * 每当有新的集群消息达到是，触发行为。
     * 注意：实现方法不应该有耗时操作(e.g. 访问数据库)
     *
     * @param im 集群消息
     */
    <T> void action(InternalMessageDTO<T> im);

    /**
     * Watcher 支持的 channel 类别
     *
     * @param channel {@link InternalMessageEnum}
     * @return true if Watcher support
     */
    boolean support(String channel);
}