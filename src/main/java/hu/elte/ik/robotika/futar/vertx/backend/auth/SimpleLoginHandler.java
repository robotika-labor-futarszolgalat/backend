package hu.elte.ik.robotika.futar.vertx.backend.auth;

import io.vertx.core.Handler;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.RoutingContext;

public interface SimpleLoginHandler extends Handler<RoutingContext> {

	static SimpleLoginHandler create(AuthProvider authProvider) {
		return new SimpleLoginHandlerImpl(authProvider);
	}
}
