package ku_rum.backend.domain.user.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserSaveRequest {

    @NotBlank(message = "아이디 입력은 필수입니다. 최소 6자 이상입니다.")
    @Size(min = 6)
    private String email;

    @NotBlank(message = "닉네임 입력은 필수입니다. 최대 8자 이하입니다.")
    @Size(max = 8)
    private String nickname;

    @NotNull(message = "비밀번호 입력은 필수입니다.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9]).{8,20}$", message = "비밀번호는 영어와 숫자를 포함해서 8자 이상 20자 이내로 입력해주세요.")
    private String password;

    @NotNull(message = "학번 입력은 필수입니다.")
    @Pattern(regexp = "^20(1[0-9]|2[0-5])\\d{5}$", message = "학번은 20으로 시작하고, 9자리여야 합니다.")
    private String studentId;

    @NotNull(message = "학과 입력은 필수입니다.")
    private String department;

    @Builder
    public UserSaveRequest(String email, String password, String studentId, String department, String nickname) {
        this.email = email;
        this.password = password;
        this.studentId = studentId;
        this.department = department;
        this.nickname = nickname;
    }

}
