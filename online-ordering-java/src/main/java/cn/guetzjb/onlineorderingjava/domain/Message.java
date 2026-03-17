package cn.guetzjb.onlineorderingjava.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message {

    // 发送方
    private String from;

    // 接收方
    private String to;

    // 消息内容
    private String content;
}

