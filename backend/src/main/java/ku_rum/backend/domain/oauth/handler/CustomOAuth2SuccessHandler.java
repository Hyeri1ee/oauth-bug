package ku_rum.backend.domain.oauth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException {
    log.info("OAuth2 로그인후, 리다이렉트 실행");

    if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
      log.info("🚀 AJAX 요청 감지, JSON 응답 반환");
      response.setContentType("application/json;charset=UTF-8");
      response.getWriter().write("{\"redirectUrl\": \"/oauth/loginInfo\"}");
      response.flushBuffer();
      return;
    }

    response.sendRedirect("/oauth/loginInfo");
  }
}
