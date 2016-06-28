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
import java.awt.Point;

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
	private Map<String, ServerWebSocket> sockets = new LinkedHashMap<String, ServerWebSocket>();

	private Map<String, Point> placedBTDevices = new LinkedHashMap<String, Point>();

	private List<String> newBTDevices = new ArrayList<String>();

	private Map<String, Map<String, Integer>> btData = new LinkedHashMap<String, Map<String, Integer>>();

	private List<String> activeRobots = new ArrayList<String>();

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
            .addOutboundPermitted(new PermittedOptions().setAddressRegex("new.robot"));
		options.addOutboundPermitted(new PermittedOptions().setAddressRegex("logout.robot"));
		options.addInboundPermitted(new PermittedOptions().setAddressRegex("login.robot"));
		options.addInboundPermitted(new PermittedOptions().setAddressRegex("login.client"));
		options.addInboundPermitted(new PermittedOptions().setAddressRegex("robot.\\.[0-9]+"));
		// use this to send message to the clients:
		// routingContext.vertx().eventBus().publish("muhaha", routingContext.getBodyAsString());
    return SockJSHandler.create(vertx).bridge(options, event -> {
          if (event.type() == BridgeEventType.SOCKET_CREATED) {
            log.info("A socket was created");
	        }
					if (event.type() == BridgeEventType.SOCKET_CLOSED) {
            log.info("A socket was closed");
						if (activeRobots.contains(event.socket().writeHandlerID()))
						{
						  log.info("Robot logged out");
						  activeRobots.remove(event.socket().writeHandlerID());
						  JsonObject response = new JsonObject();
							response.put("robotId", event.socket().writeHandlerID());
							vertx.eventBus().publish("logout.robot", Json.encode(response));
						}
 	        }
					if (event.type() == BridgeEventType.REGISTER) {
						log.info("A handler was registered");
						log.info(event.rawMessage());
 	        }
					if (event.type() == BridgeEventType.SEND) {
						log.info("Client sent a message");
						if (event.rawMessage().getString("address").equals("login.robot") && !activeRobots.contains(event.socket().writeHandlerID()))
						{
							log.info("Robot logged in");
							activeRobots.add(event.socket().writeHandlerID());
							JsonObject response = new JsonObject();
							response.put("robotId", event.socket().writeHandlerID());
							vertx.eventBus().publish("new.robot", Json.encode(response));
						} else if (event.rawMessage().getString("address").equals("login.robot"))
						{
							log.info("Robot already logged in");
						}

						if (event.rawMessage().getString("address").equals("login.client"))
						{
							for (String key : sockets.keySet()) {
								log.info("Send active robots");
								JsonObject response = new JsonObject();
								response.put("robotId", key);
								vertx.eventBus().publish("new.robot", Json.encode(response));
							}
						}
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

		router.route("/ws").handler(this::handleWebSocketConnection);

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
		http.websocketHandler(ws ->
			{
				String id = java.util.UUID.randomUUID().toString();
				ws.handler(buffer ->
					{
						try {
							JsonObject response = new JsonObject(buffer.toString());
							if (response.getString("action") != null &&
									response.getString("action").equals("login.robot") &&
									sockets.get(id) == null)
							{
								log.info("robot logged in:" + id);
								sockets.put(id, ws);
								JsonObject pb = new JsonObject();
								pb.put("robotId", id);
								vertx.eventBus().publish("new.robot", Json.encode(pb));
							} else if (response.getString("action") != null &&
									response.getString("action").equals("found.bluetooth")) {
										log.info("found.bluetooth");
										JsonObject pb = response.getJsonObject("data");
										if (placedBTDevices.get(pb.getString("address")) == null &&
												!newBTDevices.contains(pb.getString("address")))
												{
													newBTDevices.add(pb.getString("address"));
													log.info("New bt device: " + buffer);
												}

										if (btData.get(id) == null)
										{
											btData.put(id, new LinkedHashMap<String, Integer>());
											log.info("New bt data for the following robot: " + id);
										}
											btData.get(id).remove(pb.getString("address"));
											btData.get(id).put(pb.getString("address"), pb.getInteger("rssi"));
											log.info("Update bt data: " + id + " " + pb.getString("address") + " " + pb.getInteger("rssi"));
							}

							log.info("got the following message from " + id + ": " + Json.encode(response));

						} catch (Exception e)
						{
							log.info("Cannot process the following buffer: " + buffer);
							log.info("The following error happend: " + e.getMessage());
						}
					}
				);
				ws.endHandler(e -> {
					JsonObject response = new JsonObject();
					response.put("robotId", id);
					vertx.eventBus().publish("logout.robot", Json.encode(response));
					sockets.remove(id);
					log.info("The following robot logged out: " + id);
				});
			}).requestHandler(router::accept).listen(Integer.getInteger("http.port"), System.getProperty("http.address", "0.0.0.0"));
	}

	private void answer(String id)
	{
			sockets.get(id).writeFinalTextFrame("Geronimo!");
	}

	private void handleWebSocketConnection(RoutingContext context) {
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
		//ockets = new ArrayList<>();
		webRoot = config().getString("webRoot", "src/main/resources/webroot");
	}
}
