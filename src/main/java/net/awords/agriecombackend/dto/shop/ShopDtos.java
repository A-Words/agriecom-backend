package net.awords.agriecombackend.dto.shop;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import net.awords.agriecombackend.entity.ShopStatus;

import java.time.OffsetDateTime;

public class ShopDtos {
    public static class CreateRequest {
        @NotBlank(message = "店铺名称不能为空")
        @Size(max = 255, message = "店铺名称长度不能超过255字符")
        public String name;

        @Size(max = 2000, message = "店铺介绍长度不能超过2000字符")
        public String description;

        @Size(max = 512, message = "Logo 链接长度不能超过512字符")
        public String logoUrl;
    }

    public static class UpdateRequest {
        @NotBlank(message = "店铺名称不能为空")
        @Size(max = 255, message = "店铺名称长度不能超过255字符")
        public String name;

        @Size(max = 2000, message = "店铺介绍长度不能超过2000字符")
        public String description;

        @Size(max = 512, message = "Logo 链接长度不能超过512字符")
        public String logoUrl;
    }

    public static class DetailResponse {
        public Long id;
        public String name;
        public String description;
        public String logoUrl;
        public ShopStatus status;
        public OffsetDateTime createdAt;
        public OffsetDateTime updatedAt;
        public long productCount;
        public long pendingOrderCount;
        public long completedOrderCount;
    }
}
