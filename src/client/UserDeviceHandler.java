package client;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.json.simple.JSONObject;

import javax.print.attribute.standard.Fidelity;
import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;

public class UserDeviceHandler extends ChannelInboundHandlerAdapter {
    private ChannelHandlerContext ctx;
    private String host;

    UserDeviceHandler(String host) {
        // TODO: 14.06.2018 maybe we can find host from 'ctx'? I don't want receive 'host' here
        this.host = host;
    }

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

        receiveMessage((JSONObject) msg);
    }

    private void receiveMessage(JSONObject message) {
        String action = (String) message.get("action");
        switch (action) {
            case "start.upload.file":
                try {
                    Socket socket = new Socket(host, Math.toIntExact((Long) message.get("port"))); //connect to server
                    OutputStream outputStream = socket.getOutputStream();
                    File file = new File((String) message.get("path"));
                    if (file.isDirectory()) {
                        sendErrorCode("fileIsDir");
                        return;
                    }
                    FileInputStream fileInputStream = new FileInputStream(file);
                    byte[] bytes = new byte[8*1024];
                    int len;
                    while ((len = fileInputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, len); //transfer to server
                    }

                    outputStream.close();
                    fileInputStream.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "start.download.file":
                try {
                    String testPath = "userDownloads/";
                    String filename = (String) message.get("filename");
                    File file = new File(testPath + filename);
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    Socket socket = new Socket(host, Math.toIntExact((Long) message.get("port")));
                    InputStream inputStream = socket.getInputStream();
                    byte[] bytes = new byte[8*1024];
                    int len;
                    while ((len = inputStream.read(bytes)) != -1) {
                        fileOutputStream.write(bytes, 0, len); //receive file from server
                    }

                    fileOutputStream.close();
                    inputStream.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void sendErrorCode(String errorCode) {
        JSONObject query = new JSONObject();
        query.put("errorCode", errorCode);
        sendMessage(query);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.channel().close();
        System.out.println("Отключились от сервера!");
        UserDevice.getInstance().run();
    }

    public void sendMessage(JSONObject message) {
        if (!ctx.channel().isActive()) System.out.println("Нет соединения с сервером!");
        else {
                System.out.println("Server << " + message);
                ctx.writeAndFlush(message);
        }
    }
}