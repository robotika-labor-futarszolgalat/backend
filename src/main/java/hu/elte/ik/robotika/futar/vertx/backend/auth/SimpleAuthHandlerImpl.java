package hu.elte.ik.robotika.futar.vertx.backend.auth;

import org.apache.http.HttpStatus;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.impl.AuthHandlerImpl;

public class SimpleAuthHandlerImpl extends AuthHandlerImpl implements SimpleAuthHandler {

	private static final Logger log = LoggerFactory.getLogger(SimpleAuthHandlerImpl.class);

	public SimpleAuthHandlerImpl(AuthProvider authProvider) {
		super(authProvider);
	}

	@Override
	public void handle(RoutingContext context) {
		Session session = context.session();
		if (session != null) {
			User user = context.user();
			if (user != null) {
				// Already logged in, just authorise
				log.info("Already logged in, just authorise");
				authorise(user, context);
			} else {
				log.info("Protected content, user is not authenticated currently");
				context.response().setStatusCode(HttpStatus.SC_FORBIDDEN).end();
			}
		} else {
			context.fail(new NullPointerException("No session - did you forget to include a SessionHandler?"));
		}
	}

}
