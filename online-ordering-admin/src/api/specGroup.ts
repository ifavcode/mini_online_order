import { http } from "@/utils/http";

// 创建规格
export function createSpecGroup(data) {
  return http.request("post", "/specGroup", {
    data
  });
}

// 更新规格
export function updateSpecGroup(id, data) {
  return http.request("put", `/specGroup/${id}`, {
    data
  });
}

// 删除规格
export function deleteSpecGroup(goodsId) {
  return http.request("delete", `/specGroup/${goodsId}`);
}

// 根据ID查询规格详情
export function getSpecGroupById(id) {
  return http.request("get", `/specGroup/${id}`);
}

// 查询所有规格
export function getAllSpecGroup() {
  return http.request("get", "/specGroup");
}

// 分页查询规格
export function getSpecGroupPage(query) {
  return http.request("get", "/specGroup/page", {
    params: query // query包含: page, size, sortBy, direction
  });
}
