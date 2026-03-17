package cn.guetzjb.onlineorderingjava.service;

import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProducerService {

    private final RocketMQTemplate rocketMQTemplate;

    @Value("${rocketmq.producer.send-message-timeout:30000}")
    private int sendTimeout;

    // 同步发送消息 - 支持任意对象
    public <T> void sendMessage(String topic, T message) {
        rocketMQTemplate.convertAndSend(topic, message);
    }

    // 同步发送消息 - 带Tag
    public <T> void sendMessage(String topic, String tag, T message) {
        String destination = topic + ":" + tag;
        rocketMQTemplate.convertAndSend(destination, message);
    }

    // 异步发送消息 - 支持任意对象
    public <T> void sendAsyncMessage(String topic, T message) {
        rocketMQTemplate.asyncSend(topic, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                System.out.println("发送成功: " + sendResult);
            }

            @Override
            public void onException(Throwable e) {
                System.err.println("发送失败: " + e.getMessage());
            }
        });
    }

    // 异步发送消息 - 带Tag和自定义回调
    public <T> void sendAsyncMessage(String topic, String tag, T message, SendCallback callback) {
        String destination = topic + ":" + tag;
        rocketMQTemplate.asyncSend(destination, message, callback);
    }

    // 发送延迟消息 - 支持任意对象
    public <T> void sendDelayMessage(String topic, T message, int delayLevel) {
        try {
            rocketMQTemplate.syncSend(topic,
                    MessageBuilder.withPayload(message).build(),
                    sendTimeout,
                    delayLevel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 发送延迟消息 - 带Tag
    public <T> void sendDelayMessage(String topic, String tag, T message, int delayLevel) {
        String destination = topic + ":" + tag;
        rocketMQTemplate.syncSend(destination,
                MessageBuilder.withPayload(message).build(),
                sendTimeout,
                delayLevel);
    }

    // 发送顺序消息 - 支持任意对象
    public <T> void sendOrderlyMessage(String topic, T message, String hashKey) {
        rocketMQTemplate.syncSendOrderly(topic, message, hashKey);
    }

    // 发送单向消息（不关心结果）- 支持任意对象
    public <T> void sendOneWayMessage(String topic, T message) {
        rocketMQTemplate.sendOneWay(topic, message);
    }
}
