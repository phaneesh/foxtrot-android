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

package com.flipkart.foxtrot.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author phaneesh
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true)
@Builder
public class FoxtrotConfig {

    private String host;

    private int port;

    private String endpoint;

    private boolean secured;

    private boolean authEnabled = false;

    private boolean batchMode = true;

    private boolean autoRetry = true;

    private int batchSize = 10;

    private int batchInterval = 60;

    private int connectionTimeout = 5000;

    private int requestTimeout = 5000;
}
