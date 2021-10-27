package com.IndieAn.GoFundIndie.Service;

import com.IndieAn.GoFundIndie.Domain.DTO.UserModifyDTO;
import com.IndieAn.GoFundIndie.Domain.DTO.UserSIgnInDTO;
import com.IndieAn.GoFundIndie.Domain.DTO.UserSignUpDTO;
import com.IndieAn.GoFundIndie.Domain.Entity.User;
import com.IndieAn.GoFundIndie.Repository.UserRepository;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {
    private final UserRepository userRepository;
    @Value("#{info['gofundindie.signkey']}")
    private String SIGN_KEY;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 유저 정보를 받아 회원가입을 진행하는 서비스 기능
    public User CreateUserData(UserSignUpDTO userSignUpDTO) {
        // DB > User table에 유저정보를 저장한다.
        // 같은 email이 존재하면 null을 리턴
        if(userRepository.FindUserByEmail(userSignUpDTO.getEmail()) != null) return null;
        return userRepository.CreateUser(userSignUpDTO);
    }

    // 유저 정보를 확인하고 해당 유저의 정보를 수정하는 서비스 기능
    public User ModifyUserData(UserModifyDTO userModifyDTO, String email) {
        return userRepository.ModifyUser(userModifyDTO, userRepository.FindUserByEmail(email).getId());
    }

    // 유저 email을 통해 DB에 해당 email을 가진 엔티티를 삭제하는 서비스 기능
    public User DeleteUserData(String email) {
        return userRepository.DeleteUser(userRepository.FindUserByEmail(email).getId());
    }

    // 로그인 정보를 기준으로 email과 password를 확인해 유저정보를 찾는다
    public User FindUser(UserSIgnInDTO userSIgnInDTO) {
        User user = userRepository.FindUserByEmail(userSIgnInDTO.getEmail());
        // user가 없거나 비밀번호가 틀렸다면 null을 리턴한다.
        if(user == null || !user.getPassword().equals(userSIgnInDTO.getPassword())) {
            return null;
        }

        return user;
    }

    // 토큰에 존재하는 email을 통해 DB를 탐색한다.
    public User FindUserUseEmail(String email) {
        return userRepository.FindUserByEmail(email);
    }

    // AccessToken과 RefreshToken을 만드는 작업을 수행한다
    public String CreateToken(User user, Long time) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setHeader(createHeader())
                .setClaims(createClaims(user))
                .setExpiration(new Date(now.getTime() + Duration.ofSeconds(time).toMillis()))
                .signWith(SignatureAlgorithm.HS256, SIGN_KEY)
                .compact();
    }

    // 토큰의 header를 만드는 메소드
    private static Map<String, Object> createHeader() {
        Map<String, Object> header = new HashMap<>();
        header.put("typ", "JWT");
        header.put("alg", "HS256");
        return header;
    }

    // 토큰의 claim을 만드는 메소드
    private static Map<String, Object> createClaims(User user) {
        // 공개 클레임에 사용자의 이메일을 설정하여 정보를 조회할 수 있다.
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        return claims;
    }

    // 토큰 유효성 검증
    public Map<String, String> CheckToken(String token) {
        HashMap<String, String> msg = new HashMap<>();
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SIGN_KEY)
                    .parseClaimsJws(token)
                    .getBody();

            msg.put("email", (String) claims.get("email"));
            return msg;
        } catch (ExpiredJwtException e) {
            msg.put("code", "4101");
            return msg;
        } catch (JwtException e) {
            msg.put("code", "4100");
            return msg;
        }
    }

    // 쿠키에서 해당 토큰 값을 찾기 위한 메소드
    public String getStringCookie(Cookie[] cookies, String cookiesResult, String token) {
        for(Cookie i : cookies) {
            if(i.getName().equals(token)) {
                cookiesResult = i.getValue();
                break;
            }
        }
        return cookiesResult;
    }

    // 해당 토큰을 만료시키는 메소드
    public void ExpirationToken(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(token, null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
