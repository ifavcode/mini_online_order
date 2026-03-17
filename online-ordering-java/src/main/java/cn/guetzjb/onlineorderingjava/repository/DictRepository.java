package cn.guetzjb.onlineorderingjava.repository;

import cn.guetzjb.onlineorderingjava.entity.SysDict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface DictRepository extends JpaRepository<SysDict, Integer>, JpaSpecificationExecutor<SysDict> {

    List<SysDict> findByDictKeyStartingWith(String dictKey);

    Integer deleteByDictKeyStartingWith(String dictKey);

    Boolean existsByDictKey(String dictKey);

    @Modifying
    @Transactional
    @Query("UPDATE SysDict s SET s.dictValue = :#{#sysDict.dictValue} WHERE s.dictKey = :#{#sysDict.dictKey}")
    void updateByDictName(@Param("sysDict") SysDict sysDict);

    @Query("select dictValue from SysDict where dictKey = :dictKey")
    String getDictValueByDictKey(String dictKey);

}
