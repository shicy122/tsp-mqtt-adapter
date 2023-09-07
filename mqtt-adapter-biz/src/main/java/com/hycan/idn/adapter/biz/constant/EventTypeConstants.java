package com.hycan.idn.adapter.biz.constant;

/**
 * Spring事件类型常量
 *
 * @author shichongying
 * @datetime 2023年 03月 08日 9:30
 */
public interface EventTypeConstants {

    /** 连接状态事件 */
    String CONNECT_STATUS = "CONNECT_STATUS";

    /** 车辆国标登入/登出事件 */
    String GB_LOG_IO = "GB_LOG_IO";

    /** 心跳检测事件 */
    String HEARTBEAT = "HEARTBEAT";

    /** 其他APP_ID对应的事件 */
    String BIZ_MSG = "BIZ_MSG";
}
