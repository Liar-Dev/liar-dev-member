package liar.memberservice.member.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class SendSuccess {
    public String code;
    public String message;

    public static SendSuccess of() {
        return new SendSuccess(SuccessCode.OK, SuccessMessage.OK);
    }
}
