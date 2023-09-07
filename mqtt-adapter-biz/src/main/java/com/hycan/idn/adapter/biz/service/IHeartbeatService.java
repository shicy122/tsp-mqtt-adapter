package com.hycan.idn.adapter.biz.service;

/**
 * 心跳Service
 *
 * @author shichongying
 * @datetime 2023年 02月 26日 14:12
 */
public interface IHeartbeatService {

    /**
     * 更新心跳过期过期时间
     *
     * @param vin             VIN码
     * @param tBoxStatus      TBOX状态
     * @param heartbeatPeriod 心跳周期
     */
    void setHeartbeatExpire(String vin, int tBoxStatus, int heartbeatPeriod);

    /**
     * 设置默认心跳过期时间
     *
     * @param vin        VIN码
     * @param bBoxStatus TBOX状态
     */
    void setDefaultHeartbeatExpire(String vin, int bBoxStatus);

    /**
     * 删除心跳
     *
     * @param vin VIN码
     */
    void removeHeartbeatExpire(String vin);
}
