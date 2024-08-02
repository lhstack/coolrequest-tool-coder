import org.apache.commons.codec.binary.Hex

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
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


coder.registry("text", "aesCbC", (String s) -> {
    try {
        //这里环境变量使用的yaml格式
        def aes = projectEnv.getOrDefault("aes",globalEnv["aes"])
        String aesKey = aes.key
        String aesIv = aes.iv
        sysLog.info("aesKey: $aesKey,aesIv: $aesIv")
        def cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(aesKey.getBytes(StandardCharsets.UTF_8),"AES"),new IvParameterSpec(aesIv.getBytes(StandardCharsets.UTF_8)))
        def bytes = cipher.doFinal(s.getBytes(StandardCharsets.UTF_8))
        return Base64.getEncoder().encodeToString(bytes)
    } catch (Throwable e) {
        return e.getMessage()
    }
})

coder.registry("aesCbC", "text", (String s) -> {
    try {
        //这里环境变量使用的yaml格式
        def aes = projectEnv.getOrDefault("aes",globalEnv["aes"])
        String aesKey = aes.key
        String aesIv = aes.iv
        sysLog.info("aesKey: $aesKey,aesIv: $aesIv")
        def cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE,new SecretKeySpec(aesKey.getBytes(StandardCharsets.UTF_8),"AES"),new IvParameterSpec(aesIv.getBytes(StandardCharsets.UTF_8)))
        def bytes = cipher.doFinal(Base64.getDecoder().decode(s))
        return new String(bytes,StandardCharsets.UTF_8)
    } catch (Throwable e) {
        return e.getMessage()
    }
})

//获取项目环境配置中key=env1的值,如果获取不到,取默认值,环境配置可点击环境按钮设置
log.info(projectEnv.getOrDefault("env1","测试"))
//获取全局环境配置中key=env1的值,如果获取不到,取默认值,环境配置可点击环境按钮设置
log.info(globalEnv.getOrDefault("env1","测试"))
//log会输出到右侧面板,插件启动加载会输出到插件日志面板
log.info("注册函数: text to hex")
log.warn("注册函数: text to hex")
log.error("注册函数: text to hex")
sysLog.info("插件日志")
