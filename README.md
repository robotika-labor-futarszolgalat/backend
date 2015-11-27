#Backend docs
##Verticles
<dl>
<dt>DeployVerticle</dt>
<dd>This verticle deploys all the other verticles on server startup.</dd>
<dt>RobotVerticle</dt>
<dd>Communicates with the robots via TCP and gives instructions based on the shortest path (eg. go forward 10 units, turn right 90 degrees)</dd>
<dt>DaoVerticle</dt>
<dd>Communicates with the DB, validates login data, queries the map, findins the shortest path between two positions</dd>
<dt>FrontendVerticle</dt>
<dd>Serves the website and communicates with the frontend via websocket and a REST API (eg. getting the current positions of the robots, handling user events, mapping the robot's coordinate to the closes coordinate on the map)</dd>
</dl>

##Eventbus topics
Topic | Sent/published by | When | Consumed by
-------- | ------- | ------- | -------
Position updated | RobotVerticle | A robot updates its position | FrontendVerticle
Map*1 | DaoVerticle | The measured coordinates are queried at startup | HttpVerticle
Shortest path*2 | DaoVerticle | A route needs to be calculated for a robot | RobotVerticle
Login request | FrontendVerticle| A user tries to log in| DaoVerticle
*1 __Map should be stored as Vert.x shared data__

*2 __Includes sending current and destination position to DaoVerticle__
