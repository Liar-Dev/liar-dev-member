package liar.memberservice.member.service.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthTokenDto {

    String accessToken;

    String refreshToken;
    String userId;

    @Builder
    public AuthTokenDto(String accessToken, String refreshToken, String userId) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
    }

    public static AuthTokenDto of (String accessToken, String refreshToken, String userId) {
        return new AuthTokenDto(accessToken, refreshToken, userId);
    }
}
