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

  public HttpBackendServiceTest(String name) {
    super(name);
  }

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
      "000",
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
    assertEquals("000", reference.get().getName());
  }
}

