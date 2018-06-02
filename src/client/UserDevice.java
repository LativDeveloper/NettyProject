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

import java.util.ArrayList;
import java.util.Scanner;

public class UserDevice {
    private static UserDevice userDevice;
    private String host;
    private int port;
    private UserDeviceHandler userDeviceHandler;
    private Scanner in;

    public UserDevice(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static UserDevice getInstance() {
        return userDevice;
    }

    public UserDeviceHandler getUserDeviceHandler() {
        return userDeviceHandler;
    }

    public void setUserDeviceHandler(UserDeviceHandler userDeviceHandler) {
        this.userDeviceHandler = userDeviceHandler;
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
                    UserDeviceHandler userDeviceHandler = new UserDeviceHandler();
                    userDevice.setUserDeviceHandler(userDeviceHandler);
                    ch.pipeline().addLast(
                            new RequestEncoder(),
                            new ResponseDecoder(),
                            userDeviceHandler);
                }
            });

            ChannelFuture f = bootstrap.connect(host, port).sync();
            waitInputConsole();
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            //e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            try {
                int mills = 5000;
                System.out.println("Нет соединения с сервером! Ожидаем "+mills/1000+" сек....");
                Thread.sleep(mills);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            run();
        }
    }

    public void waitInputConsole() {
        System.out.println("Для отправки запроса на сервер воспользуйтесь шаблоном:");
        System.out.println("key1:value1:key1:value1 - будет преобразовано в JSONObject и отправлено на сервер.");
        System.out.println("Ожидаем пользовательский ввод...");
        in = new Scanner(System.in);
        String input;

        while ((input = in.nextLine()) != null) {
            if (userDeviceHandler == null) System.out.println("Соединение не установлено!");
            try {
                String[] params = input.split(":");
                if (params.length == 0 || params.length % 2 != 0) System.out.println("Неверный ввод! (см. key:value)");
                else
                    receiveInputConsole(params);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Запрос должен быть формата JSON!");
            }
        }
    }

    private void receiveInputConsole(String[] params) {
        JSONObject response = new JSONObject();
        ArrayList keys = new ArrayList();
        keys.add("count");
        keys.add("id");
        keys.add("seconds");
        for (int i = 0; i < params.length; i += 2) {
            if (keys.contains(params[i])) response.put(params[i], Integer.parseInt(params[i+1]));
            else response.put(params[i], params[i+1]);
        }
        if (response.containsKey("action")) {
            if (response.get("action").equals("set.login.ips")) {
                JSONArray ips = new JSONArray();
                ips.add("111.222.333.444:1111");
                ips.add("444.333.222.111:1121");
                response.put("ips", ips);
            }
        }

        userDeviceHandler.sendMessage(response);
    }

    public static void main(String[] args) throws Exception {
        userDevice = new UserDevice("localhost", 1121);
        userDevice.run();
    }
}