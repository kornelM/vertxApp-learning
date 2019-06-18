package com.autobot;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

public class App extends AbstractVerticle {

    @Override
    public void start(Future<Void> fut) {
        ConfigRetriver
        vertx.createHttpServer()
                .requestHandler(r ->
                        r.response().end("<h1>Hello Kornel from my first Vert.x application</h1>"))

                .listen(config().getInteger("HTTP_PORT", 8080),
                        result -> {
                            if (result.succeeded()) {
                                fut.complete();
                            } else {
                                fut.fail(result.cause());
                            }
                        });
    }
}
