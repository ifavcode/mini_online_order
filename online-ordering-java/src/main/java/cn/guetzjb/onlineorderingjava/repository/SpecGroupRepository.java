package cn.guetzjb.onlineorderingjava.repository;

import cn.guetzjb.onlineorderingjava.entity.SpecGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpecGroupRepository extends JpaRepository<SpecGroup, Integer>, JpaSpecificationExecutor<SpecGroup> {

    Optional<SpecGroup> findByGoodsId(Integer goodsId);

    @Query("SELECT sg.id FROM SpecGroup sg WHERE sg.goodsId = :goodsId")
    List<Integer> findIdsByGoodsId(Integer goodsId);

}
