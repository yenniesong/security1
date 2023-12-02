package com.study.security1.config;

import com.study.security1.config.oauth.PrincipalOauth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)  // secured 어노테이션 활성화, preAuthorize 어노테이션 활성화
public class SecurityConfig {

    @Autowired
    private PrincipalOauth2UserService principalOauth2UserService;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){
        return web -> web.ignoring().antMatchers("/resources/**");
    }

    @Bean   // 해당 메서드의 리턴되는 오브젝트를 loC로 등록해줌
    public BCryptPasswordEncoder encodePwd(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf().disable()
                .headers()
                    .frameOptions().disable().and()
                .authorizeRequests()
                    .antMatchers("/user/**").authenticated()    // "/user/**" 로 가면 인증을 해야 한다, 인증만 되면 들어갈 수 있는 주소
                    .antMatchers("/manager/**").access("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
                    .antMatchers("/admin/**").access("hasRole('ROLE_ADMIN')")
                    .anyRequest().permitAll().and() // 나머지 권한은 모두 허용
                .formLogin()
                    .loginPage("/loginForm")
                    .loginProcessingUrl("/login")   // "loginProcessingUrl" : /login 주소가 호출이 되면 시큐리티가 낚아채서 대신 로그인을 진행해줌
                    .defaultSuccessUrl("/").and()
                .oauth2Login()
                    .loginPage("/loginForm")    // 구글 로그인 완료된 뒤의 후처리가 필요함
                    .userInfoEndpoint()
                    .userService(principalOauth2UserService).and()
                .and()
                .build();
    }

}
