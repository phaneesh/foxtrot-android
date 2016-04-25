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

package com.flipkart.foxtrot.client;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.flipkart.foxtrot.config.FoxtrotConfig;
import com.flipkart.foxtrot.core.Document;
import com.flipkart.foxtrot.database.DocumentQueueDatabaseHelper;
import com.flipkart.foxtrot.queue.BatchDocument;
import com.flipkart.foxtrot.queue.BatchSender;
import com.flipkart.foxtrot.util.SimpleIdProvider;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import lombok.Builder;
import lombok.Getter;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author phaneesh
 */
public class Foxtrot {

    private static Context context;

    private OkHttpClient okHttpClient;

    @Getter
    private FoxtrotConfig config;

    private String baseUrl;

    private Retrofit retrofit;

    @Getter
    private Gson gson;

    private FoxtrotHttpClient foxtrotHttpClient;

    private AuthenticationProvider authenticationProvider = null;

    private static DeviceIdProvider deviceIdProvider;

    @Getter
    private ReentrantLock batchLock = new ReentrantLock();

    private Timer timer;

    @Getter
    private DocumentQueueDatabaseHelper databaseHelper;

    //Hide the default constructor
    private Foxtrot() {

    }

    public static Context getContext() {
        return context;
    }

    public static DeviceIdProvider getDeviceIdProvider() {
        return deviceIdProvider;
    }

    @Builder(builderMethodName = "defaultClient")
    public Foxtrot(final FoxtrotConfig config, Context applicationContext) {
        context = applicationContext;
        this.config = config;
        this.deviceIdProvider = new SimpleIdProvider();
        this.okHttpClient = new OkHttpClient.Builder()
                .connectionPool(
                new ConnectionPool(1, 3000, TimeUnit.SECONDS)
            )
                .connectTimeout(config.getConnectionTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(config.getRequestTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(config.getRequestTimeout(), TimeUnit.MILLISECONDS)
            .build();
        setupBaseUrl();
        this.gson = new GsonBuilder().create();
        setupClient(gson);
    }

    @SuppressLint("DefaultLocale")
    private void setupBaseUrl() {
        baseUrl = String.format("%s://%s:%d/%s", config.isSecured() ? "https" : "http", config.getHost(), config.getPort(), config.getEndpoint());
    }

    private void setupClient(Gson gson) {
        retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        foxtrotHttpClient = retrofit.create(FoxtrotHttpClient.class);
        if(config.isBatchMode()) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new BatchSender(this), 10000, config.getBatchInterval());
        }
        databaseHelper = new DocumentQueueDatabaseHelper(this, context);
    }

    @Builder(builderMethodName = "from")
    public Foxtrot(OkHttpClient okHttpClient, FoxtrotConfig config, Gson gson, AuthenticationProvider authenticationProvider, DeviceIdProvider deviceIdProvider, Context applicationContext) {
        context = applicationContext;
        this.config = config;
        this.okHttpClient = okHttpClient;
        this.authenticationProvider = authenticationProvider;
        this.deviceIdProvider = deviceIdProvider;
        this.gson = gson;
        setupBaseUrl();
        setupClient(gson);
    }

    public void send(String table, Document document) {
        send(table, Collections.singletonList(document), null);
    }


    public void send(String table, Document document, final Callback callback) {
        send(table, Collections.singletonList(document), callback);
    }

    public void send(String table, List<Document> documents) {
        send(table, documents, null);
    }

    public void send(final String table, final List<Document> documents, final Callback callback) {
        Call call = authenticationProvider ==  null ?
                foxtrotHttpClient.send(table, documents) :
                foxtrotHttpClient.send(String.format("%s %s", authenticationProvider.prefix(),
                        authenticationProvider. token()), table,
                        documents);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                if(response.isSuccessful()) {
                    Log.i("foxtrot", "Documents sent successfully: " +response.body());
                } else {
                    if(config.isAutoRetry()) {
                        Log.w("foxtrot", "Documents queued since send was unsuccessful: " +response.body());
                        queue(table, documents);
                    } else {
                        Log.e("foxtrot", "Document cannot be sent:" +response.body());
                    }
                }
                if (callback != null) {
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                if(config.isAutoRetry()) {
                    Log.w("foxtrot", "Documents queued since send was unsuccessful: " +t.getMessage());
                    queue(table, documents);
                } else {
                    Log.e("foxtrot", "Documents cannot be sent:", t);
                }
                if (callback != null) {
                    callback.onFailure(call, t);
                }
            }
        });
    }

    public void queue(String table, Document document) {
        databaseHelper.insert(table, BatchDocument.builder()
            .table(table)
            .document(document).build());
        if(databaseHelper.count() >= config.getBatchSize()) {
            sendBatch();
        }
    }

    public void queue(String table, List<Document> documents) {
        for(Document document : documents) {
            databaseHelper.insert(table, BatchDocument.builder()
                    .table(table)
                    .document(document).build());
        }
        if(databaseHelper.count() >= config.getBatchSize()) {
            sendBatch();
        }
    }

    public void stop(final Callback<Response> callback) {
        if(config.isBatchMode()) {
            timer.cancel();
        }
        Map<String, List<Document>> batched  = new HashMap<String, List<Document>>();
        if(databaseHelper.count() != 0) {
            List<BatchDocument> documents = databaseHelper.documents();
            for(BatchDocument document : documents) {
                if(!batched.containsKey(document.getTable()))
                    batched.put(document.getTable(), Collections.<Document>emptyList());
                batched.get(document.getTable()).add(document.getDocument());
            }
        }
        for(Map.Entry<String, List<Document>> entry : batched.entrySet()) {
            send(entry.getKey(), entry.getValue(), callback);
            for(Document d : entry.getValue()) {
                databaseHelper.delete(d.getId());
            }
        }
        databaseHelper.delete();
        databaseHelper.close();
    }

    private void sendBatch() {
        try {
            if(batchLock.tryLock(1000, TimeUnit.MILLISECONDS)) {
                Map<String, List<Document>> batched  = new HashMap<String, List<Document>>();
                List<BatchDocument> documents = databaseHelper.documents();
                for(BatchDocument document : documents) {
                    if(!batched.containsKey(document.getTable()))
                        batched.put(document.getTable(), Collections.<Document>emptyList());
                    batched.get(document.getTable()).add(document.getDocument());
                }
                for(Map.Entry<String, List<Document>> entry : batched.entrySet()) {
                    send(entry.getKey(), entry.getValue(), null);
                    for(Document d : entry.getValue()) {
                        databaseHelper.delete(d.getId());
                    }
                }
            }
        } catch (Exception e) {
            Log.e("foxtrot", "Cannot send batched documents", e);
        } finally {
            batchLock.unlock();
        }
    }
}
