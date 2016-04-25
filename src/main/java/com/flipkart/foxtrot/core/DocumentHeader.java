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

import android.os.Build;

import com.flipkart.foxtrot.client.Foxtrot;
import com.flipkart.foxtrot.util.FoxtrotUtil;

import lombok.Builder;
import lombok.Data;

/**
 * @author phaneesh
 */
@Data
public class DocumentHeader {

    private String brand = Build.BRAND;

    private String model = Build.MODEL;

    private String manufacturer = Build.MANUFACTURER;

    private String version = Build.VERSION.RELEASE;

    private String os = Build.VERSION.CODENAME;

    private String networkType = FoxtrotUtil.getNetworkType();

    private String operator = FoxtrotUtil.operatorName();

    private String deviceId = Foxtrot.getDeviceIdProvider().id();

    private String eventName;

    @Builder
    public DocumentHeader(String eventName) {
        this.eventName = eventName;
    }
}
