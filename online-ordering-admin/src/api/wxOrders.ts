import { http } from "@/utils/http";

export function getWxOrdersByWxOrderNo(wxOrderNo: string) {
  return http.request("get", `/wx/orders?wxOrderNo=` + wxOrderNo);
}
