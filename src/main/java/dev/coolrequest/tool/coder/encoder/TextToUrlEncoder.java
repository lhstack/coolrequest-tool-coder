package dev.coolrequest.tool.coder.encoder;

import dev.coolrequest.tool.coder.Kind;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TextToUrlEncoder implements Encoder {
    @Override
    public String encode(String data) {
        return URLEncoder.encode(data, StandardCharsets.UTF_8);
    }

    @Override
    public Kind kind() {
        return Kind.of("text", "url");
    }
}
