package liar.memberservice.exception.type;

public class ExceptionMessage {
    public static final String USER_NOT_FOUND = "존재하지 않는 회원입니다.";
    public static final String USER_NOT_REFRESHTOKEN = "존재하지 않는 refreshToken 입니다,";
    public static final String USER_REGISTER_CONFLICT = "이미 존재하는 회원힙니다.";
    public static final String TOO_MANY_REQUEST = "너무 많은 요청으로 사이트에서 일시적 차단되었습니다.";
    public static final String BAD_REQUEST = "유효하지 않은 요청입니다.";
    public static final String INTERNAL_SERVER_ERROR = "일시적인 서버 에러입니다.";
    public static final String REDIS_ROCK_EXCEPTION = "다른 요청이 처리 중입니다.";
    public static final String DOUBLE_LOGIN = "중복 로그인 되었습니다. 로그인을 다시 시도 해주세요";
}
