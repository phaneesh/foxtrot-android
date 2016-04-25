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
package com.flipkart.foxtrot.queue;

import android.util.Log;

import com.flipkart.foxtrot.client.Foxtrot;
import com.flipkart.foxtrot.core.Document;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * @author phaneesh
 */
public class BatchSender extends TimerTask {

    private Foxtrot foxtrot;

    private BatchSender() {

    }

    public BatchSender(Foxtrot foxtrot) {
        this.foxtrot = foxtrot;
    }

    @Override
    public void run() {
        try {
            if(foxtrot.getBatchLock().tryLock(100, TimeUnit.MILLISECONDS)) {
                Map<String, List<Document>> batched  = new HashMap<String, List<Document>>();
                if(foxtrot.getDatabaseHelper().count() != 0) {
                    List<BatchDocument> documents = foxtrot.getDatabaseHelper().documents();
                    for(BatchDocument document : documents) {
                        if(!batched.containsKey(document.getTable()))
                            batched.put(document.getTable(), Collections.<Document>emptyList());
                        batched.get(document.getTable()).add(document.getDocument());
                    }
                }
                for(Map.Entry<String, List<Document>> entry : batched.entrySet()) {
                    foxtrot.send(entry.getKey(), entry.getValue(), null);
                    for(Document d : entry.getValue()) {
                        foxtrot.getDatabaseHelper().delete(d.getId());
                    }
                }
            }
        } catch (InterruptedException e) {
            Log.w("foxtrot", "Unable to run batch sender due to lock contention");
        } finally {
            foxtrot.getBatchLock().unlock();
        }
    }
}
