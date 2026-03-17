package cn.guetzjb.onlineorderingjava.repository;

import cn.guetzjb.onlineorderingjava.entity.Goods;
import cn.guetzjb.onlineorderingjava.entity.SpecItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecItemRepository extends JpaRepository<SpecItem, Integer> {
}
