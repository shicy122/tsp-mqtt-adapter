package com.hycan.idn.adapter.biz.mqtt.event.listener;

import com.hycan.idn.adapter.biz.config.AdapterConfig;
import com.hycan.idn.adapter.biz.constant.GbBizTypeConstants;
import com.hycan.idn.adapter.biz.mqtt.event.UpMessageEvent;
import com.hycan.idn.adapter.biz.service.IMqttBizMessageService;
import com.hycan.idn.adapter.biz.util.DecodeUtil;
import com.hycan.idn.adapter.biz.util.EncodeUtil;
import com.hycan.idn.adapter.biz.util.ThreadPoolUtil;
import com.hycan.idn.common.core.util.BytesUtil;
import com.hycan.idn.tsp.engine.command.AcpMessageHeader;
import com.hycan.idn.tsp.engine.command.AcpRawMessage;
import com.hycan.idn.tsp.engine.command.acp.entity.VehicleDescription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

/**
 * 国标登入/登出监听器
 *
 * @author shichongying
 * @datetime 2023年 02月 24日 10:27
 */
@Slf4j
@Component
public class GbLogIoListener {

    private final EncodeUtil encodeUtil;

    private final IMqttBizMessageService messageService;

    private final boolean enableLog;

    public GbLogIoListener(EncodeUtil encodeUtil, IMqttBizMessageService messageService, AdapterConfig config) {
        this.encodeUtil = encodeUtil;
        this.messageService = messageService;
        this.enableLog = config.getLog().getEnableGb();
    }

    /**
     * 国标车辆登入/登出应答ack
     */
    @Async(ThreadPoolUtil.OTHER_MSG_THREAD)
    @EventListener(condition = "T(com.hycan.idn.adapter.biz.constant.EventTypeConstants).GB_LOG_IO.equals(#event.type)")
    public void handleEvent(UpMessageEvent event) throws Exception {
        byte[] rawData = DecodeUtil.rawData(event.getPayload());
        if (!validType(rawData)) {
            return;
        }

        if (enableLog) {
            log.info("国标登入/登出: VIN码=[{}], APP_ID=[{}], RawData=[{}]",
                    event.getVin(), event.getAppId(), BytesUtil.bytesToHexString(rawData));
        }

        String vin = event.getVin();
        int appId = event.getAppId();

        AcpRawMessage ackRwqMessage = buildAcpRawMessage(vin, appId, event.getPayload());
        messageService.sendUpMessage(vin, appId, ackRwqMessage, false);

        // 组装mqtt报文
        byte[] downGbLoginInOutBytes = buildAckRawData(rawData).array();

        byte[] ackPayload = encodeUtil.encodePayload(event.getAppId(), downGbLoginInOutBytes);

        messageService.sendDownMessage(appId, event.getSerial(), vin, ackPayload);
    }

    /**
     * 校验命令表示是否为 国标登入 / 国标登出
     */
    private boolean validType(byte[] rawData) {
        byte[] type = BytesUtil.cutBytes(2, 1, rawData);
        return GbBizTypeConstants.NATION_LOGIN == type[0] || GbBizTypeConstants.NATION_LOGOUT == type[0];
    }

    /**
     * 组装的上行报文
     *
     * @param vin     VIN码
     * @param appId   协议标识
     * @param payload 协议数据
     * @return 上行报文
     */
    private AcpRawMessage buildAcpRawMessage(String vin, int appId, byte[] payload) {
        AcpRawMessage acpMessage = new AcpRawMessage();
        VehicleDescription vehicleDescription = new VehicleDescription();
        vehicleDescription.setVin(vin);
        acpMessage.setVehicleDescription(vehicleDescription);
        AcpMessageHeader acpMessageHeader = new AcpMessageHeader();
        acpMessageHeader.setApplicationID(appId);
        acpMessage.setHeader(acpMessageHeader);
        acpMessage.setPayload(payload);
        return acpMessage;
    }

    /**
     * 构造应答数据(总长度31)
     * <p/>
     * <h2>&nbsp;&nbsp;Header 国标登入/登出 应答数据结构</h2>
     * <pre>
     * -----------------------------------------------------------------------------------
     * | 长度 |  描述及要求                                                                |
     * ------+----------------------------------------------------------------------------
     * |  2  |  固定为 ASCII 字符‘##’，用“0x23,0x23”表示                                    |
     * ------+----------------------------------------------------------------------------
     * |  1  |  命令标识                                                                  |
     * ------+----------------------------------------------------------------------------
     * |  1  |  应答标志(01：成功 接收到的信息正确、02：错误 设置未成功、03：VIN 重复 VIN 重复错误) |
     * ------+----------------------------------------------------------------------------
     * |  17 |  车辆 VIN 码                                                               |
     * ------+----------------------------------------------------------------------------
     * |  1  |  0x01 数据不加密                                                            |
     * ------+----------------------------------------------------------------------------
     * |  2  |  数据单元长度(时间)                                                          |
     * ------+----------------------------------------------------------------------------
     * |  6  |  数据单元(时间：年月日时分秒)                                                 |
     * ------+----------------------------------------------------------------------------
     * |  1  |  校验码                                                                    |
     * ------+----------------------------------------------------------------------------
     * </pre>
     */
    private ByteBuffer buildAckRawData(byte[] rawData) {
        ByteBuffer buf = ByteBuffer.allocate(30);
        buf.put(BytesUtil.cutBytes(0, 2, rawData));
        buf.put(BytesUtil.cutBytes(2, 1, rawData));
        buf.put(BytesUtil.bit2Bytes(String.valueOf(1), 1));
        buf.put(BytesUtil.cutBytes(4, 17, rawData));
        buf.put(BytesUtil.bit2Bytes(String.valueOf(1), 1));
        buf.put(BytesUtil.int2bytes2(6));
        buf.put(BytesUtil.cutBytes(24, 6, rawData));
        buf.flip();

        // 计算bcc校验码
        byte[] ackRawData = buf.array();
        byte[] bccCode = calculationBccCode(ackRawData);
        ByteBuffer allocate = ByteBuffer.allocate(31);
        allocate.put(ackRawData);
        allocate.put(bccCode);
        allocate.flip();
        return allocate;
    }

    /**
     * BCC校验(异或校验)
     */
    private byte[] calculationBccCode(byte[] data) {
        byte[] bcc = new byte[1];

        for (byte datum : data) {
            bcc[0] ^= datum;
        }

        String hex = Integer.toHexString(bcc[0] & 255);
        if (hex.length() == 1) {
            hex = '0' + hex;
        }

        String ret = hex.toUpperCase();
        return BytesUtil.toStringHex(ret);
    }
}
