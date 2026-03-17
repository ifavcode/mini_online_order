package cn.guetzjb.onlineorderingjava.controller;

import cn.guetzjb.onlineorderingjava.entity.Goods;
import cn.guetzjb.onlineorderingjava.utils.NullAwareBeanUtils;
import cn.guetzjb.onlineorderingjava.repository.GoodsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsRepository goodsRepository;
    private final NullAwareBeanUtils beanUtils;

    /**
     * 创建商品
     */
    @PostMapping
    public ResponseEntity<Goods> createGoods(@RequestBody Goods goods) {
        Goods savedGoods = goodsRepository.save(goods);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedGoods);
    }

    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    public ResponseEntity<Goods> updateGoods(@PathVariable Integer id, @RequestBody Goods goods) {
        return goodsRepository.findById(id)
                .map(existingGoods -> {
                    beanUtils.copyProperties(goods, existingGoods);
                    Goods updatedGoods = goodsRepository.save(existingGoods);
                    return ResponseEntity.ok(updatedGoods);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoods(@PathVariable Integer id) {
        return goodsRepository.findById(id)
                .map(goods -> {
                    goodsRepository.delete(goods);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据ID查询商品详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Goods> getGoodsById(@PathVariable Integer id) {
        Optional<Goods> goods = goodsRepository.findById(id);
        return goods.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 查询所有商品
     */
    @GetMapping
    public ResponseEntity<List<Goods>> getAllGoods() {
        List<Goods> goodsList = goodsRepository.findAll();
        goodsList.sort(Comparator.comparingInt(Goods::getSortOrder));
        return ResponseEntity.ok(goodsList);
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
    public ResponseEntity<Page<Goods>> getGoodsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "sortOrder") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(defaultValue = "") String name) {

        Sort sort = "DESC".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Specification<Goods> spec = (root, query, criteriaBuilder) -> {
            if (StringUtils.hasText(name)) {
                return criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%"
                );
            }
            return criteriaBuilder.conjunction();
        };
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Goods> goodsPage = goodsRepository.findAll(spec, pageable);

        return ResponseEntity.ok(goodsPage);
    }

    /**
     * 根据分类ID查询商品
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Goods>> getGoodsByCategory(@PathVariable Integer categoryId) {
        List<Goods> goodsList = goodsRepository.findByCategoryId(categoryId);
        return ResponseEntity.ok(goodsList);
    }
}