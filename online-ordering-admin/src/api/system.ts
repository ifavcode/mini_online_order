import { http } from "@/utils/http";
import axios from "axios";

export function getDemoMode(): Promise<boolean> {
  return http.request("get", "/system/demo", {
  });
}

export function getRemoteVersion() {
  return axios.get("https://guetzjb.cn/assets_other/version-order.txt");
}