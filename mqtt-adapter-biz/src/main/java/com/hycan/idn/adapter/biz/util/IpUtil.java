package com.hycan.idn.adapter.biz.util;

import cn.hutool.core.text.StrPool;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 获取 IP 工具类
 *
 * @author shichongying
 * @datetime 2023年 01月 06日 11:20
 */
@Slf4j
public class IpUtil {

    public static String getHostIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.info("Get host throw exception=[{}]", e.getMessage());
            return "unknown";
        }
    }

    public static String getHostName() {
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            if (hostName.contains(StrPool.DOT)) {
                hostName = hostName.substring(0, hostName.indexOf(StrPool.DOT));
            }
            return hostName;
        } catch (UnknownHostException e) {
            log.info("Get host name throw exception=[{}]", e.getMessage());
            return "unknown";
        }
    }
}
