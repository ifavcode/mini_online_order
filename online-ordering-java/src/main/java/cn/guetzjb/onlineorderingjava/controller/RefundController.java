package cn.guetzjb.onlineorderingjava.controller;

import cn.guetzjb.onlineorderingjava.entity.RefundRecord;
import cn.guetzjb.onlineorderingjava.repository.RefundRecordRepository;
import cn.guetzjb.onlineorderingjava.utils.NullAwareBeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/refund")
@RequiredArgsConstructor
public class RefundController {

    private final RefundRecordRepository refundRecordRepository;
    private final NullAwareBeanUtils beanUtils;

    @PostMapping
    public ResponseEntity<RefundRecord> createRefundRecord(@RequestBody RefundRecord refundRecord) {
        RefundRecord savedRefundRecord = refundRecordRepository.save(refundRecord);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRefundRecord);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RefundRecord> updateRefundRecord(@PathVariable Integer id, @RequestBody RefundRecord refundRecord) {
        return refundRecordRepository.findById(id)
                .map(existingRefundRecord -> {
                    beanUtils.copyProperties(refundRecord, existingRefundRecord);
                    RefundRecord updatedRefundRecord = refundRecordRepository.save(existingRefundRecord);
                    return ResponseEntity.ok(updatedRefundRecord);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRefundRecord(@PathVariable Integer id) {
        return refundRecordRepository.findById(id)
                .map(refundRecord -> {
                    refundRecordRepository.delete(refundRecord);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RefundRecord> getRefundRecordById(@PathVariable Integer id) {
        Optional<RefundRecord> refundRecord = refundRecordRepository.findById(id);
        return refundRecord.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<RefundRecord>> getAllRefundRecord() {
        List<RefundRecord> refundRecordList = refundRecordRepository.findAll();
        return ResponseEntity.ok(refundRecordList);
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
    public ResponseEntity<Page<RefundRecord>> getRefundRecordPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "successTime") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(defaultValue = "") String wxOrderNo) {

        Sort sort = "DESC".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Specification<RefundRecord> spec = (root, query, criteriaBuilder) -> {
            if (StringUtils.hasText(wxOrderNo)) {
                return criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("wxOrderNo")),
                        "%" + wxOrderNo.toLowerCase() + "%"
                );
            }
            return criteriaBuilder.conjunction();
        };
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<RefundRecord> refundRecordPage = refundRecordRepository.findAll(spec, pageable);

        return ResponseEntity.ok(refundRecordPage);
    }
}
