package cn.guetzjb.onlineorderingjava.repository;

import cn.guetzjb.onlineorderingjava.entity.RefundRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RefundRecordRepository extends JpaRepository<RefundRecord, Integer>, JpaSpecificationExecutor<RefundRecord> {

    List<RefundRecord> findAllBySuccessTimeBetween(String start, String end);

}
