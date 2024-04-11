package com.hycan.idn.adapter.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 桥接消息
 *
 * @author shichongying
 * @datetime 2023年 03月 22日 19:02
 */
@Data
public class BridgeMsgDTO implements Serializable {

    private static final long serialVersionUID = 4967108846565995342L;

    private String topic;
    private byte[] payload;
}
