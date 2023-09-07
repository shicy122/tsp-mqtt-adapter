package com.hycan.idn.adapter.biz.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 车辆信息 DTO
 *
 * @author shichongying
 * @datetime 2023年 02月 27日 13:50
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleInfoDTO {
    private String serial;
    private String vin;
}
