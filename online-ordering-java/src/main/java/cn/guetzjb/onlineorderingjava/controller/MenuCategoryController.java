package cn.guetzjb.onlineorderingjava.controller;

import cn.guetzjb.onlineorderingjava.entity.MenuCategory;
import cn.guetzjb.onlineorderingjava.repository.MenuCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/menuCategory")
@RequiredArgsConstructor
public class MenuCategoryController {

    private final MenuCategoryRepository menuCategoryRepository;

    @PostMapping
    public ResponseEntity<MenuCategory> createMenuCategory(@RequestBody MenuCategory menuCategory) {
        MenuCategory saved = menuCategoryRepository.save(menuCategory);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuCategory> updateMenuCategory(@PathVariable Integer id, @RequestBody MenuCategory menuCategory) {
        return menuCategoryRepository.findById(id)
                .map(v -> {
                    menuCategory.setId(id);
                    MenuCategory updated = menuCategoryRepository.save(menuCategory);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenuCategory(@PathVariable Integer id) {
        return menuCategoryRepository.findById(id)
                .map(v -> {
                    menuCategoryRepository.delete(v);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuCategory> getMenuCategoryById(@PathVariable Integer id) {
        Optional<MenuCategory> menuCategory = menuCategoryRepository.findById(id);
        return menuCategory.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<MenuCategory>> getAllMenuCategory() {
        List<MenuCategory> list = menuCategoryRepository.findAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<MenuCategory>> getGMenuCategoryPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "sortOrder") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {

        Sort sort = direction.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<MenuCategory> menuCategories = menuCategoryRepository.findAll(pageable);

        return ResponseEntity.ok(menuCategories);
    }
}
