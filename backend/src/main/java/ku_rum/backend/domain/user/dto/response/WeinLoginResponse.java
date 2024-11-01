package ku_rum.backend.domain.user.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeinLoginResponse {
    private boolean success;
    private String message;

    public WeinLoginResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}