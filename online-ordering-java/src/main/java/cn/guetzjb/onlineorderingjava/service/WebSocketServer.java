package cn.guetzjb.onlineorderingjava.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.guetzjb.onlineorderingjava.domain.Message;
import com.alibaba.fastjson2.JSONObject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ServerEndpoint(value = "/mini_order_ws/{token}")
@Slf4j
@Component
public class WebSocketServer {

    //在线客户端集合
    public static final Map<String, Session> onlineSessionClientMap = new ConcurrentHashMap<>();
    private ScheduledExecutorService heartBeatExecutor;

    /**
     * 连接创建成功
     *
     * @param token
     * @param session
     */
    @OnOpen
    public void onOpen(@PathParam("token") String token, Session session) throws IOException {
        String id = StpUtil.getLoginIdByToken(token).toString();
        log.info("连接成功:{}", id);
        onlineSessionClientMap.put(id, session);
    }

    /**
     * 连接关闭回调
     *
     * @param id
     * @param session
     */
    @OnClose
    public void onClose(@PathParam("id") String id, Session session) {
        //从map集合中移除
        log.info("断开连接:{}", id);
        onlineSessionClientMap.remove(id);
    }

    /**
     * 收到消息后的回调
     *
     * @param message
     * @param session
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        if ("pong".equals(message)) {
            return;
        }
        Message msg = JSONObject.parseObject(message, Message.class);
        if (msg != null && msg.getTo() != null) {
            Session fromSession = onlineSessionClientMap.get(msg.getFrom());
            if (fromSession != null) {
                // 前端会通过JSON.parse解析message
                fromSession.getAsyncRemote().sendText(message);
            }
            Session toSession = onlineSessionClientMap.get(msg.getTo());
            if (toSession != null) {
                toSession.getAsyncRemote().sendText(message);
            }
        }
    }

    /**
     * 发生错误时的回调
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("websocket发生错误:{}", error.toString());
    }

}
