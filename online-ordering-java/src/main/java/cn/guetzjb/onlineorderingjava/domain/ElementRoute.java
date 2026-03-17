package cn.guetzjb.onlineorderingjava.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ElementRoute {

    private String name;

    private String path;

    private Meta meta;

    private Boolean hidden;

    private String component;

    private Boolean alwaysShow;

    private Boolean showLink;

    private String front;

    private List<ElementRoute> children = new ArrayList<>();

}
