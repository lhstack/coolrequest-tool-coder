package dev.coolrequest.tool.coder.encoder;

import dev.coolrequest.tool.coder.Kind;

public class DecToHexEncoder implements Encoder {
    @Override
    public String encode(String data) {
        try {
            return Long.toHexString(Long.parseLong(data)).toUpperCase();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public Kind kind() {
        return Kind.of("dec", "hex");
    }
}
