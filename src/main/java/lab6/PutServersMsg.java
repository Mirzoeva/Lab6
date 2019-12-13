package lab6;

import java.util.List;

public class PutServersMsg {
    private final List<String> servers;

    public  PutServersMsg(List<String> servers){
        this.servers = servers;
    }

    public List<String> getServers(){
        return servers;
    }
}
