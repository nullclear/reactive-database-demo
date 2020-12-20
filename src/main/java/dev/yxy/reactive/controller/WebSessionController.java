package dev.yxy.reactive.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ZeroCopyHttpOutputMessage;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * exchange和webSession都可以操作Session
 */
@Controller
public class WebSessionController {

    private static final Logger logger = LoggerFactory.getLogger(WebSessionController.class);

    @RequestMapping("/")
    @ResponseBody
    Mono<String> home(WebSession webSession, @CookieValue(name = "SESSION", defaultValue = "") String cookie) {
        //可以通过@CookieValue获取浏览器请求的SessionID
        logger.info("本次访问的SessionID:[{}]", cookie);//基于浏览器，如果浏览器没有就是没有
        logger.info("本次访问的WebSessionID:[{}]", webSession.getId());//假如浏览器没有，会生成一个，所以一定有
        //获取session里的属性
        logger.info("showTime:[{}]", webSession.getAttributeOrDefault("showTime", 0L).toString());
        logger.info("加密通话:[{}]", webSession.getAttributeOrDefault("加密通话", ""));
        //如果并不存在会话，创建新会话
        if (!webSession.isStarted()) {
            webSession.start();
        }
        return Mono.just("[everything] is ok");
    }

    @RequestMapping("/save")
    @ResponseBody
    Mono<String> save(ServerWebExchange exchange, WebSession webSession) {
        //操作Session改变Attribute
        webSession.getAttributes().put("加密通话", "歪比歪比, 歪比巴卜");
        //单独设置Session存活时间
        webSession.setMaxIdleTime(Duration.ofSeconds(600));
        return exchange.getSession().map(session -> {
            long millis = System.currentTimeMillis();
            if ((millis & 1) == 1) {
                session.getAttributes().put("showTime", millis);
            } else {
                session.getAttributes().remove("showTime");
            }
            return session;
        }).thenReturn("[save] attribute");
    }

    //转变session的id
    @RequestMapping("/change")
    @ResponseBody
    Mono<String> change(WebSession webSession) {
        return webSession.changeSessionId().thenReturn("[change] session id");
    }

    //废弃session的id
    @RequestMapping("/invalidate")
    @ResponseBody
    Mono<String> invalidate(WebSession webSession) {
        //sessionID无效化，不需要经过Mono
        return webSession.invalidate().thenReturn("[invalidate] session id");
    }

    //可以将session转为cookie
    @RequestMapping("/transform")
    @ResponseBody
    Mono<String> transform(ServerWebExchange exchange, @CookieValue(name = "SESSION", defaultValue = "") String cookie) {
        if (cookie.isBlank()) {
            return Mono.just("no session");
        } else {
            exchange.getResponse().addCookie(ResponseCookie.from("SESSION", cookie).maxAge(Duration.ofDays(7)).path("/").build());
            return exchange.getSession().map(session -> {
                session.setMaxIdleTime(Duration.ofDays(7));
                return session;
            }).thenReturn("[transform] session to cookie");
        }
    }

    //下载文件
    @RequestMapping("/download")
    Mono<Void> download(ServerHttpResponse response) {
        URL url = WebSessionController.class.getResource("/application.yml");
        return Mono.just(new File(url.getFile())).flatMap(file -> {
            ZeroCopyHttpOutputMessage zero = (ZeroCopyHttpOutputMessage) response;
            zero.getHeaders().setContentType(MediaType.APPLICATION_OCTET_STREAM);
            zero.getHeaders().setContentDisposition(ContentDisposition.parse("attachment;filename=" + new String(file.getName().getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1)));
            zero.getHeaders().setContentLength(file.length());
            return zero.writeWith(DataBufferUtils.readInputStream(() -> new FileInputStream(file), new DefaultDataBufferFactory(), 64));
        });
    }

    //上传文件
    @PostMapping("/upload")
    @ResponseBody
    Mono<String> upload(@RequestPart(name = "file") FilePart filePart) {
        return DataBufferUtils.join(filePart.content()).map(dataBuffer -> {
            try (InputStream is = dataBuffer.asInputStream(true);
                 ByteArrayOutputStream bos = new ByteArrayOutputStream(1024)) {
                int count;
                byte[] buff = new byte[1024];
                while ((count = is.read(buff)) != -1) {
                    bos.write(buff, 0, count);
                }
                bos.flush();
                String s = new String(bos.toByteArray(), StandardCharsets.UTF_8);
                System.out.println("#### " + s);
                return "success";
            } catch (IOException e) {
                return "failure";
            }
        });
    }
}
