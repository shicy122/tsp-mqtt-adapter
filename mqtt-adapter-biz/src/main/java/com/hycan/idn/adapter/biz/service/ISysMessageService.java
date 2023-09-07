package com.hycan.idn.adapter.biz.service;

/**
 * 系统消息Service
 *
 * @author shichongying
 * @datetime 2023年 02月 26日 14:12
 */
public interface ISysMessageService {

    /**
     * 设置默认下线时间，连续在线超过24小时，强制下线
     *
     * @param vin             VIN码
     */
    void setOfflineExpire(String vin);

    /**
     * 删除下线时间
     *
     * @param vin VIN码
     */
    void removeOfflineExpire(String vin);
}
