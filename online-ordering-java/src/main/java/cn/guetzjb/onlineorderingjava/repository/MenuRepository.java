package cn.guetzjb.onlineorderingjava.repository;

import cn.guetzjb.onlineorderingjava.entity.SysMenu;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends ListCrudRepository<SysMenu, Long>, PagingAndSortingRepository<SysMenu, Long> {

}
