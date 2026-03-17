package cn.guetzjb.onlineorderingjava.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpLogic;
import cn.guetzjb.onlineorderingjava.interceptor.AuthInterceptor;
import cn.guetzjb.onlineorderingjava.interceptor.DemoModeInterceptor;
import cn.guetzjb.onlineorderingjava.service.WebSocketServer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

// 配置类：用于提供静态资源访问
@Configuration
@EnableConfigurationProperties(ServerProperties.class)
public class WebConfig implements WebMvcConfigurer {


    private final AuthInterceptor authInterceptor;
    private final DemoModeInterceptor demoModeInterceptor;

    public WebConfig(AuthInterceptor authInterceptor, DemoModeInterceptor demoModeInterceptor) {
        this.authInterceptor = authInterceptor;
        this.demoModeInterceptor = demoModeInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(demoModeInterceptor)
                .addPathPatterns("/**");
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**");
        registry.addInterceptor(new SaInterceptor())
                .addPathPatterns("/**");
    }

    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        ServerEndpointExporter exporter = new ServerEndpointExporter();
        exporter.setAnnotatedEndpointClasses(WebSocketServer.class);
        return exporter;
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        config.addAllowedOrigin("http://192.168.0.108:5173");
        config.addAllowedOrigin("http://localhost:5173");

        config.setAllowCredentials(true);

        config.addAllowedMethod(CorsConfiguration.ALL);

        config.addAllowedHeader(CorsConfiguration.ALL);

        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
