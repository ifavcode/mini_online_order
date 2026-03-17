package cn.guetzjb.onlineorderingjava.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.guetzjb.onlineorderingjava.entity.BusinessDynamic;
import cn.guetzjb.onlineorderingjava.repository.BusinessDynamicRepository;
import cn.guetzjb.onlineorderingjava.utils.NullAwareBeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/businessDynamic")
@RequiredArgsConstructor
public class BusinessDynamicController {

    private final BusinessDynamicRepository businessDynamicRepository;
    private final NullAwareBeanUtils beanUtils;

    @PostMapping
    @SaCheckPermission("system:businessDynamic:add")
    public ResponseEntity<BusinessDynamic> createBusinessDynamic(@RequestBody BusinessDynamic businessDynamic) {
        BusinessDynamic savedBusinessDynamic = businessDynamicRepository.save(businessDynamic);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBusinessDynamic);
    }

    @PutMapping("/{id}")
    @SaCheckPermission("system:businessDynamic:add")
    public ResponseEntity<BusinessDynamic> updateBusinessDynamic(@PathVariable Integer id, @RequestBody BusinessDynamic businessDynamic) {
        return businessDynamicRepository.findById(id)
                .map(existingBusinessDynamic -> {
                    beanUtils.copyProperties(businessDynamic, existingBusinessDynamic);
                    BusinessDynamic updatedBusinessDynamic = businessDynamicRepository.save(existingBusinessDynamic);
                    return ResponseEntity.ok(updatedBusinessDynamic);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("system:businessDynamic:delete")
    public ResponseEntity<Void> deleteBusinessDynamic(@PathVariable Integer id) {
        return businessDynamicRepository.findById(id)
                .map(businessDynamic -> {
                    businessDynamicRepository.delete(businessDynamic);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusinessDynamic> getBusinessDynamicById(@PathVariable Integer id) {
        Optional<BusinessDynamic> businessDynamic = businessDynamicRepository.findById(id);
        return businessDynamic.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<BusinessDynamic>> getAllBusinessDynamic() {
        List<BusinessDynamic> businessDynamicList = businessDynamicRepository.findAll();
        return ResponseEntity.ok(businessDynamicList);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<BusinessDynamic>> getBusinessDynamicPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String businessDynamicName) {

        Specification<BusinessDynamic> spec = (root, query, criteriaBuilder) -> {
            query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
            return criteriaBuilder.conjunction();
        };
        Pageable pageable = PageRequest.of(page, size);
        Page<BusinessDynamic> businessDynamicPage = businessDynamicRepository.findAll(spec, pageable);

        return ResponseEntity.ok(businessDynamicPage);
    }

}
