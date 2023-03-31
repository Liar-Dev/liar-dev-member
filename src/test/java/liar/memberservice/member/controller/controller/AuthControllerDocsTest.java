package liar.memberservice.member.controller.controller;

import com.google.gson.Gson;
import liar.memberservice.member.controller.dto.request.FormRegisterRequest;
import liar.memberservice.member.controller.dto.request.LoginFacadeRequest;
import liar.memberservice.member.controller.dto.request.LoginRequest;
import liar.memberservice.member.service.dto.AuthTokenDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static javax.management.openmbean.SimpleType.STRING;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerDocsTest extends CommonRestDocsController {

    static String email = "kose@naver.com";
    static String password = UUID.randomUUID().toString();
    static String username = "gosekose";

    static String remoteAddr = "127.0.0.1";

    @Test
    @DisplayName("RestDocs: register / Post")
    public void registerMvc() throws Exception {
        //given
        FormRegisterRequest request = FormRegisterRequest.builder()
                .email(email)
                .username(username)
                .password(password)
                .build();

        //when
        ResultActions perform = mockMvcPerformPost("/member-service/register", request);

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("성공"))
                .andDo(customDocument("member-register",
                        requestFields(
                                fieldWithPath("username").type(STRING).description("회원 이름"),
                                fieldWithPath("email").type(STRING).description("이메일"),
                                fieldWithPath("password").type(STRING).description("패스워드")
                        ),
                        responseCustomFields()
                ));

    }

    @Test
    @DisplayName("RestDocs: login / Post")
    public void loginMvc() throws Exception {
        //given
        registerMember();
        LoginRequest request = new LoginRequest(email, password);

        //when
        ResultActions perform = mockMvc.perform(
                post("/member-service/login")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(request))
                        .header("X-Forwarded-For", remoteAddr));

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.accessToken").isString())
                .andExpect(jsonPath("$.body.refreshToken").isString())
                .andExpect(jsonPath("$.body.userId").isString())
                .andDo(customDocument("member-login",
                        requestFields(
                                fieldWithPath("email").type(STRING).description("이메일"),
                                fieldWithPath("password").type(STRING).description("패스워드")
                        ),
                        responseCustomFields(
                                fieldWithPath("body.accessToken").type(STRING).description("액세스 토큰"),
                                fieldWithPath("body.refreshToken").type(STRING).description("리프래시 토큰"),
                                fieldWithPath("body.userId").type(STRING).description("유저 아이디")
                        )));

    }

    @Test
    @DisplayName("RestDocs: logout / Post")
    public void logoutMvc() throws Exception {
        //given
        AuthTokenDto loginTokens = loginMember();

        //when
        ResultActions perform = mockMvc.perform(
                customPost("/member-service/logout", null)
                        .header("Authorization", loginTokens.getAccessToken())
                        .header("RefreshToken", loginTokens.getRefreshToken())
                        .header("UserId", loginTokens.getUserId())
        );

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("성공"))
                .andDo(customDocument("member-logout",
                        responseCustomFields()
                ));
    }

    @Test
    @DisplayName("RestDocs: Reissue / Post")
    public void reissueMvc() throws Exception {
        //given
        AuthTokenDto loginTokens = loginMember();

        //when
        ResultActions perform = mockMvc.perform(
                customPost("/member-service/reissue", null)
                        .header("RefreshToken", loginTokens.getRefreshToken())
        );

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.accessToken").isString())
                .andExpect(jsonPath("$.body.refreshToken").isString())
                .andExpect(jsonPath("$.body.userId").isString())
                .andDo(customDocument("member-reissue",
                        responseCustomFields(
                                fieldWithPath("body.accessToken").type(STRING).description("액세스 토큰"),
                                fieldWithPath("body.refreshToken").type(STRING).description("리프래시 토큰"),
                                fieldWithPath("body.userId").type(STRING).description("유저 아이디")
                        )));
    }

    @Test
    @DisplayName("RestDocs: Users / Get")
    public void usersMvc() throws Exception {
        //given
        AuthTokenDto loginTokens = loginMember();

        //when
        ResultActions perform = mockMvcPerformGet("/member-service/users", null, loginTokens);

        //then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.email").value(email))
                .andExpect(jsonPath("$.body.username").value(username))
                .andDo(customDocument("member-users",
                        responseCustomFields(
                                fieldWithPath("body.email").type(STRING).description("회원 가입한 이메일"),
                                fieldWithPath("body.username").type(STRING).description("회원 가입한 유저 닉네임")
                        )));
    }


    private void registerMember() {
        facadeService.register(FormRegisterRequest.builder()
                .email(email)
                .username(username)
                .password(password)
                .build());
    }

    private AuthTokenDto loginMember() {
        registerMember();
        AuthTokenDto dto = null;
        try {
            dto = facadeService.login(new LoginFacadeRequest(email, password, remoteAddr));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dto;
    }


}