package com.trip_gg.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;
import com.trip_gg.domain.User;

@Getter
@Setter
public class UserRequestDto {
    @NotBlank(message = "아이디는 필수입니다.")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "아이디는 영어만 가능합니다.")
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-]).{8,}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함한 8자 이상이어야 합니다"
    )
    private String password;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "유효한 이메일 주소를 입력하세요")
    private String email;

    private String nickname;

    // DTO -> Entity 변환
    public User toUser(String encodedPassword){
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(this.username);
        user.setEmail(this.email);
        user.setNickname(this.nickname);
        user.setPassword(encodedPassword);
        user.setRole("USER");
        user.setProvider("credentials");
        user.setCreated_at(LocalDateTime.now());
        return user;
    }
}
