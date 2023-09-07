package com.hycan.idn.adapter.biz.util;

import com.hycan.idn.adapter.biz.pojo.dto.MessageHeadDTO;
import com.hycan.idn.common.core.dto.HeartConfig;
import com.hycan.idn.common.core.util.BytesUtil;

import java.nio.ByteBuffer;

/**
 * MQTT协议解码工具类
 *
 * @author shichongying
 * @datetime 2023年 02月 24日 15:48
 */
public class DecodeUtil {

    /**
     * 根据字节数组解析还原成AcpMessageHeader
     * <p/>
     * <h2>&nbsp;&nbsp;Header说明</h2>
     * <pre>
     * ---------------------------------------------------------------------------------------------------
     * |  数据格式  | 长度 | 数据类型 |   定义说明                                                           |
     * ------------+-----+---------+----------------------------------------------------------------------
     * | 协议版本号  |  1  |  Short | 表示正文内容是采用哪种编/解码协议版本，（从 1 开始计数到 65535）初始版本为 1   |
     * ------------+-----+---------+----------------------------------------------------------------------
     * |  时间戳    |  8  |  Long   | 该时间戳为毫秒级，采用 Unix 时间戳                                      |
     * ------------+-----+---------+----------------------------------------------------------------------
     * | 正文长度   |  3  |   Int   | 表示正文内容字节数量                                                   |
     * ------------+-----+---------+----------------------------------------------------------------------
     * |  APPID    |  1  |  Short  | 15                                                                  |
     * ------------+-----+---------+----------------------------------------------------------------------
     * | 会话序列ID |  2  |  Short  | 用于区分响应返回值为哪一条请求所发起（从 0 开始计数，到 65535 后重新循环为 0） |
     * ------------+-----+---------+----------------------------------------------------------------------
     * | 保留字段   |  2  |  String | Set to 0                                                            |
     * ---------------------------------------------------------------------------------------------------
     * </pre>
     *
     * @param payload
     * @return
     */
    public static MessageHeadDTO header(byte[] payload) {
        MessageHeadDTO header = new MessageHeadDTO();
        header.setVersion(BytesUtil.getIntFromBytes(0, payload));
        header.setTimestamp(BytesUtil.bytesToLong(BytesUtil.cutBytes(1, 8, payload)));
        header.setLength(headerLength(BytesUtil.cutBytes(9, 3, payload)));
        header.setAppId(BytesUtil.getIntFromBytes(12, payload));
        header.setSeqId((BytesUtil.parseBytesToInt(BytesUtil.getWord(13, payload))));
        header.setReserved(BytesUtil.parseBytesToInt(BytesUtil.getWord(15, payload)));

        return header;
    }

    /**
     * 字节还原MessageHeader长度
     *
     * @param headerLengthBytes
     * @return
     */
    private static int headerLength(byte[] headerLengthBytes) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.put((byte) 0);
        buffer.put(headerLengthBytes, 0, headerLengthBytes.length);
        buffer.flip();
        return buffer.getInt();
    }

    /**
     * 从上报报文获取原始十六进制的payload
     * <p/>
     * <h2>&nbsp;&nbsp;Payload说明</h2>
     * <pre>
     * -------------------------------------------------------------
     * |  数据格式  | 长度 |          描述及说明                       |
     * ------------+-----+------------------------------------------
     * |  Header   |  17 | 消息头                                   |
     * ------------+-----+------------------------------------------
     * |  RawData  |  N  | 正文内容                                 |
     * ------------+-----+------------------------------------------
     * |  CRC32    |  4  | 使用 CRC32(Header+RawData) 校验数据完整性  |
     * -------------------------------------------------------------
     * </pre>
     *
     * @param payload 原始报文
     * @return rawData
     */
    public static byte[] rawData(byte[] payload) {
        return BytesUtil.cutBytes(17, (payload.length - 17 - 4), payload);
    }

    /**
     * 解析连接状态数据
     * <p/>
     * <h2>&nbsp;&nbsp;Payload说明</h2>
     * <pre>
     * ------------------------------------------------------------------
     * |     数据格式     | 长度 |          描述及说明                      |
     * -----------------+-----+------------------------------------------
     * |      命令ID     |  2  | Set to 1                                |
     * -----------------+-----+------------------------------------------
     * |     参数个数     |  1  | Set to 2                                |
     * -----------------+-----+------------------------------------------
     * |  参数1：心跳时长  |  1  |  Set to 1                               |
     * -----------------+-----+------------------------------------------
     * |     参数类型     |  1  |  Set to 2：uint16                       |
     * -----------------+-----+------------------------------------------
     * |     参数值      |  2  |  心跳时间(单位：秒)                        |
     * -----------------+-----+------------------------------------------
     * |  参数2：TBOX状态 |  1  |  Set to 2                               |
     * -----------------+-----+------------------------------------------
     * |     参数类型     |  1  |  Set to 1：uint8                        |
     * -----------------+-----+------------------------------------------
     * |     参数值      |  1  |  1：工作状态、2：低功耗                     |
     * ------------------------------------------------------------------
     * </pre>
     *
     * @param payload 原始报文
     * @return 心跳配置
     */
    public static HeartConfig decodeConnectStatus(byte[] payload) {
        HeartConfig config = new HeartConfig();
        // 获取心跳时长
        byte[] heartValueBytes = BytesUtil.cutBytes(22, 2, payload);
        config.setHeartValue(BytesUtil.checkBytesToInt(heartValueBytes));
        // 获取TBOX状态
        config.setTBoxStatus(BytesUtil.getByte(26, payload));
        return config;
    }
}
