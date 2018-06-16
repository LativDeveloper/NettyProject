package encoders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.json.simple.JSONObject;

import java.nio.charset.Charset;

public class RequestEncoder extends MessageToByteEncoder<JSONObject> {

    private final Charset charset = Charset.forName("UTF-8");

    @Override
    protected void encode(ChannelHandlerContext ctx, JSONObject msg, ByteBuf out) throws Exception {
//        System.out.println("Request encoder: " + msg);
        String json = msg.toJSONString();
//        System.out.println("Packet length: " + json.getBytes(charset).length);
        out.writeInt(json.getBytes(charset).length);
        out.writeCharSequence(json, charset);
    }
}