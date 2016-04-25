/*
 * Copyright 2016 Phaneesh Nagaraja <phaneesh.n@gmail.com>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.flipkart.foxtrot.core;

import com.flipkart.foxtrot.util.FoxtrotUtil;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author phaneesh
 */
@Data
@NoArgsConstructor
public class Document {

    private String id;

    private long timestamp;

    private DocumentData data;

    @Builder(builderMethodName = "fromData")
    public Document(final String eventName, final Object data) {
        this.id = FoxtrotUtil.id();
        this.timestamp = System.currentTimeMillis();
        this.data = DocumentData.builder().eventName(eventName).data(data).build();
    }

    @Builder(builderMethodName = "fromIdAndData")
    public Document(final String eventName, final String id, final Object data) {
        this.id = id;
        this.timestamp = System.currentTimeMillis();
        this.data = DocumentData.builder().eventName(eventName).data(data).build();
    }

    @Builder(builderMethodName = "from")
    public Document(final String eventName, final String id, final long timestamp, final Object data) {
        this.id = id;
        this.timestamp = timestamp;
        this.data = DocumentData.builder().eventName(eventName).data(data).build();
    }
}
