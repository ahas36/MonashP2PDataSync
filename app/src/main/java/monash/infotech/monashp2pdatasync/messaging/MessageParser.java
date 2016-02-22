package monash.infotech.monashp2pdatasync.messaging;

import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;

import monash.infotech.monashp2pdatasync.utils.Compress;

/**
 * Created by john on 11/27/2015.
 */
public class MessageParser {
    public static Message parse(byte[] data)
    {
        String msg="";
        Gson gson=new Gson();
        Message message=null;
        try {
            msg = new String(data, StandardCharsets.UTF_8);
            message=gson.fromJson(msg,Message.class);
            return message;
        }
        catch (Exception e)
        {
            return Compress.unZip(data);
        }
    }

}
