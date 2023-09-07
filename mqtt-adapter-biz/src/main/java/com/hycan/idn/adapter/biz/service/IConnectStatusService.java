package com.hycan.idn.adapter.biz.service;

/**
 * 连接状态Service
 *
 * @author shichongying
 * @datetime 2023年 02月 26日 14:12
 */
public interface IConnectStatusService {

    /**
     * 心跳模式切换时，设置心跳周期(单位:秒)
     *
     * @param vin             VIN号
     * @param heartbeatPeriod 心跳时长
     */
    void setConnectStatus(String vin, int heartbeatPeriod, boolean isClusterMessage);

    /**
     * 获取心跳周期
     *
     * @param vin        VIN号
     * @param tBoxStatus TBOX状态
     * @return 心跳周期
     */
    int getConnectStatus(String vin, int tBoxStatus);

    /**
     * 发送车辆状态到上游业务服务
     *
     * @param vin           VIN号
     * @param vehicleStatus TBOX状态
     * @param sendTime      发送时间
     */
    void sendVehicleStatus(String vin, int vehicleStatus, long sendTime);
}
