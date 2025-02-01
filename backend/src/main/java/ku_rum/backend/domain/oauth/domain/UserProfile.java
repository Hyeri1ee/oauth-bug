package ku_rum.backend.domain.oauth.domain;

import ku_rum.backend.domain.department.domain.Department;
import ku_rum.backend.domain.department.domain.repository.DepartmentRepository;
import ku_rum.backend.domain.user.domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserProfile {
    private String id;
    private String username; // 사용자 이름
    private String provider; // 로그인한 서비스
    private String email; // 사용자의 이메일
    private Department department; //사용자의 학부
    private String studentId; //사용자의 학번

    public void setId(String id) {
        this.id = id;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public void setUserName(String userName) {
        this.username = userName;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    // DTO 파일을 통하여 Entity를 생성하는 메소드
    public User toEntity() {
        return User.builder()
                .email(this.email) // email 설정
                .nickname(this.username) // username을 nickname으로 설정
                .password(null) // OAuth 로그인에서는 비밀번호는 null로 처리
                .department(this.department) // department는 설정
                .studentId(this.studentId) // 학번 설정
                .build();
    }
}
