package com.hycan.idn.adapter.biz.pojo;

import lombok.Data;

@Data
public class OkHttpResponse {

    /** HTTP响应码 */
    private int code;

    /** 成功/失败 */
    private boolean isSuccess;

    /** 响应的内容 */
    private String body;
}
