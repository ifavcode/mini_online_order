package cn.guetzjb.onlineorderingjava.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.guetzjb.onlineorderingjava.domain.UserInfo;
import cn.guetzjb.onlineorderingjava.entity.SysUser;
import cn.guetzjb.onlineorderingjava.repository.UserRepository;
import cn.guetzjb.onlineorderingjava.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequiredArgsConstructor
public class LoginController {

    private final UserRepository userRepository;
    private final UserService userService;

    @Value("${sa-token.timeout}")
    private int expireTime;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody UserInfo userInfo) throws Exception {
        SysUser sysUser = userRepository.findByUsername(userInfo.getUsername());
        if (sysUser == null) {
            throw new Exception("用户名不存在");
        }
        if (!userService.checkPassword(userInfo.getPassword(), sysUser.getPassword())) {
            throw new Exception("用户名或者密码错误");
        }
        if (sysUser.getStatus() == '0') {
            throw new Exception("账号被禁用，请联系管理员");
        }
        StpUtil.login(sysUser.getUserId());
        String accessToken = StpUtil.getTokenValue();

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        Map<String, Object> data = new HashMap<>();
        data.put("id", sysUser.getUserId());
        data.put("avatar", sysUser.getAvatar());
        data.put("username", sysUser.getUsername());
        data.put("nickname", sysUser.getNickname());

        List<String> permissions = Arrays.stream(sysUser.getPermission().split(",")).toList();
        data.put("permissions", permissions);
        StpUtil.getSession().set("permissions", permissions);

        data.put("accessToken", accessToken);
        data.put("refreshToken", "eyJhbGciOiJIUzUxMiJ9.adminRefresh");
        long curExpireTime = expireTime * 1000L;
        if (curExpireTime == -1) {
            curExpireTime = 60 * 24 * 365;
        }
        data.put("expires", new Date().getTime() + curExpireTime);

        result.put("data", data);
        return result;
    }

    @GetMapping("/logout")
    public boolean logout() {
        StpUtil.logout();
        return true;
    }
}
