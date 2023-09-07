package com.hycan.idn.adapter.biz.pojo.dto;

import lombok.Data;

/**
 * 发送车辆状态通知DTO
 *
 * @author shichongying
 * @datetime 2023年 02月 27日 13:50
 */
@Data
public class VehicleStatusDTO {

    /**
     * 车架号
     */
    private String vin;

    /**
     * 车辆在线状态 0--不在线；1--正常在线；2--睡眠
     */
    private Integer vehicleStatus;

    /**
     * 数据更新时间
     */
    private Long updateTime;

    public static VehicleStatusDTO of(String vin, Integer vehicleStatus) {
        VehicleStatusDTO dto = new VehicleStatusDTO();
        dto.setVin(vin);
        dto.setVehicleStatus(vehicleStatus);
        dto.setUpdateTime(System.currentTimeMillis());
        return dto;
    }
}
