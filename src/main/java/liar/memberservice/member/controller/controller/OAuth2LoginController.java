package liar.memberservice.member.controller.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import liar.memberservice.common.principal.PrincipalUser;
import liar.memberservice.exception.exception.NotFoundUserException;
import liar.memberservice.member.service.FacadeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class OAuth2LoginController {

    private final FacadeService facadeService;

    /**
     *
     * 로그인 요청 콜백
     * 상태코드
     * NOT_FOUND: 400 (principalUser가 없을 경우, 혹은 인증이 안된 경우, UNAUTHORIZED 403이지만 404로 통일)
     * OK: 200 (로그인 완료, token, refreshToken 발급)
     *
     */
    @GetMapping("/")
    public ResponseEntity loginForProvideToken(@AuthenticationPrincipal PrincipalUser principalUser,
                                               Authentication authentication) throws JsonProcessingException {

        if (principalUser == null) throw new NotFoundUserException();
        return new ResponseEntity(facadeService.login(authentication), HttpStatus.OK);
    }

}
