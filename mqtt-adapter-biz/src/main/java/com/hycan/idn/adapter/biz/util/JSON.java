package com.hycan.idn.adapter.biz.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * 基于 <strong>Jackson</strong> 的工具类
 *
 * @author Shadow
 * @see ObjectMapper
 * @since 1.2.1
 */
@Slf4j
public class JSON {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIME_FORMAT = "HH:mm:ss";

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        // LocalDateTime, LocalDate, LocalTime 三种类型序列化、反序列化配置
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(TIME_FORMAT)));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(TIME_FORMAT)));
        mapper.registerModule(javaTimeModule);

        // 时区
        mapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

        // 反序列化配置
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /**
     * 返回内部引用
     */
    public static ObjectMapper getObjectMapper() {
        return mapper;
    }


    /**
     * @see ObjectMapper#readTree(String)
     */
    public static JsonNode readTree(String content) {
        try {
            return mapper.readTree(content);
        } catch (JsonProcessingException e) {
            log.error("read value failed, exception : {}", e.getMessage());
            return null;
        }
    }

    /**
     * @see ObjectMapper#readTree(byte[])
     */
    public static JsonNode readTree(byte[] content) {
        try {
            return mapper.readTree(content);
        } catch (IOException e) {
            log.error("read value failed, exception : {}", e.getMessage());
            return null;
        }
    }

    /**
     * @see ObjectMapper#treeToValue(TreeNode, Class)
     */
    public static <T> T treeToValue(JsonNode jsonNode, Class<T> clazz) {
        try {
            return mapper.treeToValue(jsonNode, clazz);
        } catch (JsonProcessingException e) {
            log.error("tree to value failed, exception : {}", e.getMessage());
            return null;
        }
    }

    /**
     * {@link #treeToValue(JsonNode, Class)} 泛型版本
     */
    public static <T> T treeToValue(JsonNode jsonNode, TypeReference<T> typeRef) {
        try {
            return mapper.readValue(mapper.treeAsTokens(jsonNode), mapper.getTypeFactory().constructType(typeRef));
        } catch (IOException e) {
            log.error("tree to value failed, exception : {}", e.getMessage());
            return null;
        }
    }

    /**
     * @see ObjectMapper#createObjectNode()
     */
    public static ObjectNode createObjectNode() {
        return mapper.createObjectNode();
    }

    /**
     * @see ObjectMapper#createArrayNode()
     */
    public static ArrayNode createArrayNode() {
        return mapper.createArrayNode();
    }

    /**
     * @see ObjectMapper#readValue(String, Class)
     */
    public static <T> T readValue(String content, Class<T> clazz) {
        try {
            return mapper.readValue(content, clazz);
        } catch (JsonProcessingException e) {
            log.error("read value failed, exception : {}", e.getMessage());
            return null;
        }
    }

    /**
     * @see ObjectMapper#readValue(byte[], Class)
     */
    public static <T> T readValue(byte[] content, Class<T> clazz) {
        try {
            return mapper.readValue(content, clazz);
        } catch (IOException e) {
            log.error("read value failed, exception : {}", e.getMessage());
            return null;
        }
    }

    /**
     * @see ObjectMapper#readValue(String, TypeReference)
     */
    public static <T> T readValue(String content, TypeReference<T> valueTypeRef) {
        try {
            return mapper.readValue(content, valueTypeRef);
        } catch (IOException e) {
            log.error("read value failed, exception : {}", e.getMessage());
            return null;
        }
    }

    /**
     * @see ObjectMapper#readValue(byte[], TypeReference)
     */
    public static <T> T readValue(byte[] content, TypeReference<T> valueTypeRef) {
        try {
            return mapper.readValue(content, valueTypeRef);
        } catch (IOException e) {
            log.error("read value failed, exception : {}", e.getMessage());
            return null;
        }
    }

    /**
     * @see ObjectMapper#writeValueAsBytes(Object)
     */
    public static byte[] writeValueAsBytes(Object value) {
        try {
            return mapper.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            log.error("write value failed, exception : {}", e.getMessage());
            return null;
        }
    }

    /**
     * @see ObjectMapper#writeValueAsString(Object)
     */
    public static String writeValueAsString(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.error("write value failed, exception : {}", e.getMessage());
            return null;
        }
    }

    /**
     * 打印格式化 JSON 数据
     */
    public static String writeValueAsPrettyString(Object value) {
        if (ObjectUtils.isEmpty(value)) {
            return "";
        }

        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.error("write value failed, exception : {}", e.getMessage());
            return null;
        }
    }

    /**
     * json 中可能存在嵌套对象，比如
     * <pre>
     * {
     *     ”user": {
     *         "name":"lewin",
     *         "age":2
     *     }
     * }
     * </pre>
     * <p>
     * 但我们仅仅只是想将 user 字段反序列化为 {@link String} 类型
     */
    public static class JsonNode2StringDeserializer extends JsonDeserializer<String> {

        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            ObjectCodec codec = p.getCodec();
            TreeNode treeNode = codec.readTree(p);
            return treeNode.toString();
        }
    }
}
