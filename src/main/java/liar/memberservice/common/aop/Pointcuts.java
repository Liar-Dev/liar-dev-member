package liar.memberservice.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Pointcut;

@Slf4j
public class Pointcuts {

    @Pointcut("execution(* liar.memberservice.member.service.token.TokenPolicy.deleteAuthTokens(..))")
    public void deleteAuthToken() {}

    @Pointcut("execution(* liar.memberservice.member.service.token.TokenPolicy.reissueAuthToken(..))")
    public void reissue() {}

    @Pointcut("@annotation(liar.memberservice.common.aop.anno.RedisTransactionalAuthTokenDto)")
    public void transactionMethod() {};

}
