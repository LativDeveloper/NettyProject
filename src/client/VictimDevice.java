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

import java.util.Scanner;

public class VictimDevice {
    private static VictimDevice victimDevice;
    private String host;
    private int port;
    private VictimDeviceHandler victimDeviceHandler;
    private Scanner in;

    public VictimDevice(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static VictimDevice getInstance() {
        return victimDevice;
    }

    public VictimDeviceHandler getVictimDeviceHandler() {
        return victimDeviceHandler;
    }

    public void setVictimDeviceHandler(VictimDeviceHandler victimDeviceHandler) {
        this.victimDeviceHandler = victimDeviceHandler;
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
                    VictimDeviceHandler victimDeviceHandler = new VictimDeviceHandler();
                    victimDevice.setVictimDeviceHandler(victimDeviceHandler);
                    ch.pipeline().addLast(
                            new RequestEncoder(),
                            new ResponseDecoder(),
                            victimDeviceHandler);
                }
            });

            ChannelFuture f = bootstrap.connect(host, port).sync();
            waitInputConsole();
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            try {
                int mills = 5000;
                System.out.println("Нет соединения с сервером! Ожидаем "+mills/1000+" сек....");
                Thread.sleep(mills);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            run();
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
            if (victimDeviceHandler == null) System.out.println("Соединение не установлено!");
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
        victimDeviceHandler.sendMessage(response);
    }

    public static void main(String[] args) throws Exception {
        victimDevice = new VictimDevice("localhost", 1121);
        victimDevice.run();
    }
}