import { http } from "@/utils/http";

// 创建商家动态
export function createBusinessDynamic(data) {
  return http.request("post", "/businessDynamic", {
    data
  });
}

// 更新商家动态
export function updateBusinessDynamic(id, data) {
  return http.request("put", `/businessDynamic/${id}`, {
    data
  });
}

// 删除商家动态
export function deleteBusinessDynamic(id) {
  return http.request("delete", `/businessDynamic/${id}`);
}

// 根据ID查询商家动态详情
export function getBusinessDynamicById(id) {
  return http.request("get", `/businessDynamic/${id}`);
}

// 查询所有商家动态
export function getAllBusinessDynamic() {
  return http.request("get", "/businessDynamic");
}

// 分页查询商家动态
export function getBusinessDynamicPage(query) {
  return http.request("get", "/businessDynamic/page", {
    params: query // query包含: page, size, sortBy, direction
  });
}

// 根据分类ID查询商家动态
export function getBusinessDynamicByCategory(categoryId) {
  return http.request("get", `/businessDynamic/category/${categoryId}`);
}
