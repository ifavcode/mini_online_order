package cn.guetzjb.onlineorderingjava.repository;

import cn.guetzjb.onlineorderingjava.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<SysUser, Integer> {

    SysUser findByUsername(String username);

    SysUser findByOpenid(String openid);

    @Query("SELECT u.permission FROM SysUser u WHERE u.userId = :userId")
    String findPermissionByUserId(Long userId);

}
