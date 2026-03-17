package cn.guetzjb.onlineorderingjava.consumer;

import cn.guetzjb.onlineorderingjava.constant.MqTopicConstant;
import cn.guetzjb.onlineorderingjava.enums.OrdersEnum;
import cn.guetzjb.onlineorderingjava.repository.OrdersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(
        topic = MqTopicConstant.ORDER_TIMEOUT,
        consumerGroup = "order-consumer-group"
)
@RequiredArgsConstructor
@Slf4j
public class OrderTimeoutConsumer implements RocketMQListener<Integer> {

    private final OrdersRepository ordersRepository;

    @Override
    public void onMessage(Integer orderId) {
        ordersRepository.findById(orderId).map(v -> {
            if (v.getStatus().equals(OrdersEnum.PENDING_PAYMENT.getCode())) {
                log.info("订单序号{},支付超时", orderId);
                v.setStatus(OrdersEnum.CANCELED.getCode());
                ordersRepository.save(v);
            }
            if (v.getStatus().equals(OrdersEnum.PAID.getCode())) {
                log.info("订单序号{},商家处理超时,自动设置订单完成", orderId);
                v.setStatus(OrdersEnum.COMPLETED.getCode());
                ordersRepository.save(v);
            }
            return v;
        });
    }
}
