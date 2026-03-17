import { http } from "@/utils/http";

export function uploadImage() {
  return http.request("post", "/upload/image", {
    headers: {
      "Content-Type": "multipart/form-data"
    }
  });
}

export function uploadBase64(base64Str: string) {
  return http.request("post", "/upload/base64", {
    data: {
      base64Str,
      suffix: 'png'
    }
  });
}
