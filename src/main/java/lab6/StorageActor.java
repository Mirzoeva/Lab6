package lab6;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import java.util.ArrayList;
import java.util.Random;
import java.util.List;


public class StorageActor  extends AbstractActor {
    private List<String> storage;
    private final Random random;

    public StorageActor(){
        this.random = new Random();
        this.storage = new ArrayList<>();
    }
    public static Props props(){
        return Props.create(StorageActor.class);
    }

    @Override
    public Receive createReceive(){
        return receiveBuilder()
                .match(PutServersMsg.class, this::receivePutServerMsg)
                .match(GetRandomServerMsg.class, this::receiveGetRandomServerMsg)
                .build();
    }

    private void receivePutServerMsg(PutServersMsg msg){
        this.storage = msg.getServers();
    }

    private void receiveGetRandomServerMsg(GetRandomServerMsg msg){
        sender().tell(storage.get(random.nextInt(storage.size())), self());
    }

}
