package dev.coolrequest.tool.coder.encoder;

import java.util.ArrayList;
import java.util.List;

public class Encoders {
    private static final List<Encoder> encoders = new ArrayList<>();

    static {
        encoders.add(new TextToBase64Encoder());
        encoders.add(new TextToMD532Encoder());
        encoders.add(new TextToUrlEncoder());
        encoders.add(new TextToUnicodeEncoder());
        encoders.add(new DecToHexEncoder());
    }

    public static List<Encoder> getEncoders() {
        return encoders;
    }
}
