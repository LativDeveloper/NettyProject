package client;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.SocketAddress;
import java.util.ArrayList;

public class VictimDeviceHandler extends ChannelInboundHandlerAdapter {
    private ChannelHandlerContext ctx;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        SocketAddress address = ctx.channel().remoteAddress();
        System.out.println("Успешно подключились! ("+address+")");
        this.ctx = ctx;
        //ArrayList<String> owners = new ArrayList<>();
        //owners.add("vetal");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("action", "auth.victim");
        jsonObject.put("name", "vika");
        //jsonObject.put("owners", owners);
        sendMessage(jsonObject);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("Server >> " + msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
        System.out.println("Отключились от сервера!");
        VictimDevice.getInstance().run();
    }

    public void sendMessage(JSONObject message) {
        if (!ctx.channel().isActive()) System.out.println("Нет соединения с сервером!");
        else {
            if (message.get("action").equals("test")) {
                //our test
            } else {
                System.out.println("Server << " + message);
                ctx.writeAndFlush(message);
            }
        }
    }
}