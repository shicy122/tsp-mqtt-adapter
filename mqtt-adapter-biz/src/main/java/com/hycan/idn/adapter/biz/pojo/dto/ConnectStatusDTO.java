package com.hycan.idn.adapter.biz.pojo.dto;

import lombok.Data;

/**
 * Adapter发布车辆连接心跳周期DTO
 *
 * @author shichongying
 * @datetime 2023年 03月 02日 10:34
 */
@Data
public class ConnectStatusDTO {

    /**
     * 车架号
     */
    private String vin;

    /**
     * 心跳周期
     */
    private Integer period;

    public static ConnectStatusDTO of(String vin, int period) {
        ConnectStatusDTO connectStatusDTO = new ConnectStatusDTO();
        connectStatusDTO.setVin(vin);
        connectStatusDTO.setPeriod(period);
        return connectStatusDTO;
    }
}
