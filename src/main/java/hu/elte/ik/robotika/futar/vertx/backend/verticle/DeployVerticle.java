/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.elte.ik.robotika.futar.vertx.backend.verticle;

import hu.elte.ik.robotika.futar.vertx.backend.util.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class DeployVerticle exteds AbstractVerticle {
    private final Logger log = LoggerFactory.getLogger(DeployVerticle.class);

    public static void main(String[] args) {
        Runner.run(DeployVerticle.class);
    }

    @Override
    public void start() {
        init();
        DeploymentOptions baseConfig = new DeploymentOptions().setConfig(config());
        vertx.deployVerticle(new FrontendVerticle(), baseConfig);
        vertx.deployVerticle(new RobotVerticle(), baseConfig);
        vertx.deployVerticle(new DaoVerticle(), baseConfig);
    }

    private void init() {
        log.info("DeployVerticle starting");
    }
}
