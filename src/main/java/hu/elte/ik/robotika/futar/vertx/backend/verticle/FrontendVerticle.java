/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.elte.ik.robotika.futar.vertx.backend.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author joci
 */
public class FrontendVerticle extends AbstractVerticle {
    private final Logger log = LoggerFactory.getLogger(FrontendVerticle.class);
    private List<ServerWebSocket> sockets;
    private String webRoot;

    @Override
    public void start() {
        init();
        HttpServer http = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.route("/ws").handler(this::handleWebSocketConnection);
        router.route().handler(StaticHandler.create().setWebRoot(webRoot));
        http.requestHandler(router::accept).listen(8080);
    }

    private void handleWebSocketConnection(RoutingContext context){
        HttpServerRequest req = context.request();
        ServerWebSocket ws = req.upgrade();
        sockets.add(ws);
        ws.handler(buffer -> System.out.println(buffer));
        ws.endHandler(event -> sockets.remove(ws));
    }

    private void init() {
        log.info("FrontendVerticle starting");
        sockets = new ArrayList<>();
        webRoot = config().getString("webRoot", "./webapp");
    }
}