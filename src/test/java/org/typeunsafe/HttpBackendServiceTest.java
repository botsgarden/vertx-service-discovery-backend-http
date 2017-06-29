package org.typeunsafe;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.Record;
import org.junit.Before;
import org.junit.Test;
import junit.framework.TestCase;
import java.util.concurrent.atomic.AtomicReference;
import static com.jayway.awaitility.Awaitility.await;

/*
See https://github.com/vert-x3/vertx-service-discovery/blob/master/vertx-service-discovery/src/test/java/io/vertx/servicediscovery/spi/ServiceDiscoveryBackendTest.java
*/

public class HttpBackendServiceTest extends TestCase {
  Vertx vertx;
  HttpBackendService httpBackend;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();

    httpBackend = new HttpBackendService();
    httpBackend.init(Vertx.vertx(), new JsonObject()
      .put("host", "localhost")
      .put("port", 8080)
      .put("registerUri", "/register")
      .put("removeUri", "/remove")
      .put("updateUri", "/update")
      .put("recordsUri", "/records"));
  }

  @Test
  public void testServiceInsertion() throws Exception {
    // create the microservice record
    Record record = HttpEndpoint.createRecord(
      "001",
      "127.0.0.1",
      9090,
      "/api"
    );
    AtomicReference<Record> reference = new AtomicReference<>();
    httpBackend.store(record, res -> {
      if(!res.succeeded()) {
        res.cause().printStackTrace();
      }
      reference.set(res.result());
    });    

    await().until(() -> reference.get() != null);
    System.out.println(reference.get().getName());
    System.out.println(reference.get().getRegistration());
    assertEquals("001", reference.get().getName());
  }

  @Test
  public void testServiceDelete() throws Exception {
    // create the microservice record
    Record record = HttpEndpoint.createRecord(
      "002",
      "127.0.0.1",
      9091,
      "/api"
    );
    AtomicReference<Record> reference = new AtomicReference<>();
    httpBackend.store(record, res -> {
      if(!res.succeeded()) {
        res.cause().printStackTrace();
      }
      reference.set(res.result());
    });    

    await().until(() -> reference.get() != null);
    System.out.println(reference.get().getName());
    System.out.println(reference.get().getRegistration());

    AtomicReference<Record> referenceForDelete = new AtomicReference<>();

    httpBackend.remove(reference.get().getRegistration(), res -> {
      if(!res.succeeded()) {
        res.cause().printStackTrace();
      }
      referenceForDelete.set(res.result());
    });
    await().until(() -> referenceForDelete.get() != null);
    //assertEquals("002", referenceForDelete.get().getName());

    System.out.println(referenceForDelete.get().getRegistration());
  }


}

