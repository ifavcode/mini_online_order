import { http } from "@/utils/http";

// 查询菜单列表
export function listMenu(query) {
  return http.request("get", "/menu/list", {
    params: query
  });
}

// 查询菜单详细
export function getMenu(menuId) {
  return http.request("get", "/menu/" + menuId);
}

// 查询菜单下拉树结构
export function treeselect() {
  return http.request("get", "/menu/tree");
}

// 新增菜单
export function addMenu(data) {
  return http.request("post", "/menu", {
    data
  });
}

// 修改菜单
export function updateMenu(data) {
  return http.request("put", "/menu", {
    data
  });
}

// 删除菜单
export function delMenu(menuId) {
  return http.request("delete", "/menu/delete/" + menuId);
}
