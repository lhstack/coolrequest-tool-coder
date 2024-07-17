package dev.coolrequest.tool.coder.encoder;

import dev.coolrequest.tool.coder.Kind;

import java.util.Base64;

public class TextToBase64Encoder implements Encoder {
    @Override
    public String encode(String data) {
        return Base64.getEncoder().encodeToString(data.getBytes());
    }

    @Override
    public Kind kind() {
        return Kind.of("text", "base64");
    }
}
