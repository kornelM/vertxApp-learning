package com.autobot;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.ServerSocket;

@RunWith(VertxUnitRunner.class)
public class AppTest {
    private Vertx vertx;
    private int port = 8081;

    @Before
    public void setUp(TestContext context) throws IOException {
        vertx = Vertx.vertx();

        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();

        DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject().put("HTTP_PORT", port));
        vertx.deployVerticle(App.class.getName(), context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testMyApplication(TestContext context) {
        final WebClient webClient = WebClient.create(this.vertx);

        webClient
                .get(port, "localhost", "/")
                .send(asyncResult -> {
                    if (asyncResult.succeeded()) {
                        // Obtain response
                        HttpResponse<Buffer> response = asyncResult.result();
                        context.assertTrue(response.bodyAsString().contains("Hello"));
                        System.out.println("Received response with status code" + response.statusCode());
                    } else {
                        System.out.println("Something went wrong " + asyncResult.cause().getMessage());
                    }
                });
    }
}
