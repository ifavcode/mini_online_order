package cn.guetzjb.onlineorderingjava.handle;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        List<String> permissionList = (List<String>) StpUtil.getSessionByLoginId(loginId).get("permissions");
        return permissionList != null ? permissionList : new ArrayList<>();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 角色列表
        return new ArrayList<>();
    }
}
