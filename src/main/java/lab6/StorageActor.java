package lab6;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import junit.framework.TestResult;

import java.util.ArrayList;
import java.util.Random;


import java.util.Map;
import java.util.TreeMap;

public class StorageActor  extends AbstractActor {
    private final List<String> storage;
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
                .match(UrlTest.class, msg -> getSender().tell(new TestResult(msg, storage.get(msg)), ActorRef.noSender()))
                .match(TestResult.class, msg -> storage.put(msg.getUrltest(), msg.getMiddleValue()))
                .build();
    }
}
