package cn.guetzjb.onlineorderingjava.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = false, length = 32)
    private String orderNo;

    @Column(nullable = false, unique = false, length = 52)
    private String wxOrderNo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, columnDefinition = "VARCHAR(10)")
    private String orderType;

    @Column(nullable = false, columnDefinition = "VARCHAR(10)")
    private String pickupTime;

    @Column(columnDefinition = "VARCHAR(30)")
    private String phone;

    @Column
    private String remark;

    @Column(columnDefinition = "TINYINT DEFAULT 1 COMMENT '1待支付 2已支付 3已完成 4已取消'")
    private Integer status = 1;

    @CreationTimestamp
    @Column(updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;

    // 一对多关联订单明细
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnoreProperties({"order"})
    private List<OrderItem> orderItems;

    @ManyToOne
    @JsonIgnoreProperties({"orders"})
    private Table table;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"ordersList"})
    private SysUser sysUser;
}
