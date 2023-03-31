package liar.memberservice.exception.exception;


import liar.memberservice.exception.type.ExceptionCode;
import liar.memberservice.exception.type.ExceptionMessage;

public class BadRequestException extends CommonException {

    public BadRequestException() {super(ExceptionCode.BAD_REQUEST, ExceptionMessage.BAD_REQUEST);}
}
