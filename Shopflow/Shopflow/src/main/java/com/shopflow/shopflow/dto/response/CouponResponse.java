package com.shopflow.shopflow.dto.response;

import com.shopflow.shopflow.enums.CouponType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CouponResponse {

    private Long id;
    private String code;
    private CouponType type;
    private Double value;
    private LocalDateTime expirationDate;
    private Integer maxUsages;
    private Integer currentUsages;
    private Boolean active;
}
