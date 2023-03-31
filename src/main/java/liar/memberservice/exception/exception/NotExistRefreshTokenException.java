package liar.memberservice.exception.exception;

import liar.memberservice.exception.type.ExceptionCode;
import liar.memberservice.exception.type.ExceptionMessage;

public class NotExistRefreshTokenException extends CommonException {

    public NotExistRefreshTokenException() {
        super(ExceptionCode.UNAUTHORIZED, ExceptionMessage.USER_NOT_REFRESHTOKEN);
    }
}
