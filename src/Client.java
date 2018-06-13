import io.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONObject;

public abstract class Client {

    protected ChannelHandlerContext context;

    abstract protected void disconnect();
    abstract protected void sendMessage(JSONObject message);

    public void sendErrorCode(String errorCode) {
        JSONObject query = new JSONObject();
        query.put("errorCode", errorCode);
        sendMessage(query);
    }
}
