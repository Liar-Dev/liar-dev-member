package liar.memberservice.exception.exception;

import liar.memberservice.exception.type.ExceptionCode;
import liar.memberservice.exception.type.ExceptionMessage;

public class RedisLockException extends CommonException {

    public RedisLockException() {
        super(ExceptionCode.CONFLICT, ExceptionMessage.REDIS_ROCK_EXCEPTION);
    }
}
