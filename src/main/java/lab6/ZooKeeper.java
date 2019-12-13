package lab6;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;

import java.util.Scanner;
import java.util.concurrent.CompletionStage;
import org.apache.zookeeper.ZooKeeper;
import org.asynchttpclient.AsyncHttpClient;


import java.io.IOException;
import java.util.logging.Logger;

public class ZooKeeper {
    private static final String zooKeeperHost = "127.0.0.1:2181";
    public static void main(String[] args) throws IOException {
        System.out.println(Constants.startMsg);
        Scanner in = new Scanner(System.in);

        Constants constants = new Constants();
        constants.setPort(in.nextInt());

        final ZooKeeper zooKeeper = new ZooKeeper(
                zooKeeperHost,
                5000,
                e -> Logger.getLogger(ZooKeeper.class.getName()).info(e.toString())
        );

        final ActorSystem system = ActorSystem.create("routes");
        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);
        final AsyncHttpClient asyncHttpClient = asyncHttpClient();


        ActorRef storageActor = system.actorOf(StorageActor.props());

        final ServersHandler  serversHandler = new ServersHandler(
                zooKeeper, storageActor, "/servers");

        final Anonymizationserver anonServer = new Anonymizationserver(
                storageActor,
                asyncHttpClient,
                zooKeeper
        );


        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = anonServer.createRoute().flow(system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(
                routeFlow,
                ConnectHttp.toHost(Constants.hostName, Constants.port),
                materializer
        );

        System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
        System.in.read();
        zooKeeper.close();
        binding
                .thenCompose(ServerBinding::unbind)
                .thenAccept(unbound -> {
                    system.terminate();
                try{
                    asyncHttpClient.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
        });
    }
}



