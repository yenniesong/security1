package com.study.security1.config.oauth;

import com.study.security1.config.auth.PrincipalDetails;
import com.study.security1.config.oauth.provider.FacebookUserInfo;
import com.study.security1.config.oauth.provider.GoogleUserInfo;
import com.study.security1.config.oauth.provider.NaverUserInfo;
import com.study.security1.config.oauth.provider.OAuth2UserInfo;
import com.study.security1.model.User;
import com.study.security1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 구글로 부터 받은 userRequest 데이터에 대한 후처리 함수
        // 함수 종료시 @AuthenticationPrincipal 어노테이션이 생성됨
        System.out.println("getClientRegistration : " + userRequest.getClientRegistration());   // registrationId로 어떤 OAuth로 로그인했는지 확인 가능.
        System.out.println("getAccessToken : " + userRequest.getAccessToken());

        OAuth2User oauth2User = super.loadUser(userRequest);
        // 구글 로그인 버튼 클릭 -> 구글 로그인창 -> 로그인 완료 -> code를 리턴(OAuth-Client 라이브러리) -> AccessToken 요청 (AccessToken을 받는 거 까지가 userRequest 정보)
        // userRequest 정보로 무엇을 할까? -> loadUser 함수 호출 -> 구글로부터 회원 프로필 받음
        System.out.println("getAttributes : " + super.loadUser(userRequest).getAttributes());

        // 회원가입을 강제로 진행
        OAuth2UserInfo oAuth2UserInfo = null;
        if (userRequest.getClientRegistration().getRegistrationId().equals("google")) {
            System.out.println("구글 로그인 요청");
            oAuth2UserInfo = new GoogleUserInfo(oauth2User.getAttributes());
        } else if (userRequest.getClientRegistration().getRegistrationId().equals("facebook")) {
            System.out.println("페이스북 로그인 요청");
            oAuth2UserInfo = new FacebookUserInfo(oauth2User.getAttributes());
        } else if (userRequest.getClientRegistration().getRegistrationId().equals("naver")) {
            System.out.println("네이버 로그인 요청");
            oAuth2UserInfo = new NaverUserInfo((Map)oauth2User.getAttributes().get("response"));
        } else {
            System.out.println("우리는 구글과 페이스북, 네이버만 지원함미다용");
        }
        String provider = oAuth2UserInfo.getProvider();
        String providerId = oAuth2UserInfo.getProviderId();
        String username = provider + "_" + providerId;  // google_숫자
        String password = bCryptPasswordEncoder.encode("겟인데어");
        String email = oAuth2UserInfo.getEmail();
        String role = "ROLE_USER";

        // 무조건 회원가입 하면 안됨. 이미 되어있을수도 있음
        User userEntity = userRepository.findByUsername(username);  // 확인 해보기

        if (userEntity == null) {
            // 못 찾음
            System.out.println("OAuth 로그인이 최초입니다.");
            userEntity = User.builder()
                    .username(username)
                    .password(password)
                    .email(email)
                    .role(role)
                    .provider(provider)
                    .providerId(providerId)
                    .build();
            userRepository.save(userEntity);
        } else {
            System.out.println("로그인을 이미 한적이 있습니다. 당신은 자동 회원가입이 되어 있습니다.");
        }

        return new PrincipalDetails(userEntity, oauth2User.getAttributes());  // 왜 가능할까? -> OAuth2User가 리턴 타입이여서
    }
}
