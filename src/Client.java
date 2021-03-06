import io.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONObject;

public abstract class Client {

    ChannelHandlerContext context;
    String name;
    long lastOnlineTimeMills;

    Client() {
        lastOnlineTimeMills = System.currentTimeMillis();
    }

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

    void setLastOnlineTimeMills(long time) {
        lastOnlineTimeMills = time;
    }

    long getLastOnlineTimeMills() {
        return lastOnlineTimeMills;
    }

    void sendStartDownloadFile(String filename, long port, String downloadPath, String target) {
        JSONObject query = new JSONObject();
        query.put("action", "start.download.file");
        query.put("filename", filename);
        query.put("port", port);
        query.put("downloadPath", downloadPath);
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
