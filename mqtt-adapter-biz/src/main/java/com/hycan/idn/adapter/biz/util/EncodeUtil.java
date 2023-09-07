package com.hycan.idn.adapter.biz.util;

import com.hycan.idn.adapter.biz.constant.RedisKeyConstants;
import com.hycan.idn.common.core.util.BytesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * MQTT协议编码工具类
 *
 * @author shichongying
 * @datetime 2023年 02月 24日 15:48
 */
@Slf4j
@Component
public class EncodeUtil {

    /** 协议版本号 && 会话序列号 最大计数到 65535 **/
    public static final int MAX_COUNTER_NUMBER = 65535;

    private final RedisTemplate<String, Object> redisTemplate;

    public EncodeUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 对下发消息进行编码后返回字节数组
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
     */
    public byte[] encodePayload(int appId, byte[] rawData) {
        int rowDataLength = rawData.length;

        byte[] header = encodeHeader(appId, rowDataLength);
        byte[] crc32 = Crc32Util.encodeCrc32(header, rawData);

        ByteBuffer buf = ByteBuffer.allocate(17 + rowDataLength + 4);
        buf.put(header);
        buf.put(rawData);
        buf.put(crc32);
        buf.flip();

        return buf.array();
    }

    /**
     * 对下发消息头部进行编码后返回字节数组 (Header总长度17字节)
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
     */
    private byte[] encodeHeader(int appId, int rawDataLength) {
        ByteBuffer buf = ByteBuffer.allocate(17);
        buf.put((byte) 1);
        buf.put(encodeTimestamp());
        buf.put(encodeRawDataLength(rawDataLength));
        buf.put((byte) (appId & 0xFF));
        buf.put(getNextSeqId());
        buf.put(BytesUtil.int2bytes2(0));
        return buf.array();
    }

    /**
     * 时间戳转码为8个字节数组
     */
    private byte[] encodeTimestamp() {
        return ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array();
    }

    /**
     * 表示正文内容字节数量(rawData长度)
     */
    private static byte[] encodeRawDataLength(int rawDataLength) {
        ByteBuffer buf = ByteBuffer.allocate(3);
        byte[] messageHeadLength = ByteBuffer.allocate(4).putInt(rawDataLength).array();
        byte[] crcByte = BytesUtil.cutBytes(1, 3, messageHeadLength);
        buf.put(crcByte);
        return buf.array();
    }

    /**
     * 下发到T-BOX会话序列ID的生成key
     * 用于区分响应返回值为哪一条请求所发起(从0开始计数到65535后重新循环为0）
     */
    private byte[] getNextSeqId() {
        RedisAtomicInteger redisAtomicInteger = new RedisAtomicInteger(
                RedisKeyConstants.T_BOX_SEQUENCE_ID, Objects.requireNonNull(redisTemplate.getConnectionFactory()));
        //先自增，再获取自增之后的值
        int code = redisAtomicInteger.incrementAndGet();
        if (code > MAX_COUNTER_NUMBER) {
            code = 0;
            redisAtomicInteger.set(code);
        }
        return BytesUtil.int2bytes2(code);
    }
}
