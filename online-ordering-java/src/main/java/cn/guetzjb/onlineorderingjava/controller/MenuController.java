package cn.guetzjb.onlineorderingjava.controller;

import cn.guetzjb.onlineorderingjava.domain.ElementRoute;
import cn.guetzjb.onlineorderingjava.entity.SysMenu;
import cn.guetzjb.onlineorderingjava.repository.MenuRepository;
import cn.guetzjb.onlineorderingjava.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RequestMapping("/menu")
@RestController
@RequiredArgsConstructor
public class MenuController {

    private final MenuRepository menuRepository;
    private final MenuService menuService;

    @GetMapping("/tree")
    public ResponseEntity<List<SysMenu>> tree() {
        List<SysMenu> menuTree = menuService.getMenuTree();
        menuTree.sort(Comparator.comparingInt(SysMenu::getOrderNum));
        return ResponseEntity.ok(menuTree);
    }

    @GetMapping("/list")
    public ResponseEntity<Page<SysMenu>> list(@PageableDefault(
            page = 0,
            size = 999,
            sort = "id",
            direction = Sort.Direction.ASC
    ) Pageable pageable) {
        return ResponseEntity.ok(menuRepository.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<SysMenu>> get(@PathVariable Long id) {
        return ResponseEntity.ok(menuRepository.findById(id));
    }

    @GetMapping("/getRoutes")
    public ResponseEntity<List<ElementRoute>> getRoutes() {
        List<SysMenu> menuTree = menuService.getMenuTree();
        menuTree.sort(Comparator.comparingInt(SysMenu::getOrderNum));
        List<ElementRoute> result = menuService.transformMenuTree(menuTree);
        Map<String, SysMenu> map = new HashMap<>();
        for (SysMenu sysMenu : menuTree) {
            map.put(sysMenu.getMenuName(), sysMenu);
        }
        result.sort(Comparator.comparingInt(o -> map.get(o.getName()).getOrderNum()));
        return ResponseEntity.ok(result);
    }

    @PostMapping()
    public ResponseEntity<Void> add(@RequestBody SysMenu sysMenu) {
        menuRepository.save(sysMenu);
        return ResponseEntity.ok().build();
    }

    @PutMapping()
    public ResponseEntity<Object> update(@RequestBody SysMenu sysMenu) {
        Optional<SysMenu> menu = menuRepository.findById(sysMenu.getId());
        if (menu.isPresent()) {
            return ResponseEntity.internalServerError().body("menu不存在");
        }
        menuRepository.save(sysMenu);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{ids}")
    public ResponseEntity<Void> delete(@PathVariable("ids") List<Long> ids) {
        menuRepository.deleteAllById(ids);
        return ResponseEntity.ok().build();
    }

}
