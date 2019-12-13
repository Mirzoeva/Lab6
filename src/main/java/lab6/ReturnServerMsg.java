package lab6;

public class ReturnServerMsg {
    private final String server;

    public ReturnServerMsg(String server){
        this.server = server;
    }

    public String get(){
        return server;
    }
}
