package dev.coolrequest.tool.coder.impl;

import dev.coolrequest.tool.coder.Coder;
import dev.coolrequest.tool.coder.Kind;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TextToUrlCoder implements Coder {
    @Override
    public String transform(String data) {
        return URLEncoder.encode(data, StandardCharsets.UTF_8);
    }

    @Override
    public Kind kind() {
        return Kind.of("text", "url");
    }
}
