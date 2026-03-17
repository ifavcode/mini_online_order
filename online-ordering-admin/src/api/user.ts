import { http } from "@/utils/http";

export type UserResult = {
  success: boolean;
  data: {
    /** 头像 */
    avatar: string;
    /** 用户名 */
    username: string;
    /** 昵称 */
    nickname: string;
    /** 当前登录用户的角色 */
    roles: Array<string>;
    /** 按钮级别权限 */
    permissions: Array<string>;
    /** `token` */
    accessToken: string;
    /** 用于调用刷新`accessToken`的接口时所需的`token` */
    refreshToken: string;
    /** `accessToken`的过期时间（格式'xxxx/xx/xx xx:xx:xx'） */
    expires: Date;
  };
};

export type RefreshTokenResult = {
  success: boolean;
  data: {
    /** `token` */
    accessToken: string;
    /** 用于调用刷新`accessToken`的接口时所需的`token` */
    refreshToken: string;
    /** `accessToken`的过期时间（格式'xxxx/xx/xx xx:xx:xx'） */
    expires: Date;
  };
};

/** 登录 */
export const getLogin = (data?: object) => {
  return http.request<UserResult>("post", "/login", { data });
};

/** 刷新`token` */
export const refreshTokenApi = (data?: object) => {
  return http.request<RefreshTokenResult>("post", "/refresh-token", { data });
};

export const listUser = (params?: object) => {
  return http.request<any>("get", "/user/page", { params });
};

export const delUser = (idsStr?: number | number[]) => {
  let ids = [];
  if (typeof idsStr == "number") {
    ids.push(idsStr);
  } else {
    ids = idsStr;
  }
  return http.request<any>("delete", "/user", {
    data: ids
  });
};

export const getUser = (id?: number) => {
  return http.request<any>("get", "/user/" + id, {});
};

export const updateUser = (data: any) => {
  return http.request<any>("put", "/user/" + data.userId, { data });
};

export const addUser = (data: any) => {
  return http.request<any>("post", "/user", { data });
};

export const changeUserStatus = (data: any) => {
  return http.request<any>("put", "/user/" + data.userId, { data });
};

export const resetUserPwd = (data?: object) => {
  return http.request<any>("post", "/user/resetPwd", { data });
};
