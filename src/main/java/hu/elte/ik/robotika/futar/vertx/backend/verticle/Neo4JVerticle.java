package hu.elte.ik.robotika.futar.vertx.backend.verticle;

import hu.elte.ik.robotika.futar.vertx.backend.event.Event;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;

public class Neo4JVerticle extends AbstractVerticle{
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
        eb = vertx.eventBus();
    }
}
