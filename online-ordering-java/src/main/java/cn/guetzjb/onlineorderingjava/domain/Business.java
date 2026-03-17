package cn.guetzjb.onlineorderingjava.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Business {

    private String shopName;
    private String shopDesc;
    private String shopLocation;
    private String shopImage;
    private String shopBgImage;

    private Map<String, Object> businessHours;

    private Map<String, Object> chooseLngLat;

    private String shopPhone;

}
