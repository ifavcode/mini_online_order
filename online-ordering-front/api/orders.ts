import http from "../utils/http";

// 创建订单
export function createOrders(data) {
  return http.request("post", "/orders", {
    data,
  });
}

// 更新订单
export function updateOrders(id, data) {
  return http.request("put", `/orders/${id}`, {
    data,
  });
}

// 删除订单
export function deleteOrders(id) {
  return http.request("delete", `/orders/${id}`);
}

// 根据ID查询订单详情
export function getOrdersById(id) {
  return http.request("get", `/orders/${id}`);
}

// 查询所有订单
export function getAllOrders() {
  return http.request("get", "/orders");
}

// 分页查询订单
export function getOrdersPage(query: any) {
  return http.request("get", `/orders/page?page=${query.page}&size=${query.size}`);
}

export function postWxPay(wxOrderNo) {
  return http.request("get", "/wx/pay?wxOrderNo=" + wxOrderNo, {
  });
}

export function checkWxPay(wxOrderNo) {
  return http.request("get", "/wx/pay/check?wxOrderNo=" + wxOrderNo, {
  });
}