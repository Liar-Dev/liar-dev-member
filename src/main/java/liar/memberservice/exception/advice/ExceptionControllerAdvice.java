package liar.memberservice.exception.advice;

import com.fasterxml.jackson.core.JsonProcessingException;
import liar.memberservice.exception.exception.*;
import liar.memberservice.exception.type.ExceptionCode;
import liar.memberservice.exception.type.ExceptionMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class ExceptionControllerAdvice {

    /**
     *  바인딩 에러
     */
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler
    public ResponseEntity<ErrorDto> MethodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {

        List<String> errorMessages = new ArrayList<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errorMessages.add(fieldError.getDefaultMessage());
        }

        return new ResponseEntity<>(new ErrorDto("400", String.join(".", errorMessages)), BAD_REQUEST);
    }

    /**
     *  권한 없는 사용자 접근
     */
    @ResponseStatus(UNAUTHORIZED)
    @ExceptionHandler
    public ResponseEntity<ErrorDto> handlerNotExistsRefreshTokenHandler(NotExistRefreshTokenException e) {
        return new ResponseEntity<>(new ErrorDto(e.getErrorCode(), e.getMessage()), UNAUTHORIZED);
    }

    /**
     *  권한 없는 사용자 접근
     */
    @ResponseStatus(UNAUTHORIZED)
    @ExceptionHandler
    public ResponseEntity<ErrorDto> handlerBadJwtRequestExceptionHandler(BadJwtRequestException e) {
        return new ResponseEntity<>(new ErrorDto(e.getErrorCode(), e.getMessage()), UNAUTHORIZED);
    }


    /**
     *  등록되지 않은 유저 접근
     */
    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler
    public ResponseEntity<ErrorDto> handlerUserNotFoundHandler(NotFoundUserException e) {
        return new ResponseEntity<>(new ErrorDto(e.getErrorCode(), e.getMessage()), NOT_FOUND);
    }

    /**
     *  회원가입시 중복 이메일
     */
    @ResponseStatus(CONFLICT)
    @ExceptionHandler
    public ResponseEntity<ErrorDto> handlerUserEmailConflict(UserRegisterConflictException e) {
        return new ResponseEntity<>(new ErrorDto(e.getErrorCode(), e.getMessage()), CONFLICT);
    }

    /**
     * 유효하지 않은 json 요청시 발생
     */
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler
    public ResponseEntity<ErrorDto> handlerJsonProcessingException(JsonProcessingException e) {
        return new ResponseEntity<>(new ErrorDto(ExceptionCode.BAD_REQUEST, ExceptionMessage.BAD_REQUEST), BAD_REQUEST);
    }

    /**
     * 유효하지 않은 BadRequest 요청시 발생
     */
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler
    public ResponseEntity<ErrorDto> handlerBadRequestException(BadRequestException e) {
        return new ResponseEntity<>(new ErrorDto(e.getErrorCode(), e.getMessage()), BAD_REQUEST);
    }

    /**
     * 너무 많은 요청시 발생
     */
    @ResponseStatus(TOO_MANY_REQUESTS)
    @ExceptionHandler
    public ResponseEntity<ErrorDto> handlerTooManyRequestException(TooManyRequestException e) {
        return new ResponseEntity<>(new ErrorDto(e.getErrorCode(), e.getMessage()), TOO_MANY_REQUESTS);
    }

    /**
     * 중복 로그인 결과, 이전 로그인 정보 권한 없음
     */
    @ResponseStatus(UNAUTHORIZED)
    @ExceptionHandler
    public ResponseEntity<ErrorDto> handlerDoubleLoginException(DoubleLoginException e) {
        return new ResponseEntity<>(new ErrorDto(e.getErrorCode(), e.getMessage()), UNAUTHORIZED);
    }

    /**
     *  공통 4XX 에러
     */
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDto> illegalHandler(IllegalArgumentException e) {
        return new ResponseEntity<>(new ErrorDto(ExceptionCode.BAD_REQUEST, ExceptionMessage.BAD_REQUEST), BAD_REQUEST);
    }


    /**
     *  공통 5XX 에러
     */
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler
    public ResponseEntity<ErrorDto> internalServerErrorHandler(Exception e) {
        System.out.println("e.getStackTrace() = " + e.getStackTrace());
        System.out.println("e.getMessage() = " + e.getMessage());
        System.out.println("e.getCause() = " + e.getCause());
        System.out.println("e.getSuppressed() = " + e.getSuppressed());
        System.out.println("e = " + e);
        return new ResponseEntity<>(new ErrorDto(ExceptionCode.INTERNAL_SERVER_ERROR, ExceptionMessage.INTERNAL_SERVER_ERROR), INTERNAL_SERVER_ERROR);
    }



}
