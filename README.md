# Foxtrot Android Client

Allows sending events to Foxtrot from android a breeze.

## Features:
* Sync & Async events (With & without callback)
* Batched mode
* Persistent document queue for minimizing message loss

## Dependencies
* retrofit
* okhttp3

## Usage

Add the following repository to your build.gradle

```
repositories {
    maven {
        url 'https://dl.bintray.com/phaneesh/maven/'
    }
}
```

Add the dependency

```
compile 'com.flipkart.foxtrot:foxtrot-android:1.0'
```

### Simple send
```java
FoxtrotConfig config = FoxtrotConfig.builder()
    .host("myflipcast.com")
    .port(8080)
    .endpoint("/mobile/api")
    .secured(false)
    .authEnabled(false)
    .batchMode(true)
    .batchSize(5)
    .batchInterval(30)
    .build();
Foxtrot foxtrot = Foxtrot.defaultClient(config)
    .config(config)
    .context(context)
    .build();

MyData myData = new MyData(true, 200);
Document document = Document.fromData()
        .eventName("myevent")
        .data(myData)
        .build();

foxtrot.send(document);

foxtrot.stop();
```
### Bulk send
```java
MyData myData1 = new MyData(true, 200);
MyData myData2 = new MyData(true, 300);
MyData myData3 = new MyData(true, 150);
Document document1 = Document.fromData()
        .eventName("myevent")
        .data(myData1)
        .build();
Document document2 = Document.fromData()
        .eventName("myevent")
        .data(myData2)
        .build();
Document document3 = Document.fromData()
        .eventName("myevent")
        .data(myData3)
        .build();
List<Document> documents = new ArrayList<Document>();
documents.add(document1);
documents.add(document2);
documents.add(document3);
foxtrot.send(documents);
```

### Queue message
```java
MyData myData = new MyData(true, 200);
Document document = Document.fromData()
        .eventName("myevent")
        .data(myData)
        .build();

foxtrot.queue(document);
```

### Queue messages
```java
MyData myData1 = new MyData(true, 200);
MyData myData2 = new MyData(true, 300);
MyData myData3 = new MyData(true, 150);
Document document1 = Document.fromData()
        .eventName("myevent")
        .data(myData1)
        .build();
Document document2 = Document.fromData()
        .eventName("myevent")
        .data(myData2)
        .build();
Document document3 = Document.fromData()
        .eventName("myevent")
        .data(myData3)
        .build();
List<Document> documents = new ArrayList<Document>();
documents.add(document1);
documents.add(document2);
documents.add(document3);
foxtrot.queue(documents);
```

### Send events with callback
```java
Callback callback = new Callback() {
                                @Override
                                public void onResponse(Call call, Response response) {
                                    //Success
                                }
                    
                                @Override
                                public void onFailure(Call call, Throwable t) {
                                    //Handle failure
                                }
                            };
MyData myData = new MyData(true, 200);
Document document = Document.fromData()
        .eventName("myevent")
        .data(myData)
        .build();

foxtrot.send(document, callback);
``` 

### Handling call failures
When send fails; the events are automatically queued up which will be sent later.
This behaviour can be disabled by changing autoRetry to false in FoxtrotConfig. 

LICENSE
-------

Copyright 2016 Phaneesh Nagaraja <phaneesh.n@gmail.com>.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
