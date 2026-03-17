package cn.guetzjb.onlineorderingjava.entity.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemStatDTO {

    private Integer id;

    private BigDecimal unitPrice;

    private Integer count;

    private BigDecimal subtotal;

    private String specGroupJson;

    private GoodsDTO goods;

}
