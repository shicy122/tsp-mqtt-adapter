package com.hycan.idn.adapter.biz.util;

import cn.hutool.core.text.StrPool;
import com.hycan.idn.adapter.biz.pojo.dto.SysNoticeDTO;
import com.hycan.idn.adapter.biz.pojo.dto.VehicleInfoDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MQTT Topic 工具类
 *
 * @author shichongying
 * @datetime 2023年 02月 26日 15:59
 */
@Slf4j
public class MqttTopicUtil {

    private static final Pattern SYS_CONNECT_TOPIC = Pattern.compile("^\\$SYS/broker/(.+)/clients/(.+)/(connected|disconnected)$");

    private static final Pattern T_BOX_TOPIC = Pattern.compile("^(up|down)/tbox/(.+)/(.+)/(.+)$");

    private static final int T_BOX_TOPIC_SERIAL_INDEX = 2;

    private static final int T_BOX_TOPIC_VIN_INDEX = 3;

    private static final int SYS_TOPIC_CLIENT_ID_INDEX = 2;

    private static final int SYS_TOPIC_TYPE_INDEX = 3;

    private static final String BRIDGE_CANDATA_PERIOD = "candata_period";
    private static final String BRIDGE_GB_PERIOD = "gb_period";
    private static final String BRIDGE_CANDATA_TRIGGER = "candata_trigger";

    /**
     * TBOX clientId 由 tbox_vin码_1 组成
     */
    private static final int T_BOX_CLIENT_ID_LENGTH = 3;

    /**
     * 是否为MQTT Broker桥接的消息
     * @param topic
     * @return
     */
    public static boolean isBridgeMsg(String topic) {
        return topic.endsWith(BRIDGE_CANDATA_PERIOD) || topic.endsWith(BRIDGE_GB_PERIOD) || topic.endsWith(BRIDGE_CANDATA_TRIGGER);
    }

    /**
     * 从上报的Topic获取车系信息
     *
     * @param topic
     * @return
     */
    public static VehicleInfoDTO vehicleInfo(String topic) {
        Matcher matcher = T_BOX_TOPIC.matcher(topic);
        if (!matcher.matches()) {
            return null;
        }

        String serial = matcher.group(T_BOX_TOPIC_SERIAL_INDEX);
        String vin = matcher.group(T_BOX_TOPIC_VIN_INDEX);
        return VehicleInfoDTO.builder().serial(serial).vin(vin).build();
    }

    /**
     * 是否系统主题-客户端上下线通知
     *
     * @param topic 系统主题
     * @return true/false
     */
    public static boolean isSysNotice(String topic) {
        return SYS_CONNECT_TOPIC.matcher(topic).matches();
    }

    public static SysNoticeDTO getSysNotice(String topic) {
        Matcher matcher = SYS_CONNECT_TOPIC.matcher(topic);
        if (!matcher.matches()) {
            return null;
        }

        String clientId = matcher.group(SYS_TOPIC_CLIENT_ID_INDEX);
        String type = matcher.group(SYS_TOPIC_TYPE_INDEX);
        return SysNoticeDTO.of(type, clientId);
    }

    /**
     * 通过ClientId获取上下线事件的VIN号
     *
     * @param clientId TBOX对应的客户端ID
     * @return VIN码
     */
    public static String getVinByClientId(String clientId) {
        String[] clientIdArray = clientId.split(StrPool.UNDERLINE);
        if (clientIdArray.length == T_BOX_CLIENT_ID_LENGTH) {
            return clientIdArray[1];
        }
        return null;
    }
}
