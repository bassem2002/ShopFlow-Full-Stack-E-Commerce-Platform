package com.shopflow.shopflow.service.impl;

import com.shopflow.shopflow.dto.request.CouponRequest;
import com.shopflow.shopflow.dto.response.CouponResponse;
import com.shopflow.shopflow.entity.Coupon;
import com.shopflow.shopflow.exception.BusinessException;
import com.shopflow.shopflow.repository.CouponRepository;
import com.shopflow.shopflow.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    @Override
    @Transactional
    public CouponResponse createCoupon(CouponRequest request) {
        if (couponRepository.findByCode(request.getCode()).isPresent()) {
            throw new BusinessException("Coupon code already exists");
        }

        Coupon coupon = Coupon.builder()
                .code(request.getCode().toUpperCase())
                .type(request.getType())
                .value(request.getValue())
                .expirationDate(request.getExpirationDate())
                .maxUsages(request.getMaxUsages())
                .currentUsages(0)
                .active(request.getActive())
                .build();

        return mapToResponse(couponRepository.save(coupon));
    }

    @Override
    @Transactional
    public CouponResponse updateCoupon(Long id, CouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Coupon not found"));

        if (!coupon.getCode().equalsIgnoreCase(request.getCode()) &&
                couponRepository.findByCode(request.getCode()).isPresent()) {
            throw new BusinessException("Coupon code already owned by another coupon");
        }

        coupon.setCode(request.getCode().toUpperCase());
        coupon.setType(request.getType());
        coupon.setValue(request.getValue());
        coupon.setExpirationDate(request.getExpirationDate());
        coupon.setMaxUsages(request.getMaxUsages());
        coupon.setActive(request.getActive());

        return mapToResponse(couponRepository.save(coupon));
    }

    @Override
    @Transactional
    public void deleteCoupon(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new BusinessException("Coupon not found");
        }
        couponRepository.deleteById(id);
    }

    @Override
    public CouponResponse getCouponById(Long id) {
        return couponRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new BusinessException("Coupon not found"));
    }

    @Override
    public CouponResponse getCouponByCode(String code) {
        return couponRepository.findByCode(code.toUpperCase())
                .map(this::mapToResponse)
                .orElseThrow(() -> new BusinessException("Coupon not found"));
    }

    @Override
    public List<CouponResponse> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean isValid(String code) {
        return couponRepository.findByCode(code.toUpperCase())
                .map(coupon -> coupon.getActive() 
                        && coupon.getExpirationDate().isAfter(LocalDateTime.now()) 
                        && coupon.getCurrentUsages() < coupon.getMaxUsages())
                .orElse(false);
    }

    private CouponResponse mapToResponse(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .type(coupon.getType())
                .value(coupon.getValue())
                .expirationDate(coupon.getExpirationDate())
                .maxUsages(coupon.getMaxUsages())
                .currentUsages(coupon.getCurrentUsages())
                .active(coupon.getActive())
                .build();
    }
}
