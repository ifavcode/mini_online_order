package cn.guetzjb.onlineorderingjava.service;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaSubscribeMessage;
import cn.binarywang.wx.miniapp.constant.WxMaConstants;
import cn.guetzjb.onlineorderingjava.entity.Orders;
import cn.guetzjb.onlineorderingjava.repository.DictRepository;
import cn.guetzjb.onlineorderingjava.repository.GoodsRepository;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class WxCommonService {

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final WxMaService wxService;
    private final GoodsRepository goodsRepository;
    private final DictRepository dictRepository;

    public void sendTemplateMsg(String openid, Orders orders) {
        WxMaSubscribeMessage message = new WxMaSubscribeMessage();
        message.setTemplateId("4HNjqvQ6ZWAMleu9uIpiZ2EfNwSU_l6iTt4oqAvqSKI");
        message.setToUser(openid);
        message.setLang(WxMaConstants.MiniProgramLang.ZH_CN);
        message.setMiniprogramState(WxMaConstants.MiniProgramState.FORMAL);
        message.setPage("pages/order/details/?id=" + orders.getId());
        String goodsName = goodsRepository.getNameById(orders.getOrderItems().getFirst().getGoods().getId()) + (orders.getOrderItems().size() > 1 ? "等" : "");
        String shopName = dictRepository.getDictValueByDictKey("shop_name");
        message.setData(Lists.newArrayList(
                new WxMaSubscribeMessage.MsgData("thing6", shopName),
                new WxMaSubscribeMessage.MsgData("character_string5", String.valueOf(orders.getWxOrderNo())),
                new WxMaSubscribeMessage.MsgData("thing1", goodsName),
                new WxMaSubscribeMessage.MsgData("date2", orders.getCreatedAt().format(DEFAULT_FORMATTER)),
                new WxMaSubscribeMessage.MsgData("amount9", orders.getTotalAmount().toString())
        ));
        try {
            this.wxService.getMsgService().sendSubscribeMsg(message);
        } catch (WxErrorException e) {
            log.error("发送订阅消息失败：{}", e.getMessage());
        }
    }
}
