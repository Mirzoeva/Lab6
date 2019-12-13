package lab6;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import org.apache.zookeeper.*;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;


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
        Logger.getLogger(ServersHandler.class.getName()).info("Created Server Path: " + serverPath);
    }

    private void watchChildrenCallback(WatchedEvent event){
        if (event != null){
            Logger.getLogger(ServersHandler.class.getName()).info(event.toString());
        }
        try {
            saveServers(
                    zooKeeper.getChildren(serversPath, this::watchChildrenCallback).stream()
                    .map(s -> serversPath + "/" + s)
                    .collect(Collectors.toList())
            );
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }



}
