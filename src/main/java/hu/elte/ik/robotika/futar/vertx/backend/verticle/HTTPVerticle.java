/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.elte.ik.robotika.futar.vertx.backend.verticle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;

import hu.elte.ik.robotika.futar.vertx.backend.auth.SimpleAuthHandler;
import hu.elte.ik.robotika.futar.vertx.backend.auth.SimpleLoginHandler;
import hu.elte.ik.robotika.futar.vertx.backend.domain.User;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.shiro.ShiroAuth;
import io.vertx.ext.auth.shiro.ShiroAuthRealmType;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import java.util.stream.Stream;
import io.vertx.ext.web.handler.sockjs.BridgeEventType;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.RoutingContext;

/**
 * @author joci
 */
public class HTTPVerticle extends AbstractVerticle {
	private final Logger log = LoggerFactory.getLogger(HTTPVerticle.class);
	private List<ServerWebSocket> sockets;

	private String webRoot;

	// Create some static test entity user
	private static Map<Integer, User> users = new LinkedHashMap<>();

	{
		User adalee = new User("Adalee Smith", "D 0.122");
		users.put(adalee.getId(), adalee);

		User bob = new User("Bob Anderson", "D 0.120");
		users.put(bob.getId(), bob);
	}

	private SockJSHandler eventBusHandler() {
    BridgeOptions options = new BridgeOptions()
            .addOutboundPermitted(new PermittedOptions().setAddressRegex("muhaha"));
		// use this to send message to the clients:
		// routingContext.vertx().eventBus().publish("muhaha", routingContext.getBodyAsString());
    return SockJSHandler.create(vertx).bridge(options, event -> {
         if (event.type() == BridgeEventType.SOCKET_CREATED) {
            log.info("A socket was created");
        }
        event.complete(true);
    });
	}

	private void getAllUser(RoutingContext routingContext) {
		log.info("Get all users");
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.end(Json.encodePrettily(users.values()));
	}

	/**
	 * Preload data
	 *
	 * @param routingContext
	 */
	private void getInfo(RoutingContext routingContext) {
		JsonObject resp = new JsonObject();
		resp.put("preload", "data");

		routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.end(Json.encodePrettily(resp));
	}

	@Override
	public void start() {

		init();
		HttpServer http = vertx.createHttpServer();
		Router router = Router.router(vertx);

		// Setup websocket connection handling
		router.route("/eventbus/*").handler(eventBusHandler());

    // Handle robot position data
    router.route("/api/robotposition/:data").handler(this::handleRobotPositionData);

		// Setup http session auth handling
		router.route().handler(CookieHandler.create());
		router.route().handler(BodyHandler.create());
		router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

		// Simple auth service which uses a properties file for user/role info
		AuthProvider authProvider = ShiroAuth.create(vertx, ShiroAuthRealmType.PROPERTIES, new JsonObject());

		// We need a user session handler too to make sure the user is stored in
		// the session between requests
		router.route().handler(UserSessionHandler.create(authProvider));

		// Any requests to URI starting '/rest/' require login
		router.route("/rest/*").handler(SimpleAuthHandler.create(authProvider));

		// Serve the static private pages from directory 'rest'
		// user/getAll TEST page
		router.route("/rest/user/getAll").handler(this::getAllUser);

		// Preload
		router.route("/rest/info").handler(this::getInfo);

		router.route("/loginhandler").handler(SimpleLoginHandler.create(authProvider));

		router.route("/logout").handler(context -> {
			context.clearUser();
			// Status OK
			context.response().setStatusCode(HttpStatus.SC_OK).end();
		});

		router.route().handler(StaticHandler.create().setWebRoot(webRoot));
		http.websocketHandler(ws -> ws.handler(buffer -> log.info("buffer"))).requestHandler(router::accept).listen(Integer.getInteger("http.port"), System.getProperty("http.address", "0.0.0.0"));
	}

	private void handleWebSocketConnection(RoutingContext context) {
		HttpServerRequest req = context.request();
		ServerWebSocket ws = req.upgrade();
		sockets.add(ws);
		ws.handler(buffer -> log.info(buffer));
		ws.endHandler(e -> sockets.remove(ws));
	}

  private void handleRobotPositionData(RoutingContext context){
      HttpServerRequest req = context.request();
      String data = req.getParam("data");
      System.out.println("Data from robot:");
      Stream.of(data.split("_")).forEach(System.out::println);
      System.out.println("\n");
      context.response().end();
  }

	private void init() {
		log.info("FrontendVerticle starting");
		sockets = new ArrayList<>();
		webRoot = config().getString("webRoot", "src/main/resources/webroot");
	}
}
