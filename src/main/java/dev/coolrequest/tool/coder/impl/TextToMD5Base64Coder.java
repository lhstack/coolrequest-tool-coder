package dev.coolrequest.tool.coder.impl;

import dev.coolrequest.tool.coder.Kind;
import dev.coolrequest.tool.coder.Coder;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

public class TextToMD5Base64Coder implements Coder {
    @Override
    public String transform(String data) {
        return Base64.encodeBase64String(DigestUtils.md5(data));
    }

    @Override
    public Kind kind() {
        return Kind.of("text", "md5Base64");
    }
}
