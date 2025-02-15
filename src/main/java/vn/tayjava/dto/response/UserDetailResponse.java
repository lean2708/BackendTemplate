package vn.tayjava.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class UserDetailResponse {
    private long id;
    private String firstName;

    private String lastName;

    private String email;

    private String phone;
}
