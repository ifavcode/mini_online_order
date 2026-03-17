package cn.guetzjb.onlineorderingjava.controller;

import cn.guetzjb.onlineorderingjava.entity.Goods;
import cn.guetzjb.onlineorderingjava.entity.MenuCategory;
import cn.guetzjb.onlineorderingjava.entity.dto.GoodsDTO;
import cn.guetzjb.onlineorderingjava.entity.dto.MenuCategoryDTO;
import cn.guetzjb.onlineorderingjava.repository.MenuCategoryRepository;
import cn.guetzjb.onlineorderingjava.repository.TableRepository;
import cn.guetzjb.onlineorderingjava.utils.NullAwareBeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final MenuCategoryRepository categoryRepository;
    private final NullAwareBeanUtils beanUtils;
    private final TableRepository tableRepository;

    @PostMapping
    public ResponseEntity<MenuCategory> createMenuCategory(@RequestBody MenuCategory menuCategory) {
        MenuCategory savedMenuCategory = categoryRepository.save(menuCategory);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMenuCategory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuCategory> updateMenuCategory(@PathVariable Integer id, @RequestBody MenuCategory menuCategory) {
        return categoryRepository.findById(id)
                .map(existingMenuCategory -> {
                    beanUtils.copyProperties(menuCategory, existingMenuCategory);
                    MenuCategory updatedMenuCategory = categoryRepository.save(existingMenuCategory);
                    return ResponseEntity.ok(updatedMenuCategory);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenuCategory(@PathVariable Integer id) {
        return categoryRepository.findById(id)
                .map(MenuCategory -> {
                    categoryRepository.delete(MenuCategory);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuCategory> getMenuCategoryById(@PathVariable Integer id) {
        Optional<MenuCategory> menuCategory = categoryRepository.findById(id);
        return menuCategory.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<MenuCategory>> getAllMenuCategoryDTO() {
        List<MenuCategory> menuCategoryList = categoryRepository.findAll();
        for (MenuCategory menuCategory : menuCategoryList) {
            List<Goods> list = menuCategory.getGoodsList().stream().filter(v -> v.getStatus() == 1).toList();
            menuCategory.setGoodsList(list);
        }
        return ResponseEntity.ok(menuCategoryList);
    }

    private MenuCategoryDTO convertToDTO(MenuCategory category) {
        MenuCategoryDTO dto = new MenuCategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setSortOrder(category.getSortOrder());
        dto.setCreatedAt(category.getCreatedAt());

        if (category.getGoodsList() != null) {
            TreeSet<GoodsDTO> goodsDTOSet = new TreeSet<>((o1, o2) -> {
                boolean o1HasStock = o1.getStock() != 0;
                boolean o2HasStock = o2.getStock() != 0;
                if (o1HasStock && !o2HasStock) {
                    return -1;
                }
                if (!o1HasStock && o2HasStock) {
                    return 1;
                }
                int compare = Integer.compare(o1.getSortOrder(), o2.getSortOrder());
                return compare != 0 ? compare : -1;
            });
            for (Goods goods : category.getGoodsList()) {
                if (goods.getStatus() == 0) {
                    continue;
                }
                GoodsDTO goodsDTO = new GoodsDTO();
                beanUtils.copyProperties(goods, goodsDTO);
                goodsDTOSet.add(goodsDTO);
            }
            dto.setGoodsList(goodsDTOSet);
        }
        return dto;
    }


    @GetMapping("/all")
    public ResponseEntity<List<MenuCategoryDTO>> getAllMenuCategory() {
        List<MenuCategory> menuCategoryList = categoryRepository.findAll();
        List<MenuCategoryDTO> collect = menuCategoryList.stream().map(this::convertToDTO)
                .sorted(Comparator.comparing(MenuCategoryDTO::getSortOrder))
                .collect(Collectors.toList());
        return ResponseEntity.ok(collect);
    }

    /**
     * 分页查询商品
     *
     * @param page      页码（从0开始）
     * @param size      每页大小
     * @param sortBy    排序字段（默认：sortOrder）
     * @param direction 排序方向（ASC/DESC，默认：ASC）
     */
    @GetMapping("/page")
    public ResponseEntity<Page<MenuCategory>> getMenuCategoryPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "sortOrder") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(defaultValue = "") String name) {

        Sort sort = "DESC".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Specification<MenuCategory> spec = (root, query, criteriaBuilder) -> {
            if (StringUtils.hasText(name)) {
                return criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%"
                );
            }
            return criteriaBuilder.conjunction();
        };
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<MenuCategory> menuCategoryPage = categoryRepository.findAll(spec, pageable);

        return ResponseEntity.ok(menuCategoryPage);
    }

}
