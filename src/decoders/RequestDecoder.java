package decoders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.nio.charset.Charset;
import java.util.List;

public class RequestDecoder extends ReplayingDecoder<JSONObject> {

    private final Charset charset = Charset.forName("UTF-8");

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        //System.out.println("Request decoder: " + in.toString());
        int length = in.readInt();
        int i = 0;
        String json = "";
        while (i < length) {
            json += in.readCharSequence(length-i, charset).toString();
            i = json.length();
        }
        JSONObject jsonObject = null;

        try {
            jsonObject = (JSONObject) new JSONParser().parse(json);
            out.add(jsonObject);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // TODO: 01.06.2018 fix: incorrectly handle Russian symbols
    }
}