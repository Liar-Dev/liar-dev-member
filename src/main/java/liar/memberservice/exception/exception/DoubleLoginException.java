package liar.memberservice.exception.exception;

import liar.memberservice.exception.type.ExceptionCode;
import liar.memberservice.exception.type.ExceptionMessage;

public class DoubleLoginException extends CommonException {

    public DoubleLoginException() {
        super(ExceptionCode.UNAUTHORIZED, ExceptionMessage.DOUBLE_LOGIN);
    }
}
