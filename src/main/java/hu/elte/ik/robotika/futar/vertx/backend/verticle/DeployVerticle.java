/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.elte.ik.robotika.futar.vertx.backend.verticle;

import hu.elte.ik.robotika.futar.vertx.backend.util.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;

public class DeployVerticle extends AbstractVerticle {
    public static void main(String[] args) {
        Runner.run(DeployVerticle.class);
    }

    @Override
    public void start() {
        DeploymentOptions baseConfig = new DeploymentOptions().setConfig(config());
        vertx.deployVerticle(new HttpVerticle(), baseConfig);
        vertx.deployVerticle(new TcpVerticle(), baseConfig);
        vertx.deployVerticle(new Neo4JVerticle(), baseConfig);
    }
}
