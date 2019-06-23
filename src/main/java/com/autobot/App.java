package com.autobot;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.LinkedHashMap;
import java.util.Map;

public class App extends AbstractVerticle {
    // Store our readingList
    private Map<Integer, Article> readingList = new LinkedHashMap<>();

    @Override
    public void start(Future<Void> fut) {
        // Create a router object.
        Router router = Router.router(vertx);

        createSomeData();

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
        router.get("/api/articles").handler(this::getAll);

        router.route("/api/articles*").handler(BodyHandler.create());
        router.post("/api/articles").handler(this::addOne);
        router.delete("/api/articles/:id").handler(this::deleteOne);

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

    // Create a readingList
    private void createSomeData() {
        Article article1 = new Article(
                "Tesla, Inc.",
                "https://en.wikipedia.org/wiki/Tesla,_Inc.");
        readingList.put(article1.getId(), article1);
        Article article2 = new Article(
                "Reactive Manifesto",
                "https://www.reactivemanifesto.org/");
        readingList.put(article2.getId(), article2);
    }

    private void getAll(RoutingContext rc) {
        rc.response()
                .putHeader("content-type",
                        "application/json; charset=utf-8")
                .end(Json.encodePrettily(readingList.values()));
    }

    private void addOne(RoutingContext rc) {
        Article article = rc.getBodyAsJson().mapTo(Article.class);
        readingList.put(article.getId(), article);
        rc.response()
                .setStatusCode(201)
                .putHeader("content-type",
                        "application/json; charset=utf-8")
                .end(Json.encodePrettily(article));
    }

    private void deleteOne(RoutingContext rc) {
        String id = rc.request().getParam("id");
        try {
            Integer idAsInteger = Integer.valueOf(id);
            readingList.remove(idAsInteger);
            rc.response().setStatusCode(204).end();
        } catch (NumberFormatException e) {
            rc.response().setStatusCode(400).end();
        }
    }

}
