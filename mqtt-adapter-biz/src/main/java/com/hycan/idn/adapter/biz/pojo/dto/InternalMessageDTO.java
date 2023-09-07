package com.hycan.idn.adapter.biz.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 集群内通知消息DTO
 *
 * @author shichongying
 * @datetime 2023年 03月 02日 10:34
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InternalMessageDTO<T> {

    /** 数据 */
    private T data;

    /** 时间戳 */
    private Long timestamp;

    /** host address */
    private String hostAddress;
}
