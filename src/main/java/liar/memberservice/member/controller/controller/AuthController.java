package liar.memberservice.member.controller.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import liar.memberservice.member.controller.dto.request.FormRegisterRequest;
import liar.memberservice.member.controller.dto.request.LoginRequest;
import liar.memberservice.member.controller.dto.response.SendSuccess;
import liar.memberservice.member.controller.dto.response.SendSuccessBody;
import liar.memberservice.member.controller.util.RequestMapperFactory;
import liar.memberservice.member.service.FacadeService;
import liar.memberservice.member.service.dto.AuthTokenDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@CrossOrigin({"http://localhost:3000", "http://localhost:8000", "http://3.39.187.39:8000"})
@RequestMapping("/member-service")
@RequiredArgsConstructor
public class AuthController {

    private final FacadeService facadeService;

    /**
     * 회원 가입 요청
     */
    @PostMapping("/register")
    public ResponseEntity formRegister(@Validated @RequestBody FormRegisterRequest request) {
        facadeService.register(request);
        return ResponseEntity.ok(SendSuccess.of());
    }

    @PostMapping("/login")
    public ResponseEntity login(@Validated @RequestBody LoginRequest request,
                                @RequestHeader("X-Forwarded-For") String remoteAddr) throws JsonProcessingException {
        AuthTokenDto authToken = facadeService
                .login(RequestMapperFactory.mapper(request, remoteAddr.split(",")[0].trim()));
        return ResponseEntity.ok().body(SendSuccessBody.of(authToken));
    }

    @PostMapping("/logout")
    public ResponseEntity logout(
            @RequestHeader("Authorization") String accessToken,
            @RequestHeader("RefreshToken") String refreshToken,
            @RequestHeader("UserId") String userId) throws JsonProcessingException {

        facadeService.logout(accessToken, refreshToken, userId);
        return ResponseEntity.ok().body(SendSuccess.of());
    }


    @PostMapping("/reissue")
    public ResponseEntity reissueToken(
            @RequestHeader("RefreshToken") String refreshToken) throws JsonProcessingException {
        return ResponseEntity.ok()
                .body(SendSuccessBody.of(facadeService.reissue(refreshToken)));

    }

    @GetMapping("/users")
    public ResponseEntity getMemberInfo(
            @RequestHeader("Authorization") String accessToken,
            @RequestHeader("RefreshToken") String refreshToken,
            @RequestHeader("UserId") String userId
    ) {
        return ResponseEntity.ok()
                .body(SendSuccessBody.of(facadeService.getMemberInfo(userId)));
    }

}
