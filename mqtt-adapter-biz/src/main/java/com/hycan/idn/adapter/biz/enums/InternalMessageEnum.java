/*
 * Copyright 2002-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hycan.idn.adapter.biz.enums;

import com.hycan.idn.adapter.biz.constant.RedisChannelConstants;

/**
 * 集群消息枚举
 *
 * @author shichongying
 * @datetime 2023年 03月 01日 10:18
 */
public enum InternalMessageEnum {

    /** 连接状态消息 */
    CONNECT_STATUS(1, RedisChannelConstants.CONNECT_STATUS);

    private final int type;

    /**
     * redis pub/sub channel
     */
    private final String channel;

    InternalMessageEnum(int type, String channel) {
        this.type = type;
        this.channel = channel;
    }

    public String getChannel() {
        return channel;
    }

    public int getType() {
        return type;
    }
}