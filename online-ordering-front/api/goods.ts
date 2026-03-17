import http from "../utils/http";

// 创建商品
export function createGoods(data) {
  return http.request("post", "/goods", {
    data
  });
}

// 更新商品
export function updateGoods(id, data) {
  return http.request("put", `/goods/${id}`, {
    data
  });
}

// 删除商品
export function deleteGoods(id) {
  return http.request("delete", `/goods/${id}`);
}

// 根据ID查询商品详情
export function getGoodsById(id) {
  return http.request("get", `/goods/${id}`);
}

// 查询所有商品
export function getAllGoods() {
  return http.request("get", "/goods");
}

// 分页查询商品
export function getGoodsPage(query) {
  return http.request("get", "/goods/page", {
    params: query // query包含: page, size, sortBy, direction
  });
}

// 根据分类ID查询商品
export function getGoodsByCategory(categoryId) {
  return http.request("get", `/goods/category/${categoryId}`);
}
