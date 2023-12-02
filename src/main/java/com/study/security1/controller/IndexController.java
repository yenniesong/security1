package com.study.security1.controller;

import com.study.security1.config.auth.PrincipalDetails;
import com.study.security1.model.User;
import com.study.security1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller // view를 리턴하겠다!
public class IndexController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping("/test/login")
    public @ResponseBody String testLogin(Authentication authentication, @AuthenticationPrincipal PrincipalDetails userDetails){   // DI(의존성 주입)
        System.out.println("/test/login ============================");
        PrincipalDetails principalDetails = (PrincipalDetails)authentication.getPrincipal();
        System.out.println("authentication : " + principalDetails.getUser());

        System.out.println("userDetails : " + userDetails.getUser());

        return "세션 정보 확인";
    }

    @GetMapping("/test/oauth/login")
    public @ResponseBody String testOAuthLogin(Authentication authentication, @AuthenticationPrincipal OAuth2User oauth){   // DI(의존성 주입)
        System.out.println("/test/oauth/login ============================");
        OAuth2User oauth2User = (OAuth2User)authentication.getPrincipal();
        System.out.println("authentication : " + oauth2User.getAttributes());
        System.out.println("oauth2User : " + oauth.getAttributes());

        return "OAuth 세션 정보 확인";
    }

    @GetMapping({"", "/"})  // localhost:8080/, localhost:8080
    public String index(){
        // "index"는 여기서 view 페이지
        // 머스테치 기본 폴더 -> src/main/resources/
        // 뷰리졸버 설정 -> templates (prefix), .mustache (suffix) but, 생략 가능
        return "index"; // src/main/resources/templates/index.mustache로 찾음 그러므로 바꿔줘야함
    }

    // OAuth 로그인을 해도 PrincipalDetails
    // 일반 로그인을 해도 PrincipalDetails
    @GetMapping("/user")
    public @ResponseBody String user(@AuthenticationPrincipal PrincipalDetails principalDetails){
        System.out.println("principalDetails : " + principalDetails.getUser());
        return "user";
    }
    @GetMapping("/admin")
    public @ResponseBody String admin(){
        return "admin";
    }
    @GetMapping("/manager")
    public @ResponseBody String manager(){
        return "manager";
    }
    @GetMapping("/loginForm")   // 초기 상태에서는 스프링 시큐리티가 해당주소를 낚아챔
    public String loginForm(){
        return "loginForm";
    }
    @GetMapping("/joinForm")
    public String joinForm(){
        return "joinForm";
    }
    @PostMapping("/join")
    public String join(User user){
        System.out.println(user);

        user.setRole("ROLE_USER");

        String rawPassword = user.getPassword();
        String encPassword = bCryptPasswordEncoder.encode(rawPassword);
        user.setPassword(encPassword);

        userRepository.save(user);  // 회원가입 잘 됨. 하지만 이렇게 하면 안 됨. 왜? 비밀번호 : 1234 가 그냥 들어감
                                    // => 시큐리티로 로그인 할 수 없음. 왜? 패스워드가 암호화가 안되었기 때문

        return "redirect:/loginForm";
    }
    @Secured("ROLE_ADMIN")
    @GetMapping("/info")
    public @ResponseBody String info(){
        return "개인정보";
    }

    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')") // data 라는 메소드가 실행되기 직전에 실행
    @GetMapping("/data")
    public @ResponseBody String data(){
        return "데이터 정보";
    }
}
