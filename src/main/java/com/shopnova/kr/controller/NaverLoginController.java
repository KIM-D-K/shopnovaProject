package com.shopnova.kr.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.shopnova.kr.domain.User;
import com.shopnova.kr.service.NaverLoginService;
import com.shopnova.kr.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/users")
public class NaverLoginController {
	
	@Autowired
    private NaverLoginService naverLoginService; // 서비스 주입


    private final String clientId = "gpKCOAznkH9Ykjj6fSuW";  // 네이버 클라이언트 ID
    private final String clientSecret = "l1Nj_6qSPd";  // 네이버 클라이언트 시크릿
    private final String redirectUri = "http://localhost:8081/users/naverLoginCallback";  // 네이버 로그인 리디렉션 URI
    private final String USER_INFO_URL = "https://openapi.naver.com/v1/nid/me";  // 네이버 사용자 정보 API URL

    @GetMapping("/naverLoginCallback")
    public String naverLoginCallback(@RequestParam("code") String code, 
                                     @RequestParam("state") String state, 
                                     HttpSession session, 
                                     Model model) {
        try {
            // 네이버 로그인 후 사용자 정보 가져오기 (서비스 활용)
            return naverLoginService.getUserInfo(code, state, model);
        } catch (Exception e) {
            model.addAttribute("signupMessage", "네이버 로그인 중 오류가 발생했습니다.");
            return "signupComplete";
        }
    }

}

