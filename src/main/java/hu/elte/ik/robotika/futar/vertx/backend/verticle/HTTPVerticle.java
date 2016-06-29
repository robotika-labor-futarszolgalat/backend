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

	private Map<String, JsonObject> placedBTDevices = new LinkedHashMap<String, JsonObject>();

	private Map<String, JsonObject> newBTDevices = new LinkedHashMap<String, JsonObject>();

	private Map<String, Map<String, Double>> btData = new LinkedHashMap<String, Map<String, Double>>();

	private List<String> activeRobots = new ArrayList<String>();

	private String webRoot;

	private static final double EPSILON = 0.000001;

	private boolean calculateThreeCircleIntersection(double x0, double y0, double r0,
                                                 double x1, double y1, double r1,
                                                 double x2, double y2, double r2)
	{
	    double a, dx, dy, d, h, rx, ry;
	    double point2_x, point2_y;

	    /* dx and dy are the vertical and horizontal distances between
	    * the circle centers.
	    */
	    dx = x1 - x0;
	    dy = y1 - y0;

	    /* Determine the straight-line distance between the centers. */
	    d = Math.sqrt((dy*dy) + (dx*dx));

	    /* Check for solvability. */
	    if (d > (r0 + r1))
	    {
	        /* no solution. circles do not intersect. */
	        return false;
	    }
	    if (d < Math.abs(r0 - r1))
	    {
	        /* no solution. one circle is contained in the other */
	        return false;
	    }

	    /* 'point 2' is the point where the line through the circle
	    * intersection points crosses the line between the circle
	    * centers.
	    */

	    /* Determine the distance from point 0 to point 2. */
	    a = ((r0*r0) - (r1*r1) + (d*d)) / (2.0 * d) ;

	    /* Determine the coordinates of point 2. */
	    point2_x = x0 + (dx * a/d);
	    point2_y = y0 + (dy * a/d);

	    /* Determine the distance from point 2 to either of the
	    * intersection points.
	    */
	    h = Math.sqrt((r0*r0) - (a*a));

	    /* Now determine the offsets of the intersection points from
	    * point 2.
	    */
	    rx = -dy * (h/d);
	    ry = dx * (h/d);

	    /* Determine the absolute intersection points. */
	    double intersectionPoint1_x = point2_x + rx;
	    double intersectionPoint2_x = point2_x - rx;
	    double intersectionPoint1_y = point2_y + ry;
	    double intersectionPoint2_y = point2_y - ry;

	    log.info("INTERSECTION Circle1 AND Circle2:", "(" + intersectionPoint1_x + "," + intersectionPoint1_y + ")" + " AND (" + intersectionPoint2_x + "," + intersectionPoint2_y + ")");

	    /* Lets determine if circle 3 intersects at either of the above intersection points. */
	    dx = intersectionPoint1_x - x2;
	    dy = intersectionPoint1_y - y2;
	    double d1 = Math.sqrt((dy*dy) + (dx*dx));

	    dx = intersectionPoint2_x - x2;
	    dy = intersectionPoint2_y - y2;
	    double d2 = Math.sqrt((dy*dy) + (dx*dx));

	    if(Math.abs(d1 - r2) < EPSILON) {
	        log.info("INTERSECTION Circle1 AND Circle2 AND Circle3:", "(" + intersectionPoint1_x + "," + intersectionPoint1_y + ")");
	    }
	    else if(Math.abs(d2 - r2) < EPSILON) {
	        log.info("INTERSECTION Circle1 AND Circle2 AND Circle3:", "(" + intersectionPoint2_x + "," + intersectionPoint2_y + ")"); //here was an error
	    }
	    else {
	        log.info("INTERSECTION Circle1 AND Circle2 AND Circle3:", "NONE");
	    }
	    return true;
	}

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
		options.addOutboundPermitted(new PermittedOptions().setAddressRegex("new.bt.device"));
		options.addOutboundPermitted(new PermittedOptions().setAddressRegex("placed.bt.device"));
		options.addInboundPermitted(new PermittedOptions().setAddressRegex("login.robot"));
		options.addInboundPermitted(new PermittedOptions().setAddressRegex("login.client"));
		options.addInboundPermitted(new PermittedOptions().setAddressRegex("robot.\\.[0-9]+"));
		options.addInboundPermitted(new PermittedOptions().setAddressRegex("place.bt.device"));
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
						log.info("Client sent a message: " + event.rawMessage());
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

							for (JsonObject value : placedBTDevices.values()) {
							    vertx.eventBus().publish("placed.bt.device", Json.encode(value));
							}

							for (JsonObject value : newBTDevices.values()) {
							    vertx.eventBus().publish("new.bt.device", Json.encode(value));
							}
						}

						if (event.rawMessage().getString("address").equals("place.bt.device"))
						{

							 JsonObject data = event.rawMessage().getJsonObject("body");
							 log.info("Place bt device: " + data.getString("address"));
							 placedBTDevices.remove(data.getString("address"));
							 placedBTDevices.put(data.getString("address"), data);
							 newBTDevices.remove(data.getString("address"));
							 vertx.eventBus().publish("placed.bt.device", Json.encode(data));
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
												newBTDevices.get(pb.getString("address")) == null)
												{
													JsonObject data = new JsonObject(Json.encode(pb));
													data.remove("rssi");
													newBTDevices.put(pb.getString("address"), data);
													log.info("New bt device: " + buffer);
													vertx.eventBus().publish("new.bt.device", Json.encode(data));
												}

											btData.get(id).remove(pb.getString("address"));

											log.info("Update bt data: " + id + " " + pb.getString("address") + " " + pb.getInteger("rssi"));
											double d = Math.pow(10,(pb.getInteger("rssi") - (-40))/((-10)*2));
											//RSSI (dBm) = -10n log10(d) + A
											log.info("the calculated distance is around: " + d + "m");
											btData.get(id).put(pb.getString("address"), d*27);
							} else if (response.getString("action") != null &&
									response.getString("action").equals("start.bluetooth.scan") &&
									sockets.get(id) == null)
							{
								log.info("start.bluetooth.scan");
								btData.remove(id);
								btData.put(id, new LinkedHashMap<String, Double>());

							}
							else if (response.getString("action") != null &&
									response.getString("action").equals("finished.bluetooth.scan") &&
									sockets.get(id) == null)
							{
								log.info("finished.bluetooth.scan");
								if (btData.get(id).size() >= 3)
								{
									double x0 = 0, y0 = 0, r0 = 0, x1 = 0, y1 = 0, r1 = 0, x2 = 0, y2 = 0, r2 = 0;
									int i = 0;
									for (Map.Entry<String, Double> entry : btData.get(id).entrySet()) {
									    String key = entry.getKey();
											JsonObject placedBT = placedBTDevices.get(key);
											if (placedBT == null)
											{
												continue;
											}
									    double value = entry.getValue();
											if (i == 0) {
												x0 = placedBT.getDouble("x");
												y0 = placedBT.getDouble("y");
												r0 = value;
											} else if (i == 1) {
												x1 = placedBT.getDouble("x");
												y1 = placedBT.getDouble("y");
												r1 = value;
											} else if (i == 2) {
												x2 = placedBT.getDouble("x");
												y2 = placedBT.getDouble("y");
												r2 = value;
											} else {
												break;
											}
											i++;
									}

									if (i == 3) {
										calculateThreeCircleIntersection(x0, y0, r0, x1, y1, r1, x2, y2, r2);
									}


								} else
								{
									log.info("There is not enough BT data to calculate the location of the robot.");
								}
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
