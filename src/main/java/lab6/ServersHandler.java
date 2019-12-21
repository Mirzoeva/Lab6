package lab6;

import akka.actor.ActorRef;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class ServersHandler {
    private  ZooKeeper keeper;
    private int port;
    private final ActorRef serversStorage;


    public ServersHandler(ActorRef storeActor, int port){
        try{
            keeper = new ZooKeeper(
                    "127.0.0.1:2181",
                    2000,
                    null
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.serversStorage = storeActor;
        this.port = port;
        watchChildrenCallback();
    }

    private void watchChildrenCallback(){
        try {
            List<String> nodeNames = keeper.getChildren("/root", watchedEvent -> {
                if (watchedEvent.getType() == Watcher.Event.EventType.NodeChildrenChanged){
                    watchChildrenCallback();
                }
            });

            List<String> servers = new ArrayList<>();
            for (String nodeName : nodeNames){
                byte[] url = keeper.getData(
                        "/root" + "/" + nodeName,
                        null,
                        null
                );
                if (url != null) {
                    servers.add(new String(url));
                }
            }
            serversStorage.tell(new PutServersMsg(servers), ActorRef.noSender());
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private  void saveServers(List<String> serversNodes){
        this.serversStorage.tell(new PutServersMsg(serversNodes), ActorRef.noSender());
    }

    public void createNode() {
        try {
            keeper.create("/root/node",
                    ("http://127.0.0.1:2181").getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL_SEQUENTIAL
            );
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
