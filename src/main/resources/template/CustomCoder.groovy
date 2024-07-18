import org.apache.commons.codec.binary.Hex

import java.nio.charset.StandardCharsets

//注册一个从text转换成hex的函数
coder.registry("text", "hex", s -> {
    try {
        sysLog.info("插件日志")
        return Hex.encodeHexString(s.getBytes(StandardCharsets.UTF_8))
    } catch (Throwable e) {
        log.error("转换失败,错误信息: ${e.getMessage()}")
    }
})

log.info("注册函数: text to hex")
log.warn("注册函数: text to hex")
log.error("注册函数: text to hex")
sysLog.info("插件日志")
