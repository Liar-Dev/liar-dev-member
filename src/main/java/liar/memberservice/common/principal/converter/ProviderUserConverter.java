package liar.memberservice.common.principal.converter;

public interface ProviderUserConverter<T, R> {

    R converter(T t);
}
