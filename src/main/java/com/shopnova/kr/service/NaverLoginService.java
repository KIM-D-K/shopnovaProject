package com.shopnova.kr.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.shopnova.kr.domain.User;
import com.shopnova.kr.repository.UserRepository;

@Service
public class NaverLoginService {

    private String clientId = "gpKCOAznkH9Ykjj6fSuW";  // 네이버 클라이언트 ID
    private String clientSecret = "l1Nj_6qSPd";         // 네이버 클라이언트 Secret
    private String redirectUri = "http://localhost:8081/users/naverLoginCallback"; // 네이버 로그인 리디렉션 URI

    @Autowired
    private UserRepository userRepository;  // userRepository 주입

 // 네이버 로그인 후 사용자 정보 가져오기
    public String getUserInfo(String code, String state, Model model) throws Exception {
        // 인증 코드로 액세스 토큰 받아오기
        String accessToken = getAccessToken(code, state);  

        // 네이버 API 호출하여 사용자 정보 받아오기
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = "https://openapi.naver.com/v1/nid/me";
        ResponseEntity<String> response = restTemplate.getForEntity(apiUrl + "?access_token=" + accessToken, String.class);

        // 응답에서 이메일 추출
        String email = parseEmail(response.getBody());

        // 이메일로 기존 사용자 존재 여부 확인
        User existingUser = userRepository.findByEmail(email);

        if (existingUser != null) {
            // 기존 사용자가 있으면 메시지 전달 후 회원가입 완료 페이지로 이동
            model.addAttribute("signupMessage", "이미 회원 가입된 회원입니다.");
            return "signupComplete"; // Thymeleaf 템플릿 이름 (signupComplete.html)
        } else {
            // 기존 사용자 없으면 새로운 사용자 생성 및 저장
            naverSignUpAndLogin(email);
            model.addAttribute("signupMessage", "회원가입이 완료되었습니다.");
            return "signupComplete";
        }
    }


    // 인증 코드로 액세스 토큰을 받아오는 메서드
    private String getAccessToken(String code, String state) {
        String apiUrl = "https://nid.naver.com/oauth2.0/token";
        
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(apiUrl)
            .queryParam("client_id", clientId)
            .queryParam("client_secret", clientSecret)
            .queryParam("code", code)
            .queryParam("state", state)
            .queryParam("grant_type", "authorization_code");

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(uriBuilder.toUriString(), String.class);

        // 액세스 토큰을 JSON 응답에서 파싱
        return parseAccessToken(response.getBody());  // 응답에서 액세스 토큰 추출
    }

    private String parseAccessToken(String responseBody) {
        // JSON 응답에서 access_token을 추출 (예: {"access_token":"your_access_token", ...})
        JSONObject jsonResponse = new JSONObject(responseBody);
        return jsonResponse.getString("access_token");  // "access_token" 값을 추출
    }

    private String parseEmail(String responseBody) {
        // JSON 응답에서 이메일을 추출 (예: {"response": {"email": "user@naver.com"}})
        JSONObject jsonResponse = new JSONObject(responseBody);
        return jsonResponse.getJSONObject("response").getString("email");
    }

    // 고정된 값으로 사용자 생성 및 저장
    public User naverSignUpAndLogin(String email) {
        // 고정된 사용자 정보를 설정하여 새로운 사용자 생성
        User newUser = new User();
        newUser.setEmail(email);   // 네이버에서 가져온 이메일 사용
        newUser.setUserId("americanian");             // 고정된 사용자 ID
        newUser.setPhone("010-1234-5678");            // 고정된 전화번호
        newUser.setAddress("부천시");           // 고정된 주소
        newUser.setName("김효태");                  // 고정된 이름
        newUser.setPassword("123456");                // 고정된 비밀번호

        // 사용자 저장 (기존 사용자 여부 상관없이 항상 저장)
        userRepository.save(newUser);  // DB에 새로운 사용자 저장

        return newUser;  // 새로 생성한 사용자 반환
    }
}

