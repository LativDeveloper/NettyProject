import io.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONObject;

public abstract class Client {

    protected ChannelHandlerContext context;

    abstract void sendMessage(JSONObject message);

    void sendErrorCode(String errorCode) {
        JSONObject query = new JSONObject();
        query.put("errorCode", errorCode);
        sendMessage(query);
    }

    void disconnect() {
        context.disconnect();
    }
}
