package hu.elte.ik.robotika.futar.vertx.backend.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.NetServer;

public class RobotVerticle extends AbstractVerticle{
    private final Logger log = LoggerFactory.getLogger(RobotVerticle.class);

    @Override
    public void start() {
        init();
        NetServer tcp = vertx.createNetServer();
        tcp.connectHandler(socket -> {
            socket.handler(buffer -> System.out.println(buffer));
            socket.closeHandler(event -> System.out.println("Connection closed"));
        });
        tcp.listen(9090);
    }

    private void init() {
        log.info("RobotVerticle starting");
    }
}
