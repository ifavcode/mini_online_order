import { http } from "@/utils/http";

// 创建退款记录
export function createRefundRecord(data) {
  return http.request("post", "/refund", {
    data
  });
}

// 更新退款记录
export function updateRefundRecord(id, data) {
  return http.request("put", `/refund/${id}`, {
    data
  });
}

// 删除退款记录
export function deleteRefundRecord(id) {
  return http.request("delete", `/refund/${id}`);
}

// 根据ID查询退款记录详情
export function getRefundRecordById(id) {
  return http.request("get", `/refund/${id}`);
}

// 查询所有退款记录
export function getAllRefundRecord() {
  return http.request("get", "/refund");
}

// 分页查询退款记录
export function getRefundRecordPage(query) {
  return http.request("get", "/refund/page", {
    params: query // query包含: page, size, sortBy, direction
  });
}

// 根据分类ID查询退款记录
export function getRefundRecordByCategory(categoryId) {
  return http.request("get", `/refund/category/${categoryId}`);
}
