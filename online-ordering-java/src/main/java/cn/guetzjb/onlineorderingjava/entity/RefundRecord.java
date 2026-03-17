package cn.guetzjb.onlineorderingjava.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefundRecord {

    /**
     * 字段描述见 WxPayRefundNotifyResult > ReqInfo 类
     */

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @Column(nullable = false, unique = false, length = 52)
        private String wxOrderNo;

        @Column
        private String refundId;

        @Column
        private Integer totalFee;

        @Column
        private Integer refundFee;

        @Column
        private String refundStatus;

        @Column
        private String successTime;

        @Column
        private String refundRecvAccout;

        @Column
        private String refundAccount;

        @Column
        private String refundRequestSource;
}
