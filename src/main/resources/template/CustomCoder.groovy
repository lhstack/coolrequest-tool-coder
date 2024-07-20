import org.apache.commons.codec.binary.Hex

import java.nio.charset.StandardCharsets

//注册一个从text转换成hex的函数
coder.registry("text", "hex", s -> {
    try {
        //这个会输出到插件日志面板
        sysLog.info("插件日志")
        return Hex.encodeHexString(s.getBytes(StandardCharsets.UTF_8))
    } catch (Throwable e) {
        log.error("转换失败,错误信息: ${e.getMessage()}")
    }
})
//获取环境配置中key=env1的值,如果获取不到,取默认值,环境配置可点击环境按钮设置
log.info(env.getOrDefault("env1","测试"))
//log会输出到右侧面板,插件启动加载会输出到插件日志面板
log.info("注册函数: text to hex")
log.warn("注册函数: text to hex")
log.error("注册函数: text to hex")
sysLog.info("插件日志")
