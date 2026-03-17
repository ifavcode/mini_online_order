package cn.guetzjb.onlineorderingjava.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.guetzjb.onlineorderingjava.entity.Address;
import cn.guetzjb.onlineorderingjava.repository.AddressRepository;
import cn.guetzjb.onlineorderingjava.utils.NullAwareBeanUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressRepository addressRepository;
    private final NullAwareBeanUtils beanUtils;

    @PostMapping
    @SaCheckLogin
    public ResponseEntity<Address> createAddress(@RequestBody Address address) {
        address.setUserId(StpUtil.getLoginIdAsInt());
        Address savedAddress = addressRepository.save(address);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAddress);
    }

    @PutMapping("/{id}")
    @SaCheckLogin
    public ResponseEntity<Address> updateAddress(@PathVariable Integer id, @RequestBody Address address) {
        return addressRepository.findById(id)
                .map(existingAddress -> {
                    int userId = StpUtil.getLoginIdAsInt();
                    if (existingAddress.getUserId() != userId) {
                        ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Address());
                    }
                    beanUtils.copyProperties(address, existingAddress);
                    Address updatedAddress = addressRepository.save(existingAddress);
                    return ResponseEntity.ok(updatedAddress);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @SaCheckLogin
    public ResponseEntity deleteAddress(@PathVariable Integer id) {
        return addressRepository.findById(id)
                .map(address -> {
                    int userId = StpUtil.getLoginIdAsInt();
                    if (address.getUserId() != userId) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                    addressRepository.delete(address);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/profile")
    public ResponseEntity<List<Address>> getAllProfileAddress() {
        List<Address> addressList = addressRepository.findAll(new Specification<Address>() {
            @Override
            public Predicate toPredicate(Root<Address> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.equal(root.get("userId").as(Integer.class), StpUtil.getLoginIdAsInt());
                return predicate;
            }
        });
        return ResponseEntity.ok(addressList);
    }

    @GetMapping("/{id}")
    @SaCheckPermission("system:address:query")
    public ResponseEntity<Address> getAddressById(@PathVariable Integer id) {
        Optional<Address> address = addressRepository.findById(id);
        return address.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @SaCheckPermission("system:address:query")
    public ResponseEntity<List<Address>> getAllAddress() {
        List<Address> addressList = addressRepository.findAll();
        return ResponseEntity.ok(addressList);
    }

    @GetMapping("/page")
    @SaCheckPermission("system:address:query")
    public ResponseEntity<Page<Address>> getAddressPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String addressName) {

        Specification<Address> spec = (root, query, criteriaBuilder) -> {
            if (StringUtils.hasText(addressName)) {
                return criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("address_name")),
                        "%" + addressName.toLowerCase() + "%"
                );
            }
            return criteriaBuilder.conjunction();
        };
        Pageable pageable = PageRequest.of(page, size);
        Page<Address> addressPage = addressRepository.findAll(spec, pageable);

        return ResponseEntity.ok(addressPage);
    }

}
