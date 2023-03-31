package liar.memberservice.member.controller.filter;

import com.google.gson.Gson;
import liar.memberservice.member.controller.controller.AuthController;
import liar.memberservice.member.controller.dto.request.FormRegisterRequest;
import liar.memberservice.member.controller.dto.request.LoginRequest;
import liar.memberservice.member.repository.rdbms.AuthorityRepository;
import liar.memberservice.member.repository.rdbms.MemberRepository;
import liar.memberservice.member.service.FacadeService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class LoginSessionFilterTest {

    @Autowired FacadeService facadeService;
    @Autowired MemberRepository memberRepository;
    @Autowired RedisTemplate redisTemplate;
    @Autowired FilterRegistrationBean<LoginSessionFilter> loginSessionFilter;
    @Autowired AuthorityRepository authorityRepository;

    MockMvc mockMvc;

    String email = "kose@naver.com";
    String password = "abcdefg123456";
    String username = "gosekose";
    String remoteAddr = "127.0.0.1";

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(facadeService))
                .addFilter(loginSessionFilter.getFilter())
                .build();
    }

    @AfterEach
    public void tearDown() {
        redisTemplate.delete(redisTemplate.keys("*"));
        authorityRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("정상 요청에 대해 1회 실행 테스트를 진행")
    public void doNormalRequest() throws Exception {
        //given
        register();

        //when
        ResultActions actions = mockMvc.perform(post("/member-service/login")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .accept(MediaType.APPLICATION_JSON)
                .content(new Gson().toJson(new LoginRequest(email, password)))
                .header("X-Forwarded-For", remoteAddr));

        //then
        actions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("성공"))
                .andExpect(jsonPath("$.body.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.body.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.body.userId").isNotEmpty());
    }

    @Test
    @DisplayName("같은 ip로 10초동안 여러번 요청한다. -> TooManyRequest 예외가 발생한다.")
    public void requestSameAddr_faile_TooManyRequest() throws Exception {
        //given
        register();

        //when
        int firstErrorCount = 0;
        boolean isFirst = true;
        for (int i = 0; i < 6; i++) {

            try {
                ResultActions actions = mockMvc.perform(post("/member-service/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .accept(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(new LoginRequest(email, password)))
                        .header("X-Forwarded-For", remoteAddr));
            } catch (Exception e) {
                e.printStackTrace();
                if (isFirst) {
                    firstErrorCount = i;
                    isFirst = false;
                }
            }
        }

        //then
        assertThat(firstErrorCount).isGreaterThanOrEqualTo(4);
    }


    private void register() {
        facadeService.register(FormRegisterRequest.builder()
                .email(email)
                .password(password)
                .username(username)
                .build());
    }

}