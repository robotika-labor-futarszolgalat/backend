package hu.elte.ik.robotika.futar.vertx.backend.auth;

import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.handler.AuthHandler;

public interface SimpleAuthHandler extends AuthHandler {

	static SimpleAuthHandler create(AuthProvider authProvider) {
		return new SimpleAuthHandlerImpl(authProvider);
	}

}
