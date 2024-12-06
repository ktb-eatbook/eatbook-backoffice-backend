package com.eatbook.backoffice.security.config;

import com.eatbook.backoffice.security.auth.filter.JwtAuthorizationFilter;
import com.eatbook.backoffice.security.auth.filter.JwtExceptionFilter;
import com.eatbook.backoffice.security.auth.filter.SameSiteCookieFilter;
import com.eatbook.backoffice.security.auth.jwt.CustomUserDetailsService;
import com.eatbook.backoffice.security.error.handler.CustomAccessDeniedHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static com.eatbook.backoffice.entity.constant.Role.ADMIN;
import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthorizationFilter jwtAuthorizationFilter;
    private final AuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final JwtExceptionFilter jwtExceptionFilter;
    private final CustomUserDetailsService userDetailsService;
    private final SameSiteCookieFilter sameSiteCookieFilter;

    @Value("${allowed.origins}")
    private String allowedOrigins;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Spring Security를 위한 SecurityFilterChain 빈 구성.
     * 이 빈은 애플리케이션의 보안 설정, 인증, 권한 부여, CORS, 세션 관리, 그리고 예외 처리를 설정합니다.
     *
     * @param http 보안을 구성하기 위한 HttpSecurity 인스턴스.
     * @param customAccessDeniedHandler 권한 없는 액세스를 처리하기 위한 사용자 정의 접근 거부 핸들러.
     *
     * @return HttpSecurity.HttpBuilder.build() 메서드로 빌드된 SecurityFilterChain.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(withDefaults())
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers("/swagger*/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/api/master/**").hasAuthority(ADMIN.name())
                        .requestMatchers("/api/admin/**").hasAnyAuthority(ADMIN.name())
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtExceptionFilter, JwtAuthorizationFilter.class)
                .addFilterAfter(sameSiteCookieFilter, JwtExceptionFilter.class);

        return http.build();
    }

    /**
     * DaoAuthenticationProvider를 빈으로 등록합니다.
     * 이 클래스는 사용자의 이메일을 기반으로 사용자 정보를 로드하고,
     * BCryptPasswordEncoder를 사용하여 비밀번호를 해싱합니다.
     *
     * @return DaoAuthenticationProvider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * CORS(Cross-Origin Resource Sharing) 설정을 구성하기 위한 빈을 만듭니다.
     * 이 빈은 {@link CorsConfigurationSource} 인스턴스를 생성하며, CORS 정책을 정의합니다.
     * CORS 정책은 특정 원본, 메소드, 헤더, 그리고 자격 증명을 허용합니다.
     *
     * @return 구성된 CORS 정책을 포함하는 {@link CorsConfigurationSource} 인스턴스.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "TRACE"));
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}