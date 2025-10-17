package net.awords.agriecombackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import net.awords.agriecombackend.dto.ApiResponseDTO;
import net.awords.agriecombackend.dto.user.UserDtos;
import net.awords.agriecombackend.service.UserProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * 当前用户资料、地址相关接口。
 */
@RestController
@RequestMapping("/api/v1/me")
@Tag(name = "User", description = "用户资料与地址管理")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @Operation(summary = "获取我的资料")
    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole(T(net.awords.agriecombackend.security.RoleConstants).USER, T(net.awords.agriecombackend.security.RoleConstants).MERCHANT, T(net.awords.agriecombackend.security.RoleConstants).ADMIN)")
    public ApiResponseDTO<UserDtos.ProfileResponse> profile(Authentication authentication) {
        String username = requireUsername(authentication);
        return ApiResponseDTO.success(userProfileService.getProfile(username));
    }

    @Operation(summary = "更新我的资料")
    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole(T(net.awords.agriecombackend.security.RoleConstants).USER, T(net.awords.agriecombackend.security.RoleConstants).MERCHANT, T(net.awords.agriecombackend.security.RoleConstants).ADMIN)")
    public ApiResponseDTO<UserDtos.ProfileResponse> updateProfile(Authentication authentication,
                                                                  @Valid @RequestBody UserDtos.UpdateProfileRequest request) {
        String username = requireUsername(authentication);
        return ApiResponseDTO.success(userProfileService.updateProfile(username, request));
    }

    @Operation(summary = "我的收货地址列表")
    @GetMapping("/addresses")
    @PreAuthorize("hasRole(T(net.awords.agriecombackend.security.RoleConstants).USER)")
    public ApiResponseDTO<List<UserDtos.AddressResponse>> addresses(Authentication authentication) {
        String username = requireUsername(authentication);
        return ApiResponseDTO.success(userProfileService.listAddresses(username));
    }

    @Operation(summary = "新增收货地址")
    @PostMapping("/addresses")
    @PreAuthorize("hasRole(T(net.awords.agriecombackend.security.RoleConstants).USER)")
    public ApiResponseDTO<UserDtos.AddressResponse> createAddress(Authentication authentication,
                                                                  @Valid @RequestBody UserDtos.CreateAddressRequest request) {
        String username = requireUsername(authentication);
        return ApiResponseDTO.success(userProfileService.createAddress(username, request));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        return authentication.getName();
    }
}
