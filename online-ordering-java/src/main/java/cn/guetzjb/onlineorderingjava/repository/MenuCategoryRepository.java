package cn.guetzjb.onlineorderingjava.repository;

import cn.guetzjb.onlineorderingjava.entity.MenuCategory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Integer>, JpaSpecificationExecutor<MenuCategory> {

}
