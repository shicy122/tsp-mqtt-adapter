package com.hycan.idn.adapter.biz.service;

import com.hycan.idn.adapter.biz.pojo.dto.InternalMessageDTO;

/**
 * 内部消息发布服务
 *
 * @author shichongying
 * @datetime 2023年 02月 26日 14:12
 */
public interface IInternalMessageService {

    /**
     * 发布集群消息
     *
     * @param im      {@link InternalMessageDTO}
     * @param <T>     {@link InternalMessageDTO#getData()} 类别
     * @param channel 推送频道
     */
    <T> void publish(InternalMessageDTO<T> im, String channel);
}