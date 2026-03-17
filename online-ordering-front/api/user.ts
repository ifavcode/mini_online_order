import { useSystemStore } from "../stores/system";
import http from "../utils/http";

export function wxLogin(code) {
  const { appId } = useSystemStore();
  if (!appId) {
    console.log("appId不存在，请填写");
    return;
  }
  return http.request("post", `/wx/user/${appId}/login`, {
    data: code,
  });
}

export function webLogin() {
  return http.request("post", `/login`, {
    data: {
      username: 'emme',
      password: 'admin123'
    }
  });
}

export const listUser = (params?: object) => {
  return http.request("get", "/user/page", { params });
};

export const getProfile = () => {
  return http.request("get", "/user/profile", {});
};

export const delUser = (idsStr?: number | number[]) => {
  let ids: number | number[] = [];
  if (typeof idsStr == "number") {
    ids.push(idsStr);
  } else {
    ids = idsStr as number[];
  }
  return http.request("delete", "/user", {
    data: ids,
  });
};

export const getUser = (id?: number) => {
  return http.request("get", "/user/" + id, {});
};

export const updateUser = (data: any) => {
  return http.request("put", "/user/update", { data });
};

export const addUser = (data: any) => {
  return http.request("post", "/user", { data });
};

export const changeUserStatus = (data: any) => {
  return http.request("put", "/user/" + data.userId, { data });
};

export const resetUserPwd = (data?: object) => {
  return http.request("post", "/user/resetPwd", { data });
};
