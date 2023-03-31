package liar.memberservice.common.principal.authority;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;

import java.util.Collection;
import java.util.HashSet;


@Slf4j
public class CustomAuthorityMapper implements GrantedAuthoritiesMapper {

    private final String PREFIX = "ROLE_";

    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
        HashSet<GrantedAuthority> mapped = new HashSet<>(authorities.size());

        for (GrantedAuthority authority : authorities) {
            log.info("authority = {}", authority);
            mapped.add(mapAuthority(authority.getAuthority()));
        }

        return mapped;
    }

    private GrantedAuthority mapAuthority(String name) {

        if (name.lastIndexOf(".") > 0) {
            int index = name.lastIndexOf(".");
            name = "SCOPE_" + name.substring(index + 1);
        }
        if (!name.startsWith(PREFIX)) {
            name = PREFIX + name;
        }
        return new SimpleGrantedAuthority(name);

    }

}
