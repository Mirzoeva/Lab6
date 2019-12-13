package lab6;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.model.*;
import org.apache.zookeeper.*;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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
    private final ZooKeeper zooKeeper;
    private final String serversPath;
    private final ActorRef serversStorage;


    public ServersHandler(ZooKeeper zooKeeper, ActorSystem serversStorage, String serversPath){
        this.zooKeeper = zooKeeper;
        this.serversStorage = serversStorage;
        this.serversPath = serversPath;

    }

    public  void removeAllWatches() throws Exception{
        zooKeeper.removeAllWatches(serversPath, Watcher.WatcherType.Any, true);
    }

    private  void saveServers(List<String> serversNodes){
        this.serversStorage.tell(new PutServersMsg(serversNodes), ActorRef.noSender());
    }

    public void createServers(String name, String host, int port) throws Exception{
        String serverPath = zooKeeper.create(
                serversPath + "/" + name,
                (host + ":" + port).getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL
        );
        Logger.getLogger(ServersHandler.class.getName()).info();
    }



}
