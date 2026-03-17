package cn.guetzjb.onlineorderingjava.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
public class SystemController {

    @Value("${demo.mode}")
    private Boolean demoMode;

    @GetMapping("/demo")
    public ResponseEntity<Boolean> demo() {
        return ResponseEntity.ok(demoMode);
    }
}
