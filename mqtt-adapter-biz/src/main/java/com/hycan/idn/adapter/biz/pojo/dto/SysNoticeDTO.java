package com.hycan.idn.adapter.biz.pojo.dto;

import lombok.Data;

/**
 * 接收MQTT Broker的客户端上/下线通知DTO
 *
 * @author shichongying
 * @datetime 2023年 03月 02日 10:34
 */
@Data
public class SysNoticeDTO {

    /** 客户端连接类型 connect-上线, disconnect-下线 */
    private String type;

    /** MQTT客户端Id */
    private String clientId;

    public static SysNoticeDTO of(String type, String clientId) {
        SysNoticeDTO dto = new SysNoticeDTO();
        dto.setType(type);
        dto.setClientId(clientId);
        return dto;
    }
}
