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
package com.flipkart.foxtrot.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;
import android.util.Base64;

import com.flipkart.foxtrot.client.Foxtrot;

/**
 * @author phaneesh
 */
public class FoxtrotUtil {

    private static String deviceId = null;

    private static TelephonyManager telephonyManager;
    private static ConnectivityManager cm;

    public static String id() {
        return Base64.encodeToString((deviceId() +"-" +System.nanoTime()).getBytes(), Base64.NO_WRAP);
    }

    public static String deviceId() {
        if(telephonyManager == null) {
            telephonyManager = (TelephonyManager) Foxtrot.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        }
        if(deviceId == null) {
            deviceId = telephonyManager.getDeviceId();
        }
        return deviceId;
    }

    public static String getNetworkType() {
        if(cm == null)
            cm = (ConnectivityManager) Foxtrot.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo().getTypeName();
    }

    public static String operatorName() {
        return telephonyManager.getNetworkOperatorName();
    }
}
