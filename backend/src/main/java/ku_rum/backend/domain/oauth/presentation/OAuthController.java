package ku_rum.backend.domain.oauth.presentation;

import ku_rum.backend.domain.oauth.application.OAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/oauth")
@RequiredArgsConstructor
public class OAuthController {
  private final OAuthService oAuthService;

  @GetMapping("/loginInfo")
  public String afterLogin(
          @AuthenticationPrincipal OAuth2User oAuth2User,
          Model model
  ) {
    if (oAuth2User != null) {
      model.addAttribute("userInfo", oAuth2User.getAttributes());
    } else {
      return "redirect:/"; // 로그인되지 않으면 메인 페이지로 리다이렉트
    }
    return "oauth/loginInfo";
  }

  @PostMapping("/loginInfo")
  public String processOAuthInfo(
          @AuthenticationPrincipal OAuth2User oAuth2User,
          @RequestParam String department,
          @RequestParam String studentId,
          Model model
  ) {

    Map<String, Object> attributes = oAuthService.saveUser(department,studentId,oAuth2User);
    model.addAttribute("userInfo", attributes);

    return "oauth/oauthInfo"; // 최종 저장된 user 정보 표시
  }

}
