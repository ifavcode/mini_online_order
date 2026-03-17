package cn.guetzjb.onlineorderingjava.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SQLRestriction("del_flag = 0")
@Builder
public class SysUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    private String username;

    private String password;

    private String nickname;

    private String email;

    private String phoneNumber;

    private Character sex;

    private String openid;

    private String avatar;

    private String permission; // 权限字符——多个使用逗号(,)分隔，*最高权限

    private Character status; // 启动状态——0未启用、1已启用

    private Character delFlag = '0'; // 删除标记——0未删除、1已删除

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createAt;

    @OneToMany(mappedBy = "sysUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"sysUser"})
    private List<Address> addressList;

    @OneToMany(mappedBy = "sysUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"sysUser"})
    private List<Orders> ordersList;

    @OneToMany(mappedBy = "sysUser", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<BusinessDynamic> businessDynamicList;
}
