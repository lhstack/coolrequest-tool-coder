package dev.coolrequest.tool.coder.encoder;

import dev.coolrequest.tool.coder.Kind;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TextToMD532Encoder implements Encoder {
    @Override
    public String encode(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(data.getBytes());
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException ignored) {
        }
        return "error";
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    @Override
    public Kind kind() {
        return Kind.of("text", "md5");
    }
}
