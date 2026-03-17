package cn.guetzjb.onlineorderingjava.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class Goods {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(length = 500)
    private String imgUrl;

    @Column
    private Integer hasSpec = 0;

    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer status = 1;

    @Column(columnDefinition = "INT DEFAULT -1 COMMENT '库存: -1无限'")
    private Integer stock = -1;

    @Column
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "category_id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_goods_menu_category"))
    @JsonIgnoreProperties({"goodsList"})
    private MenuCategory category;

    @OneToMany(mappedBy = "goods", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"goods"})
    private List<SpecGroup> specGroups;

    @OneToMany(mappedBy = "goods", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<OrderItem> orderItem;
}
