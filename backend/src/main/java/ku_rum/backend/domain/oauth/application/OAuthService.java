package ku_rum.backend.domain.oauth.application;

import ku_rum.backend.domain.department.domain.Department;
import ku_rum.backend.domain.user.domain.User;
import ku_rum.backend.domain.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuthService {
  private final UserRepository userRepository;

  public Map<String, Object> getUserAttributes(OAuth2User oAuth2User) {
    if (oAuth2User == null) {
      return null;
    }
    Map<String, Object> attributes = oAuth2User.getAttributes();
    return attributes;

  }

  public Map<String, Object> saveUser(String department, String studentId, OAuth2User oAuth2User) {
    if (oAuth2User == null) {
      return null;
    }
    Map<String, Object> attributes = oAuth2User.getAttributes();

    // 학과 및 학번 추가
    attributes.put("department", department);
    attributes.put("studentId", studentId);

    User user = User.of(
            (String) attributes.get("email"),
            (String) attributes.get("nickname"),
            (String) attributes.get(null),
            (String) attributes.get("studentId"),
            (Department) attributes.get("department")
    );

    // DB에 저장
    userRepository.save(user);

    return attributes;
  }
}
