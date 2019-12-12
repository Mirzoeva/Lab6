package lab5;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import java.util.concurrent.CompletionStage;
import static org.asynchttpclient.Dsl.asyncHttpClient;
import org.asynchttpclient.AsyncHttpClient;


import java.io.IOException;

public class AkkaStream{
    public static void main(String[] args) throws IOException {
        System.out.println(Constants.startMsg);
        ActorSystem system = ActorSystem.create("routes");
        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        final AsyncHttpClient asyncHttpClient = asyncHttpClient();
        final Tester tester = new Tester(asyncHttpClient, system, materializer);
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = tester.createRoute();
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(
                routeFlow,
                ConnectHttp.toHost(Constants.hostName, Constants.port),
                materializer
        );
        System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
        System.in.read();
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



