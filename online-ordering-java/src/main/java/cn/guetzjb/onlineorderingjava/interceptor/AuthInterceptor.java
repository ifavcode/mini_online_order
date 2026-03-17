package cn.guetzjb.onlineorderingjava.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Value("${sa-token.token-name}")
    private String tokenName;

    private final Set<String> whiteUrls = new HashSet<>() {{
        add("/login");
        add("/upload/image");
        add("/upload/video");
        add("/upload/base64");
        add("/wx/user/**/login");
        add("/wx/pay/notify");
        add("/wx/pay/refund/notify");
        add("/table/code2Name");
    }};

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws IOException {
        String method = request.getMethod();
        if ("GET".equals(method)) {
            return true;
        }

        String requestURI = request.getRequestURI();
        for (String whiteUrl : whiteUrls) {
            if (pathMatcher.match(whiteUrl, requestURI)) {
                return true;
            }
        }

        String token = request.getHeader(tokenName);


        // StpUtil.isLogin() 内部已经校验token是否为伪造
        if (token != null && StpUtil.isLogin()) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"未授权\"}");
        return false;
    }

}
