package liar.memberservice.member.controller.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import liar.memberservice.exception.exception.BadRequestException;
import liar.memberservice.exception.exception.TooManyRequestException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.PatternMatchUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static java.time.Duration.between;

@Slf4j
public class LoginSessionFilter extends OncePerRequestFilter {

    private final ConcurrentHashMap<String, LoginSessionCheck> sessionFilter = new ConcurrentHashMap<>();

//    @Qualifier("defaultObjectMapper")
//    private final ObjectMapper defaultObjectMapper;

//    public LoginSessionFilter(@Qualifier("defaultObjectMapper") ObjectMapper defaultObjectMapper) {
//        this.defaultObjectMapper = defaultObjectMapper;
//    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String xForHeader = request.getHeader("X-Forwarded-For");
        if (xForHeader == null || xForHeader.isEmpty()) throw new BadRequestException();

        String remoteAddr = xForHeader.split(",")[0].trim();

        clearCheckStatus();
        LocalDateTime now = LocalDateTime.now();
        LoginSessionCheck addrCheck = sessionFilter.get(remoteAddr);

        if (addrCheck != null) {
            setCheckStatus(remoteAddr, addrCheck, now);
        } else {
            sessionFilter.put(remoteAddr, new LoginSessionCheck(now));
        }

        filterChain.doFilter(request, response);
    }

    private void setCheckStatus(String key, LoginSessionCheck addrCheck, LocalDateTime now) {
        if (addrCheck.isBlock()) {
            if (between(addrCheck.getLast(), now).toSeconds() > 20) { // 차단 해제 조건
                addrCheck.setInit();
            } else {
                throw new TooManyRequestException(); // 차단 유지
            }
        }

        if (addrCheck.getFirst() == null) { // first가 null 인경우
            addrCheck.setFirst(now);
            addrCheck.setLast(now);
        }

        else {
            log.info("addCheck.getCount = {}", addrCheck.getCount());

            if (addrCheck.getCount() > 3) {
                log.info("addrCheck.getCount() = {}", addrCheck.getCount());
                addrCheck.setBlock(true); // 두 번째 이상 요청
                throw new TooManyRequestException();
            }

            if (between(addrCheck.getLast(), now).toSeconds() > 10) { // 2번 ==> 마지막 요청 후 10초가 지났다면 메모리 낭비를 제거하기 위해 체크 제거

                log.info("addrCheck.remove()");
                sessionFilter.remove(key);
            } else {
                log.info("addrCheck.increment()");
                addrCheck.incrementAndGetCount();
                addrCheck.setLast(now);
            }
        }
    }

    private void clearCheckStatus() {
        if (sessionFilter.size() > 300) {
            sessionFilter.entrySet().removeIf(entry -> !entry.getValue().isBlock());
        }
    }

    @Setter
    @Getter
    @NoArgsConstructor
    static class LoginSessionCheck {
        private LocalDateTime first;
        private LocalDateTime last;
        private AtomicInteger count = new AtomicInteger(0);
        private volatile boolean block;

        public LoginSessionCheck(LocalDateTime now) {
            this.first = now;
            this.last = now;
        }
        public int incrementAndGetCount() {
            return count.incrementAndGet();
        }
        public int getCount() {
            return count.get();
        }

        public void setInit() {
            this.first = null;
            this.last = null;
            this.count = new AtomicInteger(0);
            this.block = false;
        }
    }
}
