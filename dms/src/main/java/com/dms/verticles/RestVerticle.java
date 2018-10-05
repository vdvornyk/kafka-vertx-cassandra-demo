package com.dms.verticles;

import com.dms.entity.IncommingMessage;
import com.dms.service.IncommingMesageService;
import io.vertx.core.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import rx.Subscriber;

import java.util.LinkedList;
import java.util.List;

public class RestVerticle extends AbstractVerticle {

    private Logger logger = LoggerFactory.getLogger(RestVerticle.class);

    private static final Integer HTTP_PORT = 8080;

    public IncommingMesageService mesageService;

    public RestVerticle(IncommingMesageService mesageService) {
        this.mesageService = mesageService;
    }

    @Override
    public void start(Future<Void> fut) throws Exception {
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        router.get("/api/ping").handler(this::ping);
        router.get("/api/allMessage").handler(this::allMessage);

        router.route("/").handler(routingContext -> httpJsonResponse(routingContext).end("DMS - Rest endpoints"));


        // Create the HTTP server and pass the "accept" method to the request handler.
        vertx.createHttpServer().requestHandler(router::accept).listen(
                // Retrieve the port from the configuration,
                // default to HTTP_PORT.
                config().getInteger("http.port", HTTP_PORT), result -> {
                    if (result.succeeded()) {
                        logger.info("Rest service started successfully at port" + HTTP_PORT);
                        fut.complete();
                    } else {
                        fut.fail(result.cause());
                    }
                });
    }

    private void allMessage(RoutingContext routingContext) {
        List<String> messages = new LinkedList<>();

        mesageService.getAllMessages()
                .subscribe(new Subscriber<IncommingMessage>() {
                    @Override
                    public void onCompleted() {
                        httpJsonResponse(routingContext).end(Json.encodePrettily(messages));
                    }

                    @Override
                    public void onError(Throwable e) {
                        routingContext.response().setStatusCode(500).end(e.getMessage());
                    }

                    @Override
                    public void onNext(IncommingMessage message) {
                        messages.add(message.toString());
                    }
                });
    }

    private void ping(RoutingContext routingContext) {
        httpJsonResponse(routingContext).end("PONG");
    }

    private HttpServerResponse httpJsonResponse(RoutingContext routingContext) {
        return routingContext.response().putHeader("content-type", "application/json");
    }

    private HttpServerResponse httpHTMLResponse(RoutingContext routingContext) {
        return routingContext.response().putHeader("content-type", "text/html; charset=utf-8");
    }
}