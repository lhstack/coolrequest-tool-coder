package dev.coolrequest.tool.views.coder.impl;

import dev.coolrequest.tool.views.coder.Coder;
import dev.coolrequest.tool.views.coder.Kind;
import org.apache.commons.codec.binary.Base64;

public class TextToBase64Coder implements Coder {
    @Override
    public String transform(String data) {
        return Base64.encodeBase64String(data.getBytes());
    }

    @Override
    public Kind kind() {
        return Kind.of("text", "base64");
    }

    @Override
    public int ordered() {
        return Integer.MIN_VALUE;
    }
}
