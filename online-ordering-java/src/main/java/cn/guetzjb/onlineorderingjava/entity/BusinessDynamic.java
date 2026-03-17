package cn.guetzjb.onlineorderingjava.entity;

import cn.guetzjb.onlineorderingjava.converter.StringListConverter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class BusinessDynamic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "VARCHAR(1024)")
    private String description;

    @Column(columnDefinition = "VARCHAR(1024)")
    @Convert(converter = StringListConverter.class)
    private List<String> images;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private SysUser sysUser;

    @CreationTimestamp
    @Column(updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
