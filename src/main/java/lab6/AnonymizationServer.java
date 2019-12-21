package lab6;

import akka.actor.ActorRef;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.Query;
import akka.http.javadsl.model.Uri;
import akka.http.javadsl.server.Route;
import akka.pattern.Patterns;
import scala.compat.java8.FutureConverters;
import scala.concurrent.Future;
import akka.japi.Pair;
import java.util.concurrent.CompletionStage;
import static akka.http.javadsl.server.Directives.*;


public class AnonymizationServer {
    private final Http http;
    private final ActorRef serversStorage;
    private int port;

    public AnonymizationServer(ActorRef serversStorage, Http http, int port){
        this.http = http;
        this.serversStorage = serversStorage;
        this.port = port;

        new ZooKeeperServer(serversStorage, port);
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
        if (count == 0) {
            return completeWithFuture(http.singleRequest(HttpRequest.create(url)));
        } else {
            Future<Object> future = Patterns.ask(serversStorage, new GetRandomServerMsg(), 2000);
            CompletionStage<Object> stage = FutureConverters.toJava(future);
            return  completeWithFuture(
                    stage.thenCompose(
                            newUrl -> {
                                System.out.println(newUrl + " " + count);
                                return http.singleRequest(
                                        HttpRequest.create(
                                                Uri.create((String) newUrl)
                                                .query(Query.create(
                                                        Pair.create("url", url),
                                                        Pair.create("count", Integer.toString(count - 1))
                                                )).toString()
                                        )
                                );
                            }
                    ));
        }
    }

//    private CompletionStage<Response> redirectToAnother(String url, int count) {
//        return  Patterns.ask(serversStorage, new GetRandomServerMsg(), Duration.ofSeconds(3))
//                .thenApply(o -> ((ReturnServerMsg)o).get())
//                .thenCompose(znode ->
//                        fetch(createServerRequest(getServerUrl(znode), url, count))
//                                .handle((resp, ex) -> handleBadRedirection(resp, ex, znode))
//                );
//    }

//    private Response handleBadRedirection(Response resp, Throwable ex, String znode) {
//        if (ex instanceof ConnectException) {
//            serversStorage.tell(new DeleteServerMsg(znode), ActorRef.noSender());
//        }
//        return resp;
//    }
//
//    private CompletionStage<Response> fetch(Request req) {
//        return http.executeRequest(req).toCompletableFuture();
//    }
//
//    private Request createServerRequest(String serverUrl, String url, int count) {
//        count -=1;
//        return http.prepareGet(serverUrl)
//                .addQueryParam("url", url)
//                .addQueryParam("count", Integer.toString(count))
//                .build();
//    }
//
//    private String getServerUrl(String serverZNode) {
//        try {
//            return new String(zooKeeper.getData(serverZNode, false, null));
//        } catch (KeeperException | InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//    }





}
