package dev.coolrequest.tool.coder.encoder;

import dev.coolrequest.tool.coder.Kind;

import java.nio.charset.StandardCharsets;

public class TextToUnicodeEncoder implements Encoder {
    @Override
    public String encode(String data) {
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
