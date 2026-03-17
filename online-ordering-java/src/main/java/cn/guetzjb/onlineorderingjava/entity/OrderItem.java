package cn.guetzjb.onlineorderingjava.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(indexes = {
        @Index(name = "idx_order", columnList = "order_id")
})
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer count;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(columnDefinition = "JSON COMMENT '规格组JSON'")
    private String specGroupJson;

    @ManyToOne
    @JoinColumn(name = "goods_id")
    @JsonIgnoreProperties({"orderItem", "specGroups", "category"})
    private Goods goods;

    // 多对一关联订单
    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnoreProperties({"orderItem", "table", "sysUser"})
    private Orders order;

}
