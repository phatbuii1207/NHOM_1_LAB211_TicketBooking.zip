package model;

/**
 * Kiểu khoá ghế trong hệ thống.
 * OPTIMISTIC  – Optimistic Locking: dùng trường version để phát hiện xung đột.
 * PESSIMISTIC – Pessimistic Locking: khoá ghế ngay khi người dùng chọn.
 */
public enum LockType {
    OPTIMISTIC,
    PESSIMISTIC
}
