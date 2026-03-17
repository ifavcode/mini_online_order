package cn.guetzjb.onlineorderingjava.utils;

import cn.dev33.satoken.stp.StpUtil;
import cn.guetzjb.onlineorderingjava.domain.Message;
import cn.guetzjb.onlineorderingjava.service.WebSocketServer;
import com.alibaba.fastjson2.JSON;
import jakarta.websocket.Session;

import java.util.Map;

public class WebSocketUtil {

    public static void sendAll(String msg) {
        for (Map.Entry<String, Session> entry : WebSocketServer.onlineSessionClientMap.entrySet()) {
            // 5 —— sys_user
            Message message = Message.builder().from("5").content(msg).to(entry.getKey()).build();
            Session session = entry.getValue();
            session.getAsyncRemote().sendText(JSON.toJSONString(message));
        }
    }

    public static void sendAllNeedLogin(String msg) {
        for (Map.Entry<String, Session> entry : WebSocketServer.onlineSessionClientMap.entrySet()) {
            Message message = Message.builder().from(StpUtil.getLoginIdAsString()).content(msg).to(entry.getKey()).build();
            Session session = entry.getValue();
            session.getAsyncRemote().sendText(JSON.toJSONString(message));
        }
    }
}
