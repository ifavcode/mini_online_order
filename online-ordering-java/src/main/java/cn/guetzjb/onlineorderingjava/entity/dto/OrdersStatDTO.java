package cn.guetzjb.onlineorderingjava.entity.dto;

import cn.guetzjb.onlineorderingjava.entity.OrderItem;
import cn.guetzjb.onlineorderingjava.entity.Orders;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrdersStatDTO {

    private Integer id;

    private String orderNo;

    private String wxOrderNo;

    private BigDecimal totalAmount;

    private String orderType;

    private String pickupTime;

    private String phone;

    private String remark;

    private Integer status = 1;

    private LocalDateTime createdAt;

    private LocalDateTime payTime;

    private List<OrderItemStatDTO> orderItem;

    public static OrdersStatDTO transOrders(Orders orders) {
        OrdersStatDTO ordersDTO = OrdersStatDTO.builder()
                .id(orders.getId())
                .orderNo(orders.getOrderNo())
                .wxOrderNo(orders.getWxOrderNo())
                .totalAmount(orders.getTotalAmount())
                .orderType(orders.getOrderType())
                .pickupTime(orders.getPickupTime())
                .phone(orders.getPhone())
                .remark(orders.getRemark())
                .status(orders.getStatus())
                .createdAt(orders.getCreatedAt())
                .payTime(orders.getPayTime())
                .orderItem(new ArrayList<>())
                .build();
        for (OrderItem orderItem : orders.getOrderItems()) {
            OrderItemStatDTO orderItemDTO = OrderItemStatDTO.builder().id(orderItem.getId())
                    .unitPrice(orderItem.getUnitPrice())
                    .count(orderItem.getCount())
                    .subtotal(orderItem.getSubtotal())
                    .specGroupJson(orderItem.getSpecGroupJson())
                    .goods(GoodsDTO.transGoods(orderItem.getGoods()))
                    .build();
            ordersDTO.getOrderItem().add(orderItemDTO);
        }
        return ordersDTO;
    }

}
