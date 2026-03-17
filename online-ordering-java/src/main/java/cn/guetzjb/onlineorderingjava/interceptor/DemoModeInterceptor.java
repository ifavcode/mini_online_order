package cn.guetzjb.onlineorderingjava.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashSet;
import java.util.Set;

@Component
public class DemoModeInterceptor implements HandlerInterceptor {

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Value("${demo.mode:false}")
    private boolean demoMode;

    private final Set<String> whiteUrls = new HashSet<>() {{
        add("/login");
        add("/upload/image");
        add("/upload/video");
        add("/upload/base64");
        add("/wx/**");
        add("/table/code2Name");
        add("/user/update");
        add("/orders/**");
    }};

    private final String excludeOrderPath = "/orders/admin/**";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        if (!demoMode) {
            return true;
        }

        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method) ||
                "HEAD".equalsIgnoreCase(method) ||
                "OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        String requestURI = request.getRequestURI();

        if (pathMatcher.match(excludeOrderPath, requestURI)) {
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":200,\"message\":\"操作成功，演示模式，数据不会发生变更\"}");
            return false;
        }

        for (String whiteUrl : whiteUrls) {
            if (pathMatcher.match(whiteUrl, requestURI)) {
                return true;
            }
        }

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":200,\"message\":\"操作成功，演示模式，数据不会发生变更\"}");
        return false;
    }

}
