package com.study.security1.config.auth;

import com.study.security1.model.User;
import com.study.security1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// 시큐리티 설정에서 loginProcessingUrl("/login");으로 걸어놓음
// /login 요청이 오면 자동으로 UserDetailsService 타입으로 loC되어 있는 loadUserByUsername 함수가 실행 (규칙)
@Service
public class PrincipalDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    // 아래 UserDetails 함수가 return을 하면 어디쪽으로 return 될 까?
    // Security Session 내부 (Authentication 내부 (UserDetails))
    // 함수 종료시 @AuthenticationPrincipal 어노테이션이 생성됨
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("username : " + username);
        // 여기서 넘어온 파라미터 username은 반드시 loginForm.html에서 넘겨주는 데이터 name="username"과 동일해야함
        // 저 위에 userRepository에 이름이 있는지(=username이 user에 있는지) 확인해주기 위한 것
        User userEntity = userRepository.findByUsername(username);  // 기본적인 crud 뿐이라 repository에 만들어줘야함

        if (userEntity != null) {
            return new PrincipalDetails(userEntity);
        }
        return null;
    }
}
