package cn.guetzjb.onlineorderingjava.repository;

import cn.guetzjb.onlineorderingjava.entity.BusinessDynamic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessDynamicRepository extends JpaRepository<BusinessDynamic, Integer>, JpaSpecificationExecutor<BusinessDynamic> {
}
