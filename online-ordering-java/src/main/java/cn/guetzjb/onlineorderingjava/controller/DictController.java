package cn.guetzjb.onlineorderingjava.controller;

import cn.guetzjb.onlineorderingjava.domain.Business;
import cn.guetzjb.onlineorderingjava.entity.SysDict;
import cn.guetzjb.onlineorderingjava.repository.DictRepository;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictRepository dictRepository;

    @GetMapping("/list")
    public ResponseEntity<List<SysDict>> list() {
        return ResponseEntity.ok(dictRepository.findAll());
    }

    @GetMapping("/leftLike")
    public ResponseEntity<List<SysDict>> leftLike(@RequestParam String key) {
        return ResponseEntity.ok(dictRepository.findByDictKeyStartingWith(key));
    }

    @GetMapping("/leftLikes/{keys}")
    public ResponseEntity<List<List<SysDict>>> leftLikes(@PathVariable List<String> keys) {
        List<List<SysDict>> result = new ArrayList<>();
        for (String key : keys) {
            List<SysDict> list = dictRepository.findByDictKeyStartingWith(key);
            result.add(list);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/save")
    public ResponseEntity<Void> save(@RequestBody List<SysDict> dicts) {
        for (SysDict dict : dicts) {
            if (dictRepository.existsByDictKey(dict.getDictKey())) {
                dictRepository.updateByDictName(dict);
            } else {
                dictRepository.save(dict);
            }
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete")
    @Transactional
    public ResponseEntity<Integer> delete(@RequestParam String key) {
        Integer count = dictRepository.deleteByDictKeyStartingWith(key);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/business")
    public ResponseEntity<Business> business() {
        List<SysDict> result = dictRepository.findAll((Specification<SysDict>) (root, query, criteriaBuilder) -> {
            return criteriaBuilder.or(criteriaBuilder.equal(root.get("dictKey"), "choose_lng_lat"),
                    criteriaBuilder.or(criteriaBuilder.equal(root.get("dictKey"), "shop_name")),
                    criteriaBuilder.or(criteriaBuilder.equal(root.get("dictKey"), "shop_desc")),
                    criteriaBuilder.or(criteriaBuilder.equal(root.get("dictKey"), "business_hours")),
                    criteriaBuilder.or(criteriaBuilder.equal(root.get("dictKey"), "shop_location")),
                    criteriaBuilder.or(criteriaBuilder.equal(root.get("dictKey"), "shop_phone")),
                    criteriaBuilder.or(criteriaBuilder.equal(root.get("dictKey"), "shop_image")),
                    criteriaBuilder.or(criteriaBuilder.equal(root.get("dictKey"), "shop_bg_image")));
        });
        Map<String, SysDict> map = result.stream().collect(Collectors.toMap(SysDict::getDictKey, v -> v));
        Business business = new Business();
        business.setChooseLngLat(JSONObject.parseObject(map.get("choose_lng_lat").getDictValue(), new TypeReference<Map<String, Object>>() {
        }));
        business.setShopName(map.get("shop_name").getDictValue());
        business.setShopDesc(map.get("shop_desc").getDictValue());
        business.setBusinessHours(JSONObject.parseObject(map.get("business_hours").getDictValue(), new TypeReference<Map<String, Object>>() {
        }));
        business.setShopLocation(map.get("shop_location").getDictValue());
        business.setShopImage(map.get("shop_image").getDictValue());
        business.setShopPhone(map.get("shop_phone").getDictValue());
        business.setShopBgImage(map.get("shop_bg_image").getDictValue());
        return ResponseEntity.ok(business);
    }

}
