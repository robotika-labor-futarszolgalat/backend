package hu.elte.ik.robotika.futar.vertx.backend.auth;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;

public class SimpleLoginHandlerImpl implements SimpleLoginHandler {

	private static final Logger log = LoggerFactory.getLogger(SimpleLoginHandlerImpl.class);

	protected AuthProvider authProvider;

	public SimpleLoginHandlerImpl(AuthProvider authProvider) {
		this.authProvider = authProvider;
	}

	protected JsonObject buildLoginResponse(Boolean success) {
		JsonObject result = new JsonObject();
		result.put("success", success);
		return result;
	}

	@Override
	public void handle(RoutingContext context) {
		HttpServerRequest req = context.request();
		if (req.method() != HttpMethod.POST) {
			// Must be a POST
			context.fail(405);
		} else {
			if (!req.isExpectMultipart()) {
				throw new IllegalStateException("Form body not parsed - do you forget to include a BodyHandler?");
			}
			MultiMap params = req.formAttributes();
			String username = params.get("username");
			String password = params.get("password");
			if (username == null || password == null) {
				context.response().end(Json.encodePrettily(buildLoginResponse(false)));
			} else {
				JsonObject authInfo = new JsonObject().put("username", username).put("password", password);
				authProvider.authenticate(authInfo, res -> {
					Boolean success = false;
					if (res.succeeded()) {
						User user = res.result();
						context.setUser(user);
						success = true;
					}
					log.debug("login success: {0}", success);
					context.response().end(Json.encodePrettily(buildLoginResponse(success)));
				});
			}
		}
	}

}
