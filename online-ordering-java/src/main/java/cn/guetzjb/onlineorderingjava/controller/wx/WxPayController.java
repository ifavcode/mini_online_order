package cn.guetzjb.onlineorderingjava.controller.wx;

import cn.dev33.satoken.stp.StpUtil;
import cn.guetzjb.onlineorderingjava.entity.Orders;
import cn.guetzjb.onlineorderingjava.enums.OrdersEnum;
import cn.guetzjb.onlineorderingjava.repository.OrdersRepository;
import cn.guetzjb.onlineorderingjava.utils.IpUtil;
import cn.guetzjb.onlineorderingjava.utils.OrdersUtil;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.notify.WxPayRefundNotifyResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/wx/pay")
@Slf4j
@RequiredArgsConstructor
public class WxPayController {

    private final WxPayService wxPayService;
    private final OrdersRepository ordersRepository;
    private final OrdersUtil ordersUtil;

    @PostMapping("/notify")
    public String fallback(HttpServletRequest request) {
        String xmlData;
        try {
            xmlData = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("读取回调数据失败", e);
            return WxPayNotifyResponse.fail("读取数据失败");
        }

        WxPayOrderNotifyResult notifyResult;
        try {
            notifyResult = wxPayService.parseOrderNotifyResult(xmlData);
        } catch (WxPayException e) {
            log.error("验签/解析失败", e);
            return WxPayNotifyResponse.fail("验签失败");
        }

        try {
            ordersUtil.handlePaySuccess(notifyResult);
        } catch (Exception e) {
            log.error("业务处理异常, outTradeNo={}", notifyResult.getOutTradeNo(), e);
            // 业务异常也返回成功，避免微信一直重试
        }
        return WxPayNotifyResponse.success("成功");
    }

    @PostMapping("/refund/notify")
    public String refundFallback(HttpServletRequest request) {
        String xmlData;
        try {
            xmlData = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("读取回调数据失败", e);
            return WxPayNotifyResponse.fail("读取数据失败");
        }

        WxPayRefundNotifyResult notifyResult;
        try {
            notifyResult = wxPayService.parseRefundNotifyResult(xmlData);
        } catch (WxPayException e) {
            log.error("验签/解析失败", e);
            return WxPayNotifyResponse.fail("验签失败");
        }

        try {
            ordersUtil.handleRefundSuccess(notifyResult);
        } catch (Exception e) {
            log.error("业务处理异常, outTradeNo={}", notifyResult.getReqInfo().getOutTradeNo(), e);
        }
        return WxPayNotifyResponse.success("成功");
    }

    @GetMapping
    public ResponseEntity<Object> pay(@RequestParam String wxOrderNo) {
        try {
            Orders orders = ordersRepository.findByWxOrderNoAndSysUser_UserId(wxOrderNo, StpUtil.getLoginIdAsInt());
            String openid = StpUtil.getSession().get("openid").toString();
            // 保留两位小数、四舍五入，转化为分
            int totalFee = orders.getTotalAmount().multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).intValue();
            WxPayUnifiedOrderRequest request = WxPayUnifiedOrderRequest.newBuilder()
                    .outTradeNo(wxOrderNo) // 商户订单号（唯一）
                    .body(orders.getWxOrderNo()) // 订单描述
                    .totalFee(totalFee) // 金额（单位：分，需转换）
                    .spbillCreateIp(IpUtil.getIpAddress()) // 客户端IP
                    .openid(openid) // JSAPI支付必须传openid
                    .tradeType("JSAPI") // 支付类型
                    .build();
            Object order = wxPayService.createOrder(request);
            return ResponseEntity.ok(order);
        } catch (WxPayException e) {
            String errCode = e.getErrCode();
            String message = switch (errCode) {
                case "ORDER_PAID" -> "订单已支付，请勿重复支付";
                case "OUT_TRADE_NO_USED" -> "订单号已被使用，请刷新后重试";
                case "ORDERCLOSED" -> "订单已关闭，请重新下单";
                case "ORDERNOTEXIST" -> "订单不存在，请重新下单";
                case "NOTENOUGH" -> "余额不足，请更换支付方式";
                case "AUTHCODEEXPIRE" -> "支付码已过期，请刷新后重试";
                case "OPENID_MISMATCH" -> "用户信息异常，请重新登录后支付";
                case "ACCOUNT_FROZEN" -> "账户已冻结，请联系微信客服";
                case "INVALID_REQUEST" -> "请求参数有误，请联系客服";
                case "SYSTEMERROR" -> "微信支付系统繁忙，请稍后重试";
                case "BANKERROR" -> "银行系统繁忙，请稍后重试";
                default -> "支付失败：" + e.getErrCodeDes();
            };
            log.error("微信支付异常 订单号:{} errCode:{} errMsg:{}", wxOrderNo, errCode, e.getErrCodeDes(), e);
            return ResponseEntity.ok().body(Map.of(
                    "code", errCode,
                    "message", message
            ));
        } catch (Exception e) {
            log.error("支付接口异常 订单号:{}", wxOrderNo, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "code", "SERVER_ERROR",
                    "message", "系统异常，请稍后重试"
            ));
        }
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> check(@RequestParam String wxOrderNo) {
        try {
            WxPayOrderQueryResult wxPayOrderQueryResult = wxPayService.queryOrder(null, wxOrderNo);
            String tradeState = wxPayOrderQueryResult.getTradeState();
            boolean success = "SUCCESS".equals(tradeState);
            if (success) {
                Orders orders = ordersRepository.findByWxOrderNo(wxOrderNo);
                orders.setStatus(OrdersEnum.PAID.getCode());
                ordersRepository.save(orders);
            }
            return ResponseEntity.ok().body(success);
        } catch (WxPayException e) {
            throw new RuntimeException(e);
        }
    }
}
