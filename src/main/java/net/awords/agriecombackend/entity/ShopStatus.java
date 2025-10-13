package net.awords.agriecombackend.entity;

/**
 * 店铺的生命周期状态；使用字符串枚举以便数据库存储与可读性兼得。
 */
public enum ShopStatus {
    PENDING_REVIEW,
    ACTIVE,
    SUSPENDED,
    REJECTED
}
