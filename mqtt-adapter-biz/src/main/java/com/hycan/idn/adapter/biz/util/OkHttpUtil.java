package com.hycan.idn.adapter.biz.util;

import com.hycan.idn.adapter.biz.exception.AdapterBusinessException;
import com.hycan.idn.adapter.biz.pojo.OkHttpResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * OK HTTP工具类
 *
 * @author shichongying
 * @datetime 2023年 02月 24日 15:48
 */
@Slf4j
@Component
public class OkHttpUtil {

    private final OkHttpClient okHttpClient;

    public OkHttpUtil(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    /**
     * request请求入口，请求的返回值这里处理的比较简单，如果需要响应数据，这里需要封装一个ResponseDTO
     *
     * @return 请求成功/失败
     */
    public OkHttpResponse request(Request request) throws AdapterBusinessException {
        try (Response response = okHttpClient.newCall(request).execute()) {
            int status = response.code();
            OkHttpResponse okHttpResponse = new OkHttpResponse();
            okHttpResponse.setCode(status);
            okHttpResponse.setSuccess(HttpStatus.valueOf(status).is2xxSuccessful());
            if (Objects.nonNull(response.body())) {
                okHttpResponse.setBody(response.body().string());
            }
            return okHttpResponse;
        } catch (Exception e) {
            log.error("HTTP请求异常, URL=[{}], Method=[{}]", request.url(), request.method());
        }
        return null;
    }
}