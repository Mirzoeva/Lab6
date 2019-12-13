package lab6;

import akka.actor.ActorRef;
import org.asynchttpclient.AsyncHttpClient;
import akka.http.javadsl.server.Route;
import akka.pattern.Patterns;


public class AnonymizationServer {
    private final AsyncHttpClient http;
    private final ActorRef serversStorage;
    private final ZooKeeper zooKeeper;

    public AnonymizationServer(ActorRef serversStorage, AsyncHttpClient http, ZooKeeper zooKeeper){
        this.http = http;
        this.serversStorage = serversStorage;
        this.zooKeeper = zooKeeper;
    }

    public Route createRoute(){
        return route(
                get( () -> parameter("url", url ->
                                parameter( "count", count ->
                                        handleGetWithUUrlCount(url, Integer.parseInt(count))
                                )
                        )

                )
        );
    }

    private 


}
