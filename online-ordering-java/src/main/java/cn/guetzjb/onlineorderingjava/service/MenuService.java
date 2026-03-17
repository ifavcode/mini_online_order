package cn.guetzjb.onlineorderingjava.service;

import cn.guetzjb.onlineorderingjava.domain.ElementRoute;
import cn.guetzjb.onlineorderingjava.domain.Meta;
import cn.guetzjb.onlineorderingjava.entity.SysMenu;
import cn.guetzjb.onlineorderingjava.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;

    public List<SysMenu> getMenuTree() {
        List<SysMenu> menuList = menuRepository.findAll();
        return handle(menuList);
    }

    private List<SysMenu> handle(List<SysMenu> menuList) {
        Map<Long, SysMenu> map = new HashMap<>();
        for (SysMenu menu : menuList) {
            map.put(menu.getId(), menu);
        }
        List<SysMenu> result = new ArrayList<>();
        for (SysMenu sysMenu : menuList) {
            if (sysMenu.getParentId() != 0) {
                SysMenu father = map.get(sysMenu.getParentId());
                father.getChildren().add(sysMenu);
            } else {
                result.add(sysMenu);
            }
        }
        return result;
    }

    public List<ElementRoute> transformMenuTree(List<SysMenu> menuList) {
        if (menuList == null) {
            return new ArrayList<>();
        }
        List<ElementRoute> result = new ArrayList<>();
        for (SysMenu sysMenu : menuList) {
            ElementRoute elementRoute = new ElementRoute();
            elementRoute.setName(sysMenu.getMenuName());
            elementRoute.setPath(sysMenu.getRoutePath());
            elementRoute.setMeta(new Meta(sysMenu.getIcon(), sysMenu.getMenuName(), null));
            elementRoute.setHidden(sysMenu.getHidden());
            elementRoute.setComponent(sysMenu.getComponent());
            if (sysMenu.getParentId() != 0) {
                elementRoute.setShowLink(true);
            } else {
                elementRoute.setAlwaysShow(true);
            }
            if (sysMenu.getChildren().size() == 1) {
                elementRoute.getMeta().setShowParent(true);
            }
            List<ElementRoute> children = transformMenuTree(sysMenu.getChildren());
            elementRoute.setChildren(children);
            result.add(elementRoute);
        }
        return result;
    }
}
