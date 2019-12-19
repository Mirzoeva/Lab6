package lab6;

import akka.actor.ActorRef;
import org.apache.zookeeper.*;
import java.util.List;
import java.util.stream.Collectors;


public class ServersHandler {
    private final ZooKeeper keeper;
    private final String serversPath;
    private final ActorRef serversStorage;


    public ServersHandler(ZooKeeper keeper, ActorRef serversStorage, String serversPath){
        this.keeper = keeper;
        this.serversStorage = serversStorage;
        this.serversPath = serversPath;
        watchChildrenCallback(null);
    }

    private void watchChildrenCallback(WatchedEvent event){
        try {
            saveServers(
                    keeper.getChildren(serversPath, this::watchChildrenCallback).stream()
                            .map(s -> serversPath + "/" + s)
                            .collect(Collectors.toList())
            );
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private  void saveServers(List<String> serversNodes){
        this.serversStorage.tell(new PutServersMsg(serversNodes), ActorRef.noSender());
    }

    public void createServers(String name, String host, int port) throws KeeperException, InterruptedException{
        String serverPath = keeper.create(
                serversPath + "/" + name,
                (host + ":" + port).getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL
        );
    }
}
