package com.hycan.idn.adapter.biz.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 协议消息头DTO
 *
 * @author shichongying
 * @datetime 2023年 03月 02日 10:34
 */
@Data
public class MessageHeadDTO implements Serializable {

    private static final long serialVersionUID = 9081406366569775542L;

    /**
     * 协议版本号
     * 表示正文内容是采用哪种编/解码协议版本
     * (从1开始计数到65535)初始版本为1
     */
    private Integer version;

    /**
     * 时间戳为毫秒级，采用 Unix 时间戳
     */
    private Long timestamp;

    /**
     * 表示正文内容字节数量
     */
    private Integer length;

    /**
     * 数据类型标识-appid
     */
    private Integer appId;

    /**
     * 会话序列ID,用于区分响应返回值为哪一条请求所发起
     * （从0开始计数,到65535后重新循环为0）
     */
    private Integer seqId;

    /**
     * 保留字段，Set to 0
     */
    private Integer reserved;
}
