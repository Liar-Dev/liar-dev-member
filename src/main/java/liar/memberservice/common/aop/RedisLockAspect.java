package liar.memberservice.common.aop;

import liar.memberservice.member.domain.member.Authority;
import liar.memberservice.member.domain.member.Member;
import liar.memberservice.member.service.dto.AuthTokenDto;
import liar.memberservice.member.service.dto.FormRegisterUserDto;
import liar.memberservice.exception.exception.RedisLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class RedisLockAspect {

    private final RedissonClient redissonClient;

    @Around("execution(* liar.memberservice.member.repository..*.delete(..))")
    public Object executeWithRock(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> returnType = method.getReturnType();

        String lockKey = getLockKey(joinPoint.getArgs());
        RLock lock = redissonClient.getLock(lockKey);

        try{
            boolean isLocked = lock.tryLock(10, 12, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new RedisLockException();
            }
            Object result = joinPoint.proceed();
            return returnType.cast(result);

        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Around("execution(* liar.memberservice.member.service.member.MemberPolicy.register(..)) && args(dto)")
    public boolean registerForm(ProceedingJoinPoint joinPoint, FormRegisterUserDto dto) throws Throwable {
        String lockKey = "Register:" + dto.getEmail();
        return (boolean) executeWithRedisLock(joinPoint, lockKey);
    }

    public Object executeWithRedisLock(ProceedingJoinPoint joinPoint, String lockKey) throws Throwable {
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(60, TimeUnit.SECONDS);
            if (!isLocked) throw new RedisLockException();
            return joinPoint.proceed();

        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Around("liar.memberservice.common.aop.Pointcuts.transactionMethod() && args(member, authorities, remoteAddr)")
    public AuthTokenDto saveToken(ProceedingJoinPoint joinPoint, Member member,
                                  List<Authority> authorities, String remoteAddr)
            throws Throwable {
        String lockKey = "SaveUserToken:" + member.getUserId();
        return saveTokenPointTransactionRedissonRLock(joinPoint, lockKey);
    }

    @Around("liar.memberservice.common.aop.Pointcuts.reissue() && args(refreshToken)")
    public AuthTokenDto reissue(ProceedingJoinPoint joinPoint, String refreshToken)
            throws Throwable {
        String lockKey = "Reissue:" + refreshToken;
        return saveTokenPointTransactionRedissonRLock(joinPoint, lockKey);
    }

    @Around("liar.memberservice.common.aop.Pointcuts.deleteAuthToken() && args(accessToken, refreshToken, userId)")
    public void deleteAuthToken(ProceedingJoinPoint joinPoint, String accessToken, String refreshToken, String userId)
            throws Throwable {
        String lockKey = "DeleteAuthToken:" + userId;
        voidPointTransactionRedissonRLock(joinPoint, lockKey);
    }


    private AuthTokenDto saveTokenPointTransactionRedissonRLock(ProceedingJoinPoint joinPoint,
                                                                String lockKey) throws Throwable {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean isLocked = lock.tryLock(60, TimeUnit.SECONDS);
            if (!isLocked) throw new RedisLockException();
            Object proceed = joinPoint.proceed();
            if (proceed == null) {
                return null;
            }
            return (AuthTokenDto) proceed;
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }

    private void voidPointTransactionRedissonRLock(ProceedingJoinPoint joinPoint, String lockKey) throws Throwable {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean isLocked = lock.tryLock(60, TimeUnit.SECONDS);
            if (!isLocked) throw new RedisLockException();
            joinPoint.proceed();
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }

    private <T> String getLockKey(T arg) {

        if (arg instanceof String) {
            return (String) arg;
        }

        else if (arg instanceof Member){
            return ((Member) arg).getId();
        }

        else if (arg instanceof Authority) {
            return ((Authority) arg).getId();
        }

        else if (arg instanceof Object[]) {
            StringBuilder sb = new StringBuilder();
            for (Object obj : (Object[]) arg) {
                sb.append(getLockKey(obj));
            }
            return sb.toString();
        }
        throw new RedisLockException();
    }

}
