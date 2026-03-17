package cn.guetzjb.onlineorderingjava.controller;

import cn.guetzjb.onlineorderingjava.domain.Base64UploadRequest;
import cn.guetzjb.onlineorderingjava.utils.UploadUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/upload")
public class UploadController {

    @PostMapping("/image")
    public ResponseEntity<String> uploadImage(MultipartFile file) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String today = dateFormat.format(new Date());
        String url = UploadUtil.uploadFile(today, file.getOriginalFilename(), file.getInputStream());
        return ResponseEntity.ok(url);
    }

    @PostMapping("/base64")
    public ResponseEntity<String> uploadImageByBase64(@RequestBody Base64UploadRequest request) {
        try {
            if (request == null || request.getBase64Str() == null || request.getBase64Str().isEmpty()) {
                return ResponseEntity.badRequest().body("Base64 编码字符串不能为空");
            }

            String base64Data = extractPureBase64(request.getBase64Str());
            if (base64Data.isEmpty()) {
                return ResponseEntity.badRequest().body("Base64 编码格式错误");
            }

            byte[] imageBytes = Base64.getDecoder().decode(base64Data.getBytes(StandardCharsets.UTF_8));
            InputStream inputStream = new ByteArrayInputStream(imageBytes);

            String fileName = generateFileName(request.getSuffix());
            if (fileName == null) {
                return ResponseEntity.badRequest().body("文件后缀不合法，仅支持 jpg/png/jpeg/webp");
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String today = dateFormat.format(new Date());
            String url = UploadUtil.uploadFile(today, fileName, inputStream);

            return ResponseEntity.ok(url);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Base64 解码失败：" + e.getMessage());
        }
    }

    /**
     * 提取纯 Base64 数据（去掉前缀 data:image/xxx;base64,）
     */
    private String extractPureBase64(String base64Str) {
        Pattern pattern = Pattern.compile("^data:image/\\w+;base64,");
        Matcher matcher = pattern.matcher(base64Str);
        if (matcher.find()) {
            return base64Str.substring(matcher.end());
        }
        return base64Str;
    }

    /**
     * 生成不重复的文件名
     *
     * @param suffix 文件后缀（如 png、jpg）
     */
    private String generateFileName(String suffix) {
        // 校验后缀合法性
        if (suffix == null || !isValidSuffix(suffix)) {
            return null;
        }
        return UUID.randomUUID().toString().replace("-", "") + "." + suffix;
    }

    /**
     * 校验文件后缀是否合法（仅支持常见图片格式）
     */
    private boolean isValidSuffix(String suffix) {
        return "jpg".equalsIgnoreCase(suffix)
                || "png".equalsIgnoreCase(suffix)
                || "jpeg".equalsIgnoreCase(suffix)
                || "webp".equalsIgnoreCase(suffix);
    }

    @GetMapping("/image/download")
    public ResponseEntity<byte[]> proxyImage(@RequestParam String url) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            String contentType = response.headers()
                    .firstValue("Content-Type")
                    .orElse("image/png");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(response.body());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}

