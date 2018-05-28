package client;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.json.simple.JSONObject;

import java.net.SocketAddress;

public class UserDeviceHandler extends ChannelInboundHandlerAdapter {
    private ChannelHandlerContext ctx;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        SocketAddress address = ctx.channel().remoteAddress();
        System.out.println("Успешно подключились! ("+address+")");
        this.ctx = ctx;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("action", "auth.user");
        jsonObject.put("login", "vetal");
        jsonObject.put("password", "11211121");
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
        UserDevice.getInstance().run();
    }

    public void sendMessage(JSONObject message) {
        if (!ctx.channel().isActive()) System.out.println("Нет соединения с сервером!");
        else {
            System.out.println("Server << " + message);
            ChannelFuture future = this.ctx.writeAndFlush(message);
        }
    }
}