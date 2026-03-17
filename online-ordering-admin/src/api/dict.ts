import { http } from "@/utils/http";

export function leftLikeDict(key: string) {
  return http.request<any>("get", "/dict/leftLike", {
    params: {
      key
    }
  });
}

export function leftLikeDicts(keys: string[]) {
  return http.request<any>("get", "/dict/leftLikes/" + keys.join(","), {});
}

export function saveDict(data: any) {
  return http.request<any>("post", "/dict/save", {
    data
  });
}

// 根据前缀删除
export function deleteDict(key: string) {
  return http.request<any>("delete", "/dict/delete", {
    params: {
      key
    }
  });
}
