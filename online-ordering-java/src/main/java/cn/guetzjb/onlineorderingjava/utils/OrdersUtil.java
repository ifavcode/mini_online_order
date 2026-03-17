package cn.guetzjb.onlineorderingjava.utils;

import cn.dev33.satoken.stp.StpUtil;
import cn.guetzjb.onlineorderingjava.entity.*;
import cn.guetzjb.onlineorderingjava.enums.OrdersEnum;
import cn.guetzjb.onlineorderingjava.repository.GoodsRepository;
import cn.guetzjb.onlineorderingjava.repository.OrdersRepository;
import cn.guetzjb.onlineorderingjava.repository.RefundRecordRepository;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.notify.WxPayRefundNotifyResult;
import jakarta.persistence.criteria.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
public class OrdersUtil {

    private final OrdersRepository ordersRepository;
    private final GoodsRepository goodsRepository;
    private final RefundRecordRepository refundRecordRepository;

    public OrdersUtil(OrdersRepository ordersRepository, GoodsRepository goodsRepository, RefundRecordRepository refundRecordRepository) {
        this.ordersRepository = ordersRepository;
        this.goodsRepository = goodsRepository;
        this.refundRecordRepository = refundRecordRepository;
    }

    /**
     * 生成取餐号
     * 格式: 四位数字，按照当天的订单数量依次递增，大于四位则直接显示
     * 示例: 0001、0002
     *
     * @return 订单号
     */
    public String generateOrderNo() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        Integer count = ordersRepository.countByCreatedAtBetween(startOfDay, endOfDay);
        int len = String.valueOf(count + 1).length();
        if (len <= 4) {
            int remain = 4 - len;
            return "0".repeat(remain) + (count + 1);
        }
        return String.valueOf(count + 1);
    }

    public String generateWxOrderNo() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public boolean checkSelfOrder(Orders existingOrders) {
        return existingOrders.getSysUser().getUserId() == StpUtil.getLoginIdAsInt();
    }

    public void checkAndSubtractInventory(Orders orders) {
        checkInventory(orders);
        subtractInventory(orders);
    }

    /**
     * @param orders 检查商品库存、规格库存
     */
    public void checkInventory(Orders orders) {
        List<Integer> goodsIdList = orders.getOrderItems().stream().map(v -> v.getGoods().getId()).toList();
        Map<Integer, Goods> idToGoods = goodsRepository.findAllByIdIn(goodsIdList).stream().collect(Collectors.toMap(Goods::getId, v -> v));
        for (OrderItem orderItem : orders.getOrderItems()) {
            Goods goods = idToGoods.get(orderItem.getGoods().getId());
            if (goods.getStock() != -1 && goods.getStock() - orderItem.getCount() < 0) {
                throw new RuntimeException(goods.getName() + "库存不足!");
            }
            List<SpecGroup> costSpecGrpupList = JSONObject.parseObject(orderItem.getSpecGroupJson(), new TypeReference<>() {
            });
            Map<Integer, SpecItem> costSpecItemMap = getSpecItemMap(costSpecGrpupList);
            Map<Integer, SpecItem> haveSpecItemMap = getSpecItemMap(goods.getSpecGroups());
            for (SpecItem cost : costSpecItemMap.values()) {
                Integer stock = haveSpecItemMap.get(cost.getId()).getStock();
                if (stock != -1 && stock - orderItem.getCount() < 0) {
                    throw new RuntimeException(cost.getName() + "库存不足!");
                }
            }
        }
    }

    /**
     * @param orders 扣除商品库存、规格库存
     */
    public void subtractInventory(Orders orders) {
        List<Integer> goodsIdList = orders.getOrderItems().stream().map(v -> v.getGoods().getId()).toList();
        Map<Integer, Goods> idToGoods = goodsRepository.findAllByIdIn(goodsIdList).stream().collect(Collectors.toMap(Goods::getId, v -> v));
        for (OrderItem orderItem : orders.getOrderItems()) {
            Goods goods = idToGoods.get(orderItem.getGoods().getId());
            if (goods.getStock() != -1) {
                goods.setStock(goods.getStock() - orderItem.getCount());
            }
            List<SpecGroup> costSpecGrpupList = JSONObject.parseObject(orderItem.getSpecGroupJson(), new TypeReference<>() {
            });
            Map<Integer, SpecItem> costSpecItemMap = getSpecItemMap(costSpecGrpupList);
            Map<Integer, SpecItem> haveSpecItemMap = getSpecItemMap(goods.getSpecGroups());
            for (SpecItem cost : costSpecItemMap.values()) {
                SpecItem specItem = haveSpecItemMap.get(cost.getId());
                if (specItem.getStock() != -1) {
                    specItem.setStock(specItem.getStock() - orderItem.getCount());
                }
            }
        }
        goodsRepository.saveAll(idToGoods.values());
    }

    private Map<Integer, SpecItem> getSpecItemMap(List<SpecGroup> list) {
        Map<Integer, SpecItem> map = new HashMap<>();
        for (SpecGroup specGroup : list) {
            for (SpecItem specItem : specGroup.getSpecItems()) {
                map.put(specItem.getId(), specItem);
            }
        }
        return map;
    }

    public void handlePaySuccess(WxPayOrderNotifyResult result) {
        // 只处理支付成功的通知
        if (!"SUCCESS".equals(result.getResultCode())) {
            log.warn("非支付成功通知, resultCode={}", result.getResultCode());
            return;
        }

        String outTradeNo = result.getOutTradeNo();

        // 幂等：已支付则直接跳过
        Orders orders = ordersRepository.findByWxOrderNo(outTradeNo);
        if (orders == null) {
            log.warn("订单不存在: {}", outTradeNo);
            return;
        }
        if (OrdersEnum.PAID.getCode().equals(orders.getStatus())) {
            log.info("订单已处理，跳过重复回调: {}", outTradeNo);
            return;
        }

        // 校验金额（单位：分）
        int totalFee = orders.getTotalAmount().multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).intValue();
        if (!result.getTotalFee().equals(totalFee)) {
            log.error("金额不一致! 期望={}, 实际={}", totalFee, result.getTotalFee());
            return;
        }

        // 更新订单
        orders.setStatus(OrdersEnum.PAID.getCode());
        orders.setPayTime(LocalDateTime.now());
        ordersRepository.save(orders);

        // 通知后台更新订单状态
        WebSocketUtil.sendAll(String.valueOf(orders.getId()));

        log.info("支付成功处理完成, outTradeNo={}", outTradeNo);
    }

    public void handleRefundSuccess(WxPayRefundNotifyResult result) {
        // 获取解密后的明文信息 (wx-java-pay 会自动处理解密逻辑)
        WxPayRefundNotifyResult.ReqInfo reqInfo = result.getReqInfo();

        // 校验退款状态
        if (!"SUCCESS".equals(reqInfo.getRefundStatus())) {
            log.warn("非退款成功通知, refundStatus={}", reqInfo.getRefundStatus());
            return;
        }

        String outRefundNo = reqInfo.getOutRefundNo(); // 商户退款单号
        String outTradeNo = reqInfo.getOutTradeNo();   // 商户订单号

        // 幂等校验：根据退款单号查询
        Orders orders = ordersRepository.findByWxOrderNo(outTradeNo);
        if (orders == null) {
            log.warn("订单不存在: {}", outTradeNo);
            return;
        }

        // 状态校验：如果已经是 REFUNDED 状态，则跳过
        if (OrdersEnum.REFUNDED.getCode().equals(orders.getStatus())) {
            log.info("退款已处理，跳过重复回调: {}", outRefundNo);
            return;
        }

        // 更新订单/退款单状态
        orders.setStatus(OrdersEnum.REFUNDED.getCode());
        ordersRepository.save(orders);

        // 记录到数据库
        RefundRecord build = RefundRecord.builder().wxOrderNo(reqInfo.getOutTradeNo())
                .refundId(reqInfo.getRefundId())
                .totalFee(reqInfo.getTotalFee())
                .refundFee(reqInfo.getRefundFee())
                .refundStatus(reqInfo.getRefundStatus())
                .successTime(reqInfo.getSuccessTime())
                .refundRecvAccout(reqInfo.getRefundRecvAccout())
                .refundAccount(reqInfo.getRefundAccount())
                .refundRequestSource(reqInfo.getRefundRequestSource())
                .build();
        refundRecordRepository.save(build);

        // 通知管理端更新
        WebSocketUtil.sendAll(String.valueOf(orders.getId()));

        log.info("退款成功处理完成, outRefundNo={}, outTradeNo={}", outRefundNo, outTradeNo);
    }

}
