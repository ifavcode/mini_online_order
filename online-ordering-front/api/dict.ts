import http from "../utils/http";

export function leftLikeDict(key: string) {
  return http.request("get", "/dict/leftLike?key=" + key, {
  });
}

export function leftLikeDicts(keys: string[]) {
  return http.request("get", "/dict/leftLikes/" + keys.join(","), {});
}

export function saveDict(data: any) {
  return http.request("post", "/dict/save", {
    data,
  });
}

// 根据前缀删除
export function deleteDict(key: string) {
  return http.request("delete", "/dict/delete", {
    params: {
      key,
    },
  });
}

export function getBusinessInfo() {
  return http.request("get", "/dict/business", {});
}
