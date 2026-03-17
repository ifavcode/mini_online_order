package cn.guetzjb.onlineorderingjava.controller;

import cn.guetzjb.onlineorderingjava.entity.SpecGroup;
import cn.guetzjb.onlineorderingjava.entity.SpecItem;
import cn.guetzjb.onlineorderingjava.repository.GoodsRepository;
import cn.guetzjb.onlineorderingjava.repository.SpecGroupRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/specGroup")
@RequiredArgsConstructor
public class SpecController {

    private final SpecGroupRepository specGroupRepository;
    private final NullAwareBeanUtils beanUtils;

    @PostMapping
    @Transactional
    public ResponseEntity<List<SpecGroup>> createSpecGroup(@RequestBody List<SpecGroup> specGroups) {
        if (specGroups == null || specGroups.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Integer goodsId = specGroups.getFirst().getGoodsId();
        List<Integer> haveIds = specGroupRepository.findIdsByGoodsId(goodsId);
        Set<Integer> haveIdsSet = new HashSet<>(haveIds);
        for (SpecGroup specGroup : specGroups) {
            if (specGroup.getId() != null) {
                haveIdsSet.remove(specGroup.getId());
            }
            if (specGroup.getSpecItems() != null) {
                for (SpecItem specItem : specGroup.getSpecItems()) {
                    specItem.setSpecGroup(specGroup);
                }
            }
        }
        List<SpecGroup> list = specGroupRepository.saveAll(specGroups);
        specGroupRepository.deleteAllById(haveIdsSet);
        return ResponseEntity.status(HttpStatus.CREATED).body(list);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SpecGroup> updateSpecGroup(@PathVariable Integer id, @RequestBody SpecGroup specGroup) {
        return specGroupRepository.findById(id)
                .map(existingSpecGroup -> {
                    beanUtils.copyProperties(specGroup, existingSpecGroup);
                    SpecGroup updatedSpecGroup = specGroupRepository.save(existingSpecGroup);
                    return ResponseEntity.ok(updatedSpecGroup);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{goodsId}")
    public ResponseEntity<Void> deleteSpecGroup(@PathVariable Integer goodsId) {
        return specGroupRepository.findByGoodsId(goodsId)
                .map(specGroup -> {
                    specGroupRepository.delete(specGroup);
                    return ResponseEntity.noContent().<Void>build();
                }).orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpecGroup> getSpecGroupById(@PathVariable Integer id) {
        Optional<SpecGroup> specGroup = specGroupRepository.findById(id);
        return specGroup.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<SpecGroup>> getAllSpecGroup() {
        List<SpecGroup> specGroupList = specGroupRepository.findAll();
        return ResponseEntity.ok(specGroupList);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<SpecGroup>> getSpecGroupPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "sortOrder") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(defaultValue = "") String name) {

        Sort sort = "DESC".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Specification<SpecGroup> spec = (root, query, criteriaBuilder) -> {
            if (StringUtils.hasText(name)) {
                return criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%"
                );
            }
            return criteriaBuilder.conjunction();
        };
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<SpecGroup> specGroupPage = specGroupRepository.findAll(spec, pageable);

        return ResponseEntity.ok(specGroupPage);
    }

}
