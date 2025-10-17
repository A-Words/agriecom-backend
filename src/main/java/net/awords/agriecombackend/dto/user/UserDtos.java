package net.awords.agriecombackend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 用户资料与地址相关 DTO。
 */
public class UserDtos {

    public static class ProfileResponse {
        public Long id;
        public String username;
        public String nickname;
        public String avatarUrl;
        public String email;
        public String phone;
        public String bio;
        public String[] roles;
        public OffsetDateTime createdAt;
        public OffsetDateTime updatedAt;
    }

    public static class UpdateProfileRequest {
        @Size(max = 150, message = "昵称长度不能超过 150 字")
        public String nickname;

        @Size(max = 512, message = "头像链接过长")
        public String avatarUrl;

        @Email(message = "邮箱格式不正确")
        @Size(max = 255, message = "邮箱长度超限")
        public String email;

        @Size(max = 50, message = "手机号长度超限")
        public String phone;

        @Size(max = 500, message = "简介长度超限")
        public String bio;
    }

    public static class AddressResponse {
        public Long id;
        public String recipientName;
        public String phone;
        public String province;
        public String city;
        public String district;
        public String street;
        public String postalCode;
        public boolean isDefault;
        public OffsetDateTime createdAt;
        public OffsetDateTime updatedAt;
    }

    public static class CreateAddressRequest {
        @NotBlank(message = "收件人不能为空")
        @Size(max = 150, message = "收件人长度超限")
        public String recipientName;

        @NotBlank(message = "手机号不能为空")
        @Size(max = 50, message = "手机号长度超限")
        public String phone;

        @NotBlank(message = "省份不能为空")
        @Size(max = 150, message = "省份长度超限")
        public String province;

        @NotBlank(message = "城市不能为空")
        @Size(max = 150, message = "城市长度超限")
        public String city;

        @NotBlank(message = "区县不能为空")
        @Size(max = 150, message = "区县长度超限")
        public String district;

        @NotBlank(message = "详细地址不能为空")
        @Size(max = 255, message = "详细地址长度超限")
        public String street;

        @Size(max = 30, message = "邮编长度超限")
        public String postalCode;

        public Boolean isDefault;
    }

    public static class AddressListResponse {
        public List<AddressResponse> addresses;
    }
}
