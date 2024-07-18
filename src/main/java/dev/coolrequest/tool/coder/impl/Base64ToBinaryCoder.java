package dev.coolrequest.tool.coder.impl;

import dev.coolrequest.tool.coder.Coder;
import dev.coolrequest.tool.coder.Kind;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.BinaryCodec;

public class Base64ToBinaryCoder implements Coder {
    @Override
    public String transform(String data) {
        try {
            return BinaryCodec.toAsciiString(Base64.decodeBase64(data));
        } catch (Throwable e) {
            return "base64 transform to binary fail,error: " + e.getMessage();
        }
    }

    @Override
    public Kind kind() {
        return Kind.of("base64", "binary");
    }
}
