package lab6;

import akka.actor.ActorRef;
import org.apache.zookeeper.*;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class ServersHandler {
    private final ZooKeeperClass zooKeeperClass;
    private final String serversPath;
    private final ActorRef serversStorage;


    public ServersHandler(ZooKeeperClass zooKeeperClass, ActorRef serversStorage, String serversPath){
        this.zooKeeperClass = zooKeeperClass;
        this.serversStorage = serversStorage;
        this.serversPath = serversPath;

    }

    private  void saveServers(List<String> serversNodes){
        this.serversStorage.tell(new PutServersMsg(serversNodes), ActorRef.noSender());
    }

    public void createServers(String name, String host, int port) throws Exception{
        String serverPath = zooKeeperClass.create(
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
                    zooKeeperClass.getChildren(serversPath, this::watchChildrenCallback).stream()
                    .map(s -> serversPath + "/" + s)
                    .collect(Collectors.toList())
            );
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public  void removeAllWatches() throws Exception{
        zooKeeperClass.removeAllWatches(serversPath, Watcher.WatcherType.Any, true);
    }



}
