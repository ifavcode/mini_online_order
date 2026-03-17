package cn.guetzjb.onlineorderingjava.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class SpecGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "goods_id", nullable = false)
    private Integer goodsId;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(10)")
    private SelectType selectType;

    @Column
    private Integer sortOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"specGroups"})
    private Goods goods;

    @OneToMany(mappedBy = "specGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"specGroup"})
    private List<SpecItem> specItems;

    // 枚举类型
    public enum SelectType {
        one,   // 单选
        more   // 多选
    }

}
