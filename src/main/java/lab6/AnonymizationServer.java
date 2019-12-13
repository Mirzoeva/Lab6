package lab6;

import akka.actor.ActorRef;
import org.asynchttpclient.AsyncHttpClient;
import akka.http.javadsl.server.Route;
import akka.pattern.Patterns;
import org.asynchttpclient.Response;
import akka.actor.ActorRef;
import akka.http.javadsl.server.Route;
import akka.pattern.Patterns;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import java.net.ConnectException;
import java.time.Duration;
import java.util.concurrent.CompletionStage;


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

    private Route handleGetWithUUrlCount(String url, int count){
        CompletionStage<Response> responseCompletionStage = count == 0?
                fetch(http.prepareGet(url).build()) : redirectToAnother(url, count - 1);
        return completeOKWithFutureString(responseCompletionStage.thenApply(Response::getResponseBody));
    }

    



}
