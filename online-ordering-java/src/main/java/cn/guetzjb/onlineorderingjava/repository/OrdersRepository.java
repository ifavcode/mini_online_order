package cn.guetzjb.onlineorderingjava.repository;

import cn.guetzjb.onlineorderingjava.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Integer>, JpaSpecificationExecutor<Orders> {

    List<Orders> findAllBySysUser_UserId(Integer userId);

    Integer countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    Orders findByWxOrderNoAndSysUser_UserId(String wxOrderNo, Integer userId);

    Orders findByWxOrderNo(String wxOrderNo);

    List<Orders> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
