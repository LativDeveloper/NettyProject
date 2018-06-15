import io.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONObject;

public abstract class Client {

    ChannelHandlerContext context;
    String name;

    abstract void sendMessage(JSONObject message);

    void sendErrorCode(String errorCode) {
        JSONObject query = new JSONObject();
        query.put("errorCode", errorCode);
        sendMessage(query);
    }

    void disconnect() {
        context.disconnect();
    }

    String getName() {
        return name;
    }

    void sendStartDownloadFile(String filename, long port, String target) {
        JSONObject query = new JSONObject();
        query.put("action", "start.download.file");
        query.put("filename", filename);
        query.put("port", port);
        query.put("target", target);
        sendMessage(query);
    }

    void sendStartUploadFile(String path, long port, String target) {
        JSONObject query = new JSONObject();
        query.put("action", "start.upload.file");
        query.put("path", path);
        query.put("port", port);
        query.put("target", target);
        sendMessage(query);
    }

    void sendFinishLoadFile(String filename, String code, String target) {
        JSONObject query = new JSONObject();
        query.put("action", "finish.load.file");
        query.put("filename", filename);
        query.put("code", code);
        query.put("target", target);
        sendMessage(query);
    }
}
