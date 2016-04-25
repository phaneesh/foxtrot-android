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
package com.flipkart.foxtrot.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.flipkart.foxtrot.client.Foxtrot;
import com.flipkart.foxtrot.core.Document;
import com.flipkart.foxtrot.core.DocumentData;
import com.flipkart.foxtrot.queue.BatchDocument;

import java.util.ArrayList;
import java.util.List;

/**
 * @author phaneesh
 */
public class DocumentQueueDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DOCUMENT_QUEUE_TABLE_NAME = "foxtrot_document_queue";
    private static final String DATABASE_NAME = "foxtrot.db";
    private static final String DOCUMENT_QUEUE_TABLE_CREATE =
            "CREATE TABLE " + DOCUMENT_QUEUE_TABLE_NAME + " (id TEXT, doc_table TEXT, created INTEGER, data TEXT);";

    private Foxtrot foxtrot;

    public DocumentQueueDatabaseHelper(Foxtrot foxtrot, Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.foxtrot = foxtrot;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DOCUMENT_QUEUE_TABLE_CREATE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //DO nothing
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void insert(String table, BatchDocument document) {
        ContentValues values = new ContentValues();
        values.put("id", document.getDocument().getId());
        values.put("doc_table", table);
        values.put("created", document.getDocument().getTimestamp());
        values.put("data", foxtrot.getGson().toJson(document.getDocument().getData()));
        getWritableDatabase().insert(DOCUMENT_QUEUE_TABLE_NAME, null, values);
    }

    public void delete(String id) {
        getWritableDatabase().delete(DOCUMENT_QUEUE_TABLE_NAME, "id = ?", new String[]{id});
    }

    public void delete() {
        getWritableDatabase().delete(DOCUMENT_QUEUE_TABLE_NAME, null, null);
    }

    public long count() {
        return DatabaseUtils.queryNumEntries(getReadableDatabase(), DOCUMENT_QUEUE_TABLE_NAME);
    }

    public List<BatchDocument> documents() {
        Cursor cursor = getReadableDatabase().query(DOCUMENT_QUEUE_TABLE_NAME, null, null, null, null, null, null);
        List<BatchDocument> documentList = new ArrayList<BatchDocument>();
        if(cursor.moveToFirst()) {
            while(!cursor.isAfterLast()) {
                String id = cursor.getString(cursor.getColumnIndex("id"));
                long created = cursor.getLong(cursor.getColumnIndex("created"));
                DocumentData data = foxtrot.getGson().fromJson(cursor.getString(cursor.getColumnIndex("data")), DocumentData.class);
                documentList.add(BatchDocument.builder()
                        .table(cursor.getString(cursor.getColumnIndex("doc_table")))
                        .document(
                                Document.from()
                                        .data(data)
                                        .id(id)
                                        .timestamp(created)
                                        .build())
                        .build());
                cursor.moveToNext();
            }
        }
        cursor.close();
        return documentList;
    }

}
