package com.hycan.idn.adapter.biz.exception;

import com.hycan.idn.adapter.biz.enums.ErrorCodeEnum;
import com.hycan.idn.tsp.common.core.constant.CommonConstants;
import lombok.Getter;

/**
 * 业务异常
 *
 * @author shichongying
 * @datetime 2023年 03月 01日 10:18
 */
@Getter
public class AdapterBusinessException extends Exception {

    private int code;
    private Object data;

    public AdapterBusinessException(String msg) {
        super(msg);
        this.code = CommonConstants.FAIL;
    }

    public AdapterBusinessException(String msg, int code) {
        super(msg);
        this.code = code;
    }

    public AdapterBusinessException(ErrorCodeEnum errorCodeEnum) {
        super(errorCodeEnum.getMessage());
        this.code = errorCodeEnum.getCode();
    }

    public AdapterBusinessException(String msg, int code, Object data) {
        super(msg);
        this.code = code;
        this.data = data;
    }

}
