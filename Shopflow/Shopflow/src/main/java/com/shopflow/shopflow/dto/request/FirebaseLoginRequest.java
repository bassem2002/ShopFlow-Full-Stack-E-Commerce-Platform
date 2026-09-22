package com.shopflow.shopflow.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FirebaseLoginRequest {
    private String idToken;
    private String role;
}
