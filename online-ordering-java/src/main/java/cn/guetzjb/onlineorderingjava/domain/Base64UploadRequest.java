package cn.guetzjb.onlineorderingjava.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Base64UploadRequest {

    private String base64Str;
    private String suffix;

}
