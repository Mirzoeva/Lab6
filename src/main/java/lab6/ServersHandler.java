package lab6;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.model.*;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.asynchttpclient.AsyncHttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ServersHandler {
    private final ActorMaterializer materializer;
    private final ActorRef storage;
    private final AsyncHttpClient asyncHttpClient;
    private int count;
    private 

    public ServersHandler(AsyncHttpClient asyncHttpClient, ActorSystem system, ActorMaterializer materializer){
        this.asyncHttpClient = asyncHttpClient;
        this.materializer = materializer;
        this.storage = system.actorOf(StorageActor.props());
        this.count = Constants.countOfRequests;
    }

    public Flow<HttpRequest, HttpResponse, NotUsed> createRoute(){
        return Flow.of(HttpRequest.class)
                .map(this::parseRequest)
                .mapAsync(count, test ->
                     Patterns.ask(storage, test, Duration.ofSeconds(5))
                            .thenApply(o -> (TestResult)o)
                            .thenCompose(result -> result.get().isPresent() ?
                                    CompletableFuture.completedFuture(result.get().get()) : runTest(test)))
                .map(result -> {
                    storage.tell(result, ActorRef.noSender());
                    return HttpResponse.create()
                            .withStatus(StatusCodes.OK)
                            .withEntity(ContentTypes.APPLICATION_JSON,
                                    ByteString.fromString(new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(result)
                            ));
                });
    }

    public UrlTest parseRequest(HttpRequest request){
        Query query = request.getUri().query();
        Optional<String> testUrl = query.get(Constants.url);
        Optional<String> count = query.get(Constants.count);
        return new UrlTest(testUrl.get(), Integer.parseInt(count.get()));
    }

    private CompletionStage<TestResult> runTest(UrlTest test){
        final Sink<UrlTest, CompletionStage<Long>> testSink =
                Flow.of(UrlTest.class)
                .mapConcat(t -> {
                    count = t.getCount();
                    return Collections.nCopies(t.getCount(), t.getUrl());
                })
                .mapAsync(count, url -> {
                    Long requestStart = Instant.now().toEpochMilli();
                    return asyncHttpClient.prepareGet(url).execute()
                            .toCompletableFuture()
                            .thenCompose(resp -> CompletableFuture.supplyAsync(()->Instant.now().toEpochMilli() - requestStart));
                })
                .toMat(Sink.fold(0L, Long::sum), Keep.right());

        return Source.from(Collections.singleton(test))
                .toMat(testSink, Keep.right())
                .run(materializer)
                .thenApply(sum -> new TestResult(test, sum/test.getCount()));
    }
}
