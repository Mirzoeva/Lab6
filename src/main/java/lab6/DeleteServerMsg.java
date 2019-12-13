package lab6;

public class DeleteServerMsg {
    private final String server;

    public DeleteServerMsg(String server){
        this.server = server;
    }

    public String getServer(){
        return server;
    }
}
