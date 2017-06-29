package org.typeunsafe;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.spi.ServiceDiscoveryBackend;

import io.vertx.ext.web.client.WebClient;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class HttpBackendService implements ServiceDiscoveryBackend {

  //private String httpBackendRecordsKey;
  private Integer httpBackendPort;
  private String httpBackendHost;
  //private String httpBackendAuth;
  // uries
  private String registerUri;
  private String removeUri;
  private String updateUri;
  private String recordsUri;

  private WebClient client;


  @Override
  public void init(Vertx vertx, JsonObject configuration) {
    client = WebClient.create(vertx);

    System.out.println(configuration.toString());

    
    //httpBackendRecordsKey = configuration.getString("key");
    httpBackendPort = configuration.getInteger("port");
    httpBackendHost  = configuration.getString("host");
    //httpBackendAuth  = configuration.getString("auth");
    registerUri  = configuration.getString("registerUri"); 
    removeUri  = configuration.getString("removeUri");
    updateUri  = configuration.getString("updateUri");  
    recordsUri  = configuration.getString("recordsUri"); 

    System.out.println("🤖 HttpBackendService initialized");

  }

  @Override
  public void store(Record record, Handler<AsyncResult<Record>> resultHandler) {
    
    System.out.println(record.toJson().toString());

    if (record.getRegistration() != null) {
      resultHandler.handle(Future.failedFuture("The record has already been registered"));
      return;
    }
    String uuid = UUID.randomUUID().toString();
    record.setRegistration(uuid);

    System.out.println(uuid);


    System.out.println("POST: " + this.httpBackendPort + " " + this.httpBackendHost + " " + this.registerUri);

    client.post(this.httpBackendPort, this.httpBackendHost, this.registerUri)
      .sendJsonObject(record.toJson(), ar -> {
        System.out.println("Hey Oh!!!");
        if (ar.succeeded()) {
          System.out.println("succeeded");
          resultHandler.handle(Future.succeededFuture(record));
        } else {
          System.out.println("not succeeded");
          resultHandler.handle(Future.failedFuture(ar.cause()));
        }
      });

  }

  @Override
  public void remove(Record record, Handler<AsyncResult<Record>> resultHandler) {
    Objects.requireNonNull(record.getRegistration(), "No registration id in the record");
    remove(record.getRegistration(), resultHandler);
  }

  @Override
  public void remove(String uuid, Handler<AsyncResult<Record>> resultHandler) {
    Objects.requireNonNull(uuid, "No registration id in the record");

    client.delete(this.httpBackendPort, this.httpBackendHost, this.removeUri + "/" + uuid)
      .send(ar -> {
        if (ar.succeeded()) {
          resultHandler.handle(Future.succeededFuture());
        } else {
          resultHandler.handle(Future.failedFuture(ar.cause()));
        }
      });

  }

  @Override
  public void update(Record record, Handler<AsyncResult<Void>> resultHandler) {
    Objects.requireNonNull(record.getRegistration(), "No registration id in the record");

    client.put(this.httpBackendPort, this.httpBackendHost, this.updateUri + "/" + record.getRegistration())
      .sendJsonObject(record.toJson(), ar -> {
        if (ar.succeeded()) {
          resultHandler.handle(Future.succeededFuture());
        } else {
          resultHandler.handle(Future.failedFuture(ar.cause()));
        }
      });
  }

  @Override
  public void getRecords(Handler<AsyncResult<List<Record>>> resultHandler) {
    client.get(this.httpBackendPort, this.httpBackendHost, this.recordsUri).send(resp -> {

      if(resp.succeeded()) {
        try {
          JsonArray entries = resp.result().bodyAsJsonArray();
          List<Record> records = entries.stream().map(item -> new Record(JsonObject.mapFrom(item)))
              .collect(Collectors.toList());
                    
          resultHandler.handle(Future.succeededFuture(records));
        } catch (Exception e) {
          e.printStackTrace();
        }

      } else {
        resultHandler.handle(Future.failedFuture(resp.cause()));
      }

    });

  }

  @Override
  public void getRecord(String uuid, Handler<AsyncResult<Record>> resultHandler) {
    // foo...
  }

}
