import { http } from "@/utils/http";

// 创建订单
export function createOrders(data) {
  return http.request("post", "/orders", {
    data
  });
}

// 更新订单
export function updateOrders(id, data) {
  return http.request("put", `/orders/${id}`, {
    data
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
export function getOrdersPage(query) {
  return http.request("get", "/orders/page", {
    params: query // query包含: page, size, sortBy, direction
  });
}


// 创建订单
export function createOrdersAdmin(data) {
  return http.request("post", "/orders/admin", {
    data
  });
}

// 更新订单
export function updateOrdersAdmin(id, data) {
  return http.request("put", `/orders/admin/${id}`, {
    data
  });
}

export function updateOrdersStatusAdmin(id, data) {
  return http.request("put", `/orders/admin/status/${id}`, {
    data
  });
}

// 删除订单
export function deleteOrdersAdmin(id) {
  return http.request("delete", `/orders/admin/${id}`);
}

// 删除订单集合
export function deleteOrdersAdminBatch(ids) {
  return http.request("delete", `/orders/admin/${ids}`);
}

// 根据ID查询订单详情
export function getOrdersByIdAdmin(id) {
  return http.request("get", `/orders/admin/${id}`);
}

// 查询所有订单
export function getAllOrdersAdmin() {
  return http.request("get", "/orders/admin");
}

// 分页查询订单
export function getOrdersPageAdmin(query) {
  return http.request("get", "/orders/admin/page", {
    params: query // query包含: page, size, sortBy, direction
  });
}

export function refundOrderAdmin(wxOrderNo, refundFeeP, refundDesc) {
  return http.request("post", "/orders/admin/refund", {
    params: {
      wxOrderNo,
      refundFeeP,
      refundDesc
    }
  });
}

export function getOrdersStatistics(startTime, endTime) {
  return http.request("get", "/orders/admin/statistics", {
    params: {
      startTime,
      endTime
    }
  });
}