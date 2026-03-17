package cn.guetzjb.onlineorderingjava.controller;

import cn.guetzjb.onlineorderingjava.entity.Table;
import cn.guetzjb.onlineorderingjava.repository.TableRepository;
import cn.guetzjb.onlineorderingjava.utils.NullAwareBeanUtils;
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
@RequestMapping("/table")
@RequiredArgsConstructor
public class TableController {

    private final TableRepository tableRepository;
    private final NullAwareBeanUtils beanUtils;

    @PostMapping
    public ResponseEntity<Table> createTable(@RequestBody Table table) {
        Table savedTable = tableRepository.save(table);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTable);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Table> updateTable(@PathVariable Integer id, @RequestBody Table table) {
        return tableRepository.findById(id)
                .map(existingTable -> {
                    beanUtils.copyProperties(table, existingTable);
                    Table updatedTable = tableRepository.save(existingTable);
                    return ResponseEntity.ok(updatedTable);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTable(@PathVariable Integer id) {
        return tableRepository.findById(id)
                .map(table -> {
                    tableRepository.delete(table);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Table> getTableById(@PathVariable Integer id) {
        Optional<Table> table = tableRepository.findById(id);
        return table.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Table>> getAllTable() {
        List<Table> tableList = tableRepository.findAll();
        return ResponseEntity.ok(tableList);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<Table>> getTablePage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String tableName) {

        Specification<Table> spec = (root, query, criteriaBuilder) -> {
            if (StringUtils.hasText(tableName)) {
                return criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("table_name")),
                        "%" + tableName.toLowerCase() + "%"
                );
            }
            return criteriaBuilder.conjunction();
        };
        Pageable pageable = PageRequest.of(page, size);
        Page<Table> tablePage = tableRepository.findAll(spec, pageable);

        return ResponseEntity.ok(tablePage);
    }

    @GetMapping("/code2Name")
    public ResponseEntity<String> getCode2Name(@RequestParam String tableCode) {
        Table table = tableRepository.findTableNameByTableCode(tableCode);
        return ResponseEntity.ok(table != null ? table.getTableName() : "");
    }

}
