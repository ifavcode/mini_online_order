package cn.guetzjb.onlineorderingjava.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SysUserDTO {

    private Integer userId;

    private String username;

    private String nickname;

    private String email;

    private String phoneNumber;

    private Character sex;

    private String openid;

    private String avatar;

    private String permission; // 权限字符——多个使用逗号(,)分隔，*最高权限
}
