package hu.elte.ik.robotika.futar.vertx.backend.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.NetServer;

public class RobotVerticle extends AbstractVerticle{
    @Override
    public void start() {
        NetServer tcp = vertx.createNetServer();
        tcp.connectHandler(socket -> {
            socket.handler(buffer -> System.out.println(buffer));
            socket.closeHandler(event -> System.out.println("Connection closed"));
        });
        tcp.listen(9090);
    }
}
