package com.hycan.idn.adapter.biz.pojo.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 发送消息到MQTT Broker请求DTO
 *
 * @author shichongying
 * @datetime 2023年 01月 05日 17:00
 */
@Data
public class PubMsgDTO {

    private String clientId;

    @NotBlank
    @Pattern(regexp = "^[0-9a-zA-Z\\-\\/]{0,64}$")
    private String topic;

    @Min(0)
    @Max(2)
    private Integer qos;

    @NotNull
    private byte[] payload;
}
