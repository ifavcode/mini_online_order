package cn.guetzjb.onlineorderingjava.controller.wx;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.guetzjb.onlineorderingjava.entity.Orders;
import cn.guetzjb.onlineorderingjava.repository.OrdersRepository;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wx/orders")
@Slf4j
@RequiredArgsConstructor
public class WxOrdersController {

    private final WxPayService wxPayService;
    private final OrdersRepository ordersRepository;

    @GetMapping
    @SaCheckPermission({"system:orders:query"})
    public ResponseEntity<WxPayOrderQueryResult> wxPayOrders(@RequestParam String wxOrderNo) {
        Orders orders = ordersRepository.findByWxOrderNo(wxOrderNo);
        if (orders == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            WxPayOrderQueryResult wxPayOrderQueryResult = wxPayService.queryOrder(null, wxOrderNo);
            return ResponseEntity.ok(wxPayOrderQueryResult);
        } catch (WxPayException e) {
            throw new RuntimeException(e);
        }
    }

}
