package com.hycan.idn.adapter.biz.util;

import com.hycan.idn.common.core.util.BytesUtil;
import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

/**
 * CRC32工具类
 *
 * @author shichongying
 * @datetime 2023年 02月 26日 17:58
 */
public class Crc32Util {

    /**
     * CRC32消息加密，防止消息传输中数据出错，保证数据完整性
     */
    public static byte[] encodeCrc32(byte[] header, byte[] rowData) {
        ByteBuffer headerAndRawData = ByteBuffer.allocate(17 + rowData.length);
        headerAndRawData.put(header);
        headerAndRawData.put(rowData);

        CRC32 crc32 = new CRC32();
        crc32.update(headerAndRawData.array());
        long crc32Value = crc32.getValue();

        // 截取4个字节
        byte[] crc32BytesValue = ByteBuffer.allocate(8).putLong(crc32Value).array();
        byte[] crc32CutBytesValue = BytesUtil.cutBytes(4, 4, crc32BytesValue);

        ByteBuffer crc32ByteBuf = ByteBuffer.allocate(4).put(crc32CutBytesValue);
        return crc32ByteBuf.array();
    }

    /**
     * 校验上报数据CRC32(防止消息传输中数据出错,保证数据完整性)
     * @param payloadBytes
     * @return
     */
    public static boolean checkPayload(byte[] payloadBytes) {
        boolean checkResult;
        try {
            byte[] rawDataHead = BytesUtil.cutBytes(0, 17, payloadBytes);
            byte[] rawData = BytesUtil.cutBytes(17, payloadBytes.length - 4 - 17, payloadBytes);
            byte[] rawDataCrc32 = BytesUtil.cutBytes(payloadBytes.length - 4, 4,
                    payloadBytes);
            String rawDataCrc32HexStr = BytesUtil.bytesToHexString(rawDataCrc32);
            String localEncodeCrc32HexStr = BytesUtil.bytesToHexString(encodeCrc32(rawDataHead, rawData));
            checkResult = StringUtils.equalsIgnoreCase(rawDataCrc32HexStr, localEncodeCrc32HexStr);
        } catch (Exception e) {
            checkResult = false;
        }
        return checkResult;
    }
}
