package cn.guetzjb.onlineorderingjava.service;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import cn.guetzjb.onlineorderingjava.entity.SysUser;
import cn.guetzjb.onlineorderingjava.entity.dto.SysUserDTO;
import cn.guetzjb.onlineorderingjava.repository.UserRepository;
import cn.guetzjb.onlineorderingjava.utils.NullAwareBeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final NullAwareBeanUtils beanUtils;

    @Transactional
    public Map<String, Object> handleWxLogin(String openid) {
        SysUser sysUser = createUser(openid);
        StpUtil.login(sysUser.getUserId());
        String accessToken = StpUtil.getTokenValue();
        SysUserDTO dto = new SysUserDTO();
        beanUtils.copyProperties(sysUser, dto);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("accessToken", accessToken);
        result.put("data", dto);
        return result;
    }

    public boolean checkPassword(String rawPassword, String encryptedPassword) {
        return BCrypt.checkpw(rawPassword, encryptedPassword);
    }

    public SysUser createUser(String openid) {
        SysUser user = userRepository.findByOpenid(openid);
        if (user == null) {
            SysUser sysUser = new SysUser();
            sysUser.setOpenid(openid);
            sysUser.setNickname("未命名用户");
            sysUser.setStatus('1');
            sysUser.setPermission("");
            userRepository.save(sysUser);
            return sysUser;
        }
        return user;
    }
}
