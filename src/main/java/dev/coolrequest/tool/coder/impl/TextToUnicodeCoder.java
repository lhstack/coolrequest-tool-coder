package dev.coolrequest.tool.coder.impl;

import dev.coolrequest.tool.coder.Coder;
import dev.coolrequest.tool.coder.Kind;

import java.nio.charset.StandardCharsets;

public class TextToUnicodeCoder implements Coder {
    @Override
    public String transform(String data) {
        byte[] bytes = data.getBytes(StandardCharsets.UTF_16BE);
        return bytesToUnicode(bytes);
    }

    private static String bytesToUnicode(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("\\u%04X", b & 0xff));
        }
        return sb.toString();
    }

    @Override
    public Kind kind() {
        return Kind.of("text", "unicode");
    }
}
