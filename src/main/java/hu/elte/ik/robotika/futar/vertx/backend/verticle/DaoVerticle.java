package hu.elte.ik.robotika.futar.vertx.backend.verticle;

import hu.elte.ik.robotika.futar.vertx.backend.event.Event;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class DaoVerticle extends AbstractVerticle{
    private final Logger log = LoggerFactory.getLogger(DaoVerticle.class);
    EventBus eb;

    @Override
    public void start() {
        init();
        eb.<String>consumer(Event.EXAMPLE_EVENT1, message -> {
            String body = message.body();
            System.out.println(body.toUpperCase());
        });
    }

    private void init() {
        log.info("DaoVerticle starting");
        eb = vertx.eventBus();
    }
}
