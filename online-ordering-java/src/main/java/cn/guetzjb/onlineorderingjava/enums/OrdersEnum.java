package cn.guetzjb.onlineorderingjava.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrdersEnum {

    PENDING_PAYMENT(1, "待支付"),

    PAID(2, "已支付"),

    COMPLETED(3, "已完成"),

    CANCELED(4, "已取消"),

    REFUNDED(5, "已退款");

    private final Integer code;

    private final String desc;
}
