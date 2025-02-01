package ku_rum.backend.domain.oauth.presentation;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequestMapping("/pre-oauth")
public class PreOAuthController {

  @GetMapping("/info")
  public String showPreOAuthForm() {
    return "preoauthinfo";
  }


  @PostMapping("/info")
  public String processPreOAuthInfo(
          @RequestParam String department,
          @RequestParam String studentId,
          HttpServletResponse response
  ) {
    // 쿠키에 정보 저장
    Cookie cookie = new Cookie("department", department);
    cookie.setAttribute("studentId" , studentId);

    cookie.setPath("/"); //사이트 내 모든 경로에서 쿠키 전송
    cookie.setMaxAge(10 * 60); //유효시간 : 10분
    cookie.setHttpOnly(false);
    cookie.setAttribute("SameSite", "Lax"); //크로스 도메인에서도 유지

    response.addCookie(cookie);
    log.info("cookie에 학과 저장됨");

    // OAuth2 로그인 페이지로 리다이렉트
    return "redirect:/oauth2/authorization/google";
  }
}
