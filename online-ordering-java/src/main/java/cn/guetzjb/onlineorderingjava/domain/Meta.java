package cn.guetzjb.onlineorderingjava.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Meta {
    private String icon;
    private String title;
    private Boolean showParent;
}
