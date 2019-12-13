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
                get(() -> parameter("url", url ->
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

    private CompletionStage<Response> redirectToAnother(String url, int count) {
        return  Patterns.ask(serversStorage, new GetRandomServerMsg(), Duration.ofSeconds(3))
                .thenApply(o -> ((ReturnServerMsg)o).get())
                .thenCompose(znode ->
                        fetch(createServerRequest(getServerUrl(znode), url, count))
                                .handle((resp, ex) -> handleBadRedirection(resp, ex, znode))
                );
    }

    private Response handleBadRedirection(Response resp, Throwable ex, String znode) {
        if (ex instanceof ConnectException) {
            serversStorage.tell(new DeleteServerMsg(znode), ActorRef.noSender());
        }
        return resp;
    }

    private CompletionStage<Response> fetch(Request req) {
        return http.executeRequest(req).toCompletableFuture();
    }

    private Request createServerRequest(String serverUrl, String url, int count) {
        return http.prepareGet(serverUrl)
                .addQueryParam("url", url)
                .addQueryParam("count", Integer.toString(count))
                .build();
    }

    private String getServerUrl(String serverZNode) {
        try {
            return new String(zooKeeper.getData(serverZNode, false, null));
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }





}
