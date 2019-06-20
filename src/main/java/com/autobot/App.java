package com.autobot;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

public class App extends AbstractVerticle {

    @Override
    public void start(Future<Void> fut) {
        // Create a router object.
        Router router = Router.router(vertx);

        // Bind "/" to our hello message - so we are
        // still compatible with out tests.
        router.route("/").handler(rc -> {
            HttpServerResponse response = rc.response();
            response
                    .putHeader("content-type", "text/html")
                    .end("</pre><h1>Hello from my first Vert.x 3 app</h1><pre>");
        });

        // Serve static resources from the /assets directory
        router.route("/assets/*")
                .handler(StaticHandler.create("assets"));


        ConfigRetriever retriever = ConfigRetriever.create(vertx);
        retriever.getConfig(
                config -> {
                    if (config.failed()) {
                        fut.fail(config.cause());
                    } else {
                        // Create the HTTP server and pass the
                        // "accept" method to the request handler.
                        vertx.createHttpServer()
                                .requestHandler(router)
                                .listen(
                                        // Retrieve the port from the
                                        // configuration, default to 8080.
                                        config().getInteger("HTTP_PORT", 8080),
                                        result -> {
                                            if (result.succeeded()) {
                                                fut.complete();
                                            } else {
                                                fut.fail(result.cause());
                                            }
                                        });
                    }
                });
    }
}
