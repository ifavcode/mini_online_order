import http from "../utils/http";

// 创建桌号
export function createTable(data) {
  return http.request("post", "/table", {
    data
  });
}

// 更新桌号
export function updateTable(id, data) {
  return http.request("put", `/table/${id}`, {
    data
  });
}

// 删除桌号
export function deleteTable(id) {
  return http.request("delete", `/table/${id}`);
}

// 根据ID查询桌号详情
export function getTableById(id) {
  return http.request("get", `/table/${id}`);
}

// 查询所有桌号
export function getAllTable() {
  return http.request("get", "/table");
}

// 分页查询桌号
export function getTablePage(query) {
  return http.request("get", "/table/page", {
    params: query // query包含: page, size, sortBy, direction
  });
}

// 根据分类ID查询桌号
export function getTableByCategory(categoryId) {
  return http.request("get", `/table/category/${categoryId}`);
}

export function tableCode2Name(tableCode) {
  return http.request("get", `/table/code2Name?tableCode=` + tableCode);
}
