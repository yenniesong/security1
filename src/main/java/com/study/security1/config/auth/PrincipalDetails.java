package com.study.security1.config.auth;

import com.study.security1.model.User;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

// 시큐리티가 /login 주소 요청이 오면 낚아채서 로그인을 진행시킴
// 로그인을 진행이 완료가 되면 시큐리티 session을 만들어줌 (Security ContextHolder)
// 시큐리티 session안에 들어갈 수 있는 오브젝트 => Authentication 타입의 객체
// Authentication 안에 User 정보가 있어야 됨.
// User 오브젝트 타입 => UserDetailes 타입 객체
// Security Session => Authentication => UserDetails(PrincipalDetails)
@Data
public class PrincipalDetails implements UserDetails, OAuth2User {

    private User user; // 콤포지션 변수
    private Map<String, Object> attributes;

    // 생성자 (일반 로그인)
    public PrincipalDetails(User user){
        this.user = user;
    }
    // 생성자 (OAuth 로그인)
    public PrincipalDetails(User user, Map<String, Object> attributes){
        this.user = user;
        this.attributes = attributes;
    }

    // 해당 User의 권한을 리턴하는 곳 (ex. user.getRole();) 하지만 role이 String 타입이기 때문에 여기서 return 할 수 없음
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        Collection<GrantedAuthority> collect = new ArrayList<>();
        collect.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return user.getRole();
            }
        });
        return collect;
    }
    @Override
    public String getPassword(){
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 우리 사이트 1년동안 회원이 로그인을 안하면? 휴먼계정으로 전환
        // 그럼 user 모델에 private Timestamp loginDate가 선언 되어있어야함
        // user.getLoginDate()를 가져옴
        // 현재시간 - 로그인 시간 => 1년을 초과하면 reutnr false 이런식으로 사용 (지금은 해당 안 됨)
        return true;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return null;
    }
}
