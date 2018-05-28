package decoders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.nio.charset.Charset;
import java.util.List;

public class ResponseDecoder extends ReplayingDecoder<JSONObject> {

    private final Charset charset = Charset.forName("UTF-8");

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //System.out.println("Response decoder: " + in.toString());
        int length = in.readInt();
        String json = in.readCharSequence(length, charset).toString();
        JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
        out.add(jsonObject);
    }
}