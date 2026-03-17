package cn.guetzjb.onlineorderingjava.service;

import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
public class WebSocketHeartbeatTask {

    // 心跳间隔，单位毫秒
    private static final long HEARTBEAT_INTERVAL = 30_000L;

    @Scheduled(fixedRate = HEARTBEAT_INTERVAL)
    public void sendHeartbeat() {
        Map<String, Session> sessionMap = WebSocketServer.onlineSessionClientMap;
        if (sessionMap.isEmpty()) {
            return;
        }

        sessionMap.forEach((id, session) -> {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText("ping");
                    log.debug("心跳发送成功，userId: {}", id);
                } catch (IOException e) {
                    log.warn("心跳发送失败，移除连接，userId: {}", id);
                    sessionMap.remove(id);
                    try {
                        session.close();
                    } catch (IOException ex) {
                        log.error("关闭session失败", ex);
                    }
                }
            } else {
                // session 已关闭，直接移除
                sessionMap.remove(id);
                log.info("清理已关闭的session，userId: {}", id);
            }
        });
    }
}