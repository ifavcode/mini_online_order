package cn.guetzjb.onlineorderingjava.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import cn.guetzjb.onlineorderingjava.entity.SysUser;
import cn.guetzjb.onlineorderingjava.entity.dto.SysUserDTO;
import cn.guetzjb.onlineorderingjava.repository.UserRepository;
import cn.guetzjb.onlineorderingjava.service.UserService;
import cn.guetzjb.onlineorderingjava.utils.NullAwareBeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final NullAwareBeanUtils beanUtils;

    @Value("${salt}")
    private String salt;

    @PostMapping
    @SaCheckPermission("system:user:add")
    public ResponseEntity<SysUser> createUser(@RequestBody SysUser user) {
        SysUser savedUser = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @GetMapping
    public ResponseEntity<List<SysUser>> getAllUsers() {
        List<SysUser> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/profile")
    public ResponseEntity<SysUserDTO> getUserProfile() {
        StpUtil.checkLogin();
        int userId = StpUtil.getLoginIdAsInt();
        SysUser user = userRepository.findById(userId).orElse(null);
        SysUserDTO dto = new SysUserDTO();
        beanUtils.copyProperties(user, dto);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<SysUser>> getUsersByPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) Character sex,
            @RequestParam(required = false) Character status,
            @RequestParam(required = false) String username) {
        pageNum -= 1;
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.ASC, "createAt"));
        SysUser probe = new SysUser();
        probe.setNickname(nickname);
        probe.setEmail(email);
        probe.setPhoneNumber(phoneNumber);
        probe.setSex(sex);
        probe.setStatus(status);
        probe.setUsername(username);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase()
                .withIgnoreNullValues();
        Example<SysUser> example = Example.of(probe, matcher);
        Page<SysUser> userPage = userRepository.findAll(example, pageable);
        return ResponseEntity.ok(userPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SysUser> getUserById(@PathVariable Integer id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @SaCheckPermission("system:user:update")
    public ResponseEntity<SysUser> updateUser(@PathVariable Integer id, @RequestBody SysUser userDetails) {
        return userRepository.findById(id)
                .map(user -> {
                    if (userDetails.getAvatar() != null && !userDetails.getAvatar().isEmpty()) {
                        user.setAvatar(userDetails.getAvatar());
                    }
                    if (userDetails.getEmail() != null && !userDetails.getEmail().isEmpty()) {
                        user.setEmail(userDetails.getEmail());
                    }
                    if (userDetails.getNickname() != null && !userDetails.getNickname().isEmpty()) {
                        user.setNickname(userDetails.getNickname());
                    }
                    if (userDetails.getSex() != null) {
                        user.setSex(userDetails.getSex());
                    }
                    if (userDetails.getPhoneNumber() != null && !userDetails.getPhoneNumber().isEmpty()) {
                        user.setPhoneNumber(userDetails.getPhoneNumber());
                    }
                    if (userDetails.getStatus() != null) {
                        user.setStatus(userDetails.getStatus());
                    }
                    if (userDetails.getPermission() != null) {
                        user.setPermission(userDetails.getPermission());
                    }
                    SysUser updatedUser = userRepository.save(user);
                    return ResponseEntity.ok(updatedUser);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SysUser> deleteUser(@PathVariable Integer id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping
    public ResponseEntity<SysUser> deleteUsers(@RequestBody List<Integer> ids) {
        userRepository.deleteAllById(ids);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resetPwd")
    @SaCheckPermission("system:user:delete")
    public ResponseEntity<Object> resetPwd(@RequestBody SysUser user) {
        if (!StringUtils.hasLength(user.getPassword())) {
            return ResponseEntity.internalServerError().body("请输入密码");
        }
        if (user.getUserId() == null) {
            return ResponseEntity.internalServerError().body("userId为空");
        }
        SysUser cur = userRepository.findById(user.getUserId()).map(v -> {
            v.setPassword(BCrypt.hashpw(user.getPassword(), salt));
            return v;
        }).get();
        userRepository.save(cur);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update")
    public ResponseEntity<SysUserDTO> updateUser(@RequestBody SysUserDTO dto) {
        dto.setUserId(StpUtil.getLoginIdAsInt());
        Optional<SysUser> byId = userRepository.findById(dto.getUserId());
        if (byId.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        SysUser user = byId.get();
        NullAwareBeanUtils.copyProperties(dto, user);
        userRepository.save(user);
        SysUserDTO back = new SysUserDTO();
        beanUtils.copyProperties(user, back);
        return ResponseEntity.ok(back);
    }

}
