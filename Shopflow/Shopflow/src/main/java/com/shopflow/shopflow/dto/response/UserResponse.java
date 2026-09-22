package com.shopflow.shopflow.dto.response;

import com.shopflow.shopflow.enums.Role;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private Boolean active;
    private LocalDateTime createdAt;
}
