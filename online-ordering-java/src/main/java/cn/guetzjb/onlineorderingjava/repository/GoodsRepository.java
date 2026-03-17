package cn.guetzjb.onlineorderingjava.repository;

import cn.guetzjb.onlineorderingjava.entity.Goods;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoodsRepository extends JpaRepository<Goods, Integer>, JpaSpecificationExecutor<Goods> {
    List<Goods> findByCategoryId(Integer categoryId);

    @Query("SELECT g.name FROM Goods g WHERE g.id = :id")
    String getNameById(Integer id);

    List<Goods> findAllByIdIn(List<Integer> ids);
}
