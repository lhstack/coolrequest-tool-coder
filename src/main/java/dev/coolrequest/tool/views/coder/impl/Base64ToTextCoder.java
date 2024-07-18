package dev.coolrequest.tool.views.coder.impl;

import dev.coolrequest.tool.views.coder.Coder;
import dev.coolrequest.tool.views.coder.Kind;
import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;

public class Base64ToTextCoder implements Coder {
    @Override
    public String transform(String data) {
        try {
            return new String(Base64.decodeBase64(data), StandardCharsets.UTF_8);
        } catch (Throwable e) {
            return "base64 transform to text fail,error: " + e.getMessage();
        }
    }

    @Override
    public Kind kind() {
        return Kind.of("base64", "text");
    }

    @Override
    public int ordered() {
        return 1;
    }
}
