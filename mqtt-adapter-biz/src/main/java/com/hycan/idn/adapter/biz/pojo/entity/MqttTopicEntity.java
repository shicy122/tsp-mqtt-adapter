package com.hycan.idn.adapter.biz.pojo.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * APP_ID与TBox的主题映射关系实体类
 *
 * @author shichongying
 * @datetime 2023年 02月 27日 13:50
 */
@Data
@Builder
@Document(collection = "tsp_adpt_mqtt_topic")
public class MqttTopicEntity {

    public static final String FIELD_APP_ID = "app_id";

    public static final String FIELD_DIRECTION = "direction";

    public static final String VALUE_DOWN_MSG = "down";

    //@formatter:off

    /** 客户ID */
    @Field(name = "app_id")
    private int appId;

    /**
     * <ol>
     *     <li>0 -> at most once</li>
     *     <li>1 -> at least once</li>
     *     <li>2 -> exactly once</li>
     * </ol>
     */
    @Field(name = "qos")
    private Integer qos;

    /**  主题  **/
    @Field(name = "topic")
    private String topic;

    /** 数据传输类型 up-上行, down-下行 */
    @Field(name = "direction")
    private String direction;

    /** 备注 */
    @Field(name = "remark")
    private String remark;
}
