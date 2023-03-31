package liar.memberservice.member.controller.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import liar.memberservice.member.repository.rdbms.AuthorityRepository;
import liar.memberservice.member.repository.rdbms.MemberRepository;
import liar.memberservice.member.repository.redis.TokenRepository;
import liar.memberservice.member.service.FacadeService;
import liar.memberservice.member.service.dto.AuthTokenDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static javax.management.openmbean.SimpleType.STRING;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

@SpringBootTest
@AutoConfigureWebMvc
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "docs.api.com", uriPort = 443)
@ExtendWith(RestDocumentationExtension.class)
public class CommonRestDocsController {

    protected MockMvc mockMvc;

    @Autowired RedisTemplate redisTemplate;

    @Autowired MemberRepository memberRepository;

    @Autowired TokenRepository tokenRepository;

    @Autowired FacadeService facadeService;

    @Qualifier("redisObjectMapper")
    @Autowired ObjectMapper objectMapper;
    @Autowired
    private AuthorityRepository authorityRepository;

    static String remoteAddr = "127.0.0.1";

    @BeforeEach
    public void init(WebApplicationContext webApplicationContext,
                     RestDocumentationContextProvider restDocumentationContextProvider) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentationContextProvider))
                .build();
    }

    @AfterEach
    public void tearDown() {
        authorityRepository.deleteAll();
        memberRepository.deleteAll();
        redisTemplate.delete(redisTemplate.keys("*"));
    }


    public RestDocumentationResultHandler customDocument(String identifier,
                                                         Snippet... snippets) {
        return document(
                identifier,
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                snippets
        );
    }



    public ResponseFieldsSnippet responseCustomFields(FieldDescriptor... fieldDescriptors) {
        FieldDescriptor[] defaultFieldDescriptors = new FieldDescriptor[] {
                fieldWithPath("code").type(STRING).description("응답 상태 코드"),
                fieldWithPath("message").type(STRING).description("상태 메세지")
        };

        return responseFields(defaultFieldDescriptors).and(fieldDescriptors);
    }

    public <T> MockHttpServletRequestBuilder customGet(String uri, T t, AuthTokenDto auth) {
        return get(uri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new Gson().toJson(t))
                .header("Authorization", auth.getAccessToken())
                .header("RefreshToken", auth.getRefreshToken())
                .header("UserId", auth.getUserId());
    }

    public <T> ResultActions mockMvcPerformGet(String uri, T t, AuthTokenDto auth) throws Exception {
        return mockMvc.perform(customGet(uri, t, auth));
    }

    public <T> MockHttpServletRequestBuilder customPost(String uri, T t) {
        return post(uri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new Gson().toJson(t));
    }

    public <T> ResultActions mockMvcPerformPost(String uri, T t) throws Exception {
        return mockMvc.perform(customPost(uri, t));
    }

    public <T> MockHttpServletRequestBuilder customPost(String uri, T t, AuthTokenDto auth) {
        return post(uri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new Gson().toJson(t))
                .header("Authorization", auth.getAccessToken())
                .header("RefreshToken", auth.getRefreshToken())
                .header("UserId", auth.getUserId());

    }

    public <T> ResultActions mockMvcPerformPost(String uri, T t, AuthTokenDto auth) throws Exception {
        return mockMvc.perform(customPost(uri, t, auth));
    }

}
