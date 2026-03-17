package cn.guetzjb.onlineorderingjava.entity.dto;

import cn.guetzjb.onlineorderingjava.entity.Goods;
import cn.guetzjb.onlineorderingjava.entity.SpecGroup;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GoodsDTO {

    private Integer id;

    private Integer categoryId;

    private String name;

    private String description;

    private BigDecimal basePrice;

    private String imgUrl;

    private Integer hasSpec = 0;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<SpecGroup> specGroups;

    private Integer sortOrder = 0;

    private Integer stock = -1;

    public static GoodsDTO transGoods(Goods goods) {
        return GoodsDTO.builder()
                .id(goods.getId())
                .categoryId(goods.getCategoryId())
                .name(goods.getName())
                .description(goods.getDescription())
                .basePrice(goods.getBasePrice())
                .imgUrl(goods.getImgUrl())
                .hasSpec(goods.getHasSpec())
                .createdAt(goods.getCreatedAt())
                .updatedAt(goods.getUpdatedAt())
                .specGroups(goods.getSpecGroups())
                .sortOrder(goods.getSortOrder())
                .stock(goods.getStock())
                .build();

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GoodsDTO goodsDTO = (GoodsDTO) o;
        return Objects.equals(id, goodsDTO.id) && Objects.equals(categoryId, goodsDTO.categoryId) && Objects.equals(name, goodsDTO.name) && Objects.equals(description, goodsDTO.description) && Objects.equals(basePrice, goodsDTO.basePrice) && Objects.equals(imgUrl, goodsDTO.imgUrl) && Objects.equals(hasSpec, goodsDTO.hasSpec) && Objects.equals(createdAt, goodsDTO.createdAt) && Objects.equals(updatedAt, goodsDTO.updatedAt) && Objects.equals(specGroups, goodsDTO.specGroups);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, categoryId, name, description, basePrice, imgUrl, hasSpec, createdAt, updatedAt, specGroups);
    }
}
