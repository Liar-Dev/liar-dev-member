package liar.memberservice.exception.exception;

import liar.memberservice.exception.type.ExceptionCode;
import liar.memberservice.exception.type.ExceptionMessage;

public class TooManyRequestException extends CommonException {

    public TooManyRequestException() {super(ExceptionCode.TOO_MANY_REQUEST, ExceptionMessage.TOO_MANY_REQUEST);}
}
