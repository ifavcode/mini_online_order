import http from "../utils/http";

// 创建商品类别
export function createCategory(data) {
  return http.request("post", "/category", {
    data,
  });
}

// 更新商品类别
export function updateCategory(id, data) {
  return http.request("put", `/category/${id}`, {
    data,
  });
}

// 删除商品类别
export function deleteCategory(id) {
  return http.request("delete", `/category/${id}`);
}

// 根据ID查询商品类别详情
export function getCategoryById(id) {
  return http.request("get", `/category/${id}`);
}

// 查询所有商品类别
export function getAllCategory() {
  return http.request("get", "/category/all");
}

// 分页查询商品类别
export function getCategoryPage(query) {
  return http.request("get", "/category/page", {
    params: query, // query包含: page, size, sortBy, direction
  });
}
