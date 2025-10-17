package net.awords.agriecombackend.entity;

/**
 * 订单状态枚举，既适用于买家订单聚合，也适用于商户子订单。
 */
public enum OrderStatus {
    CREATED,
    PAID,
    PROCESSING,
    SHIPPED,
    COMPLETED,
    CANCELLED
}
