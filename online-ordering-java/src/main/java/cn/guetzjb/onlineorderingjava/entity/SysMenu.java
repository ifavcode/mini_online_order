package cn.guetzjb.onlineorderingjava.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SysMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String menuName;

    private Long parentId;

    @Comment("路由名称")
    private String routeName;

    @Comment("路由路径")
    private String routePath;

    @Comment("排序，越小越靠前")
    private Integer orderNum;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private String icon;

    private Boolean hidden;

    private String component;

    @Transient
    private List<SysMenu> children = new ArrayList<SysMenu>();
}
