package controller;

import model.Fan;
import repository.FanRepository;

import java.util.Optional;

/**
 * AuthController – Xử lý đăng ký và đăng nhập.
 *
 * Tách riêng khỏi FanController (phần của thành viên khác).
 * AuthController chỉ phụ trách:
 * - register(): tạo tài khoản mới → lưu vào fans.csv
 * - login(): xác thực email + password → trả về Fan nếu đúng
 *
 * PASSWORD HASH:
 * Dùng simpleHash (Java hashCode) cho demo.
 * Lưu ý: 500 fan pre-seeded trong fans.csv dùng BCrypt format khác,
 * nên chỉ login được bằng tài khoản TỰ ĐĂNG KÝ qua app này.
 */
public class AuthController {

    private final FanRepository fanRepo;

    public AuthController() {
        this.fanRepo = new FanRepository();
    }

    // Cho test inject
    public AuthController(FanRepository fanRepo) {
        this.fanRepo = fanRepo;
    }

    // ================================================================
    // ĐĂNG KÝ
    // ================================================================

    /**
     * Ket qua tra ve cua register/login.
     */
    public static class AuthResult {
        private final boolean success;
        private final Fan     fan;
        private final String  message;

        private AuthResult(boolean success, Fan fan, String message) {
            this.success = success;
            this.fan     = fan;
            this.message = message;
        }

        public static AuthResult ok(Fan fan)      { return new AuthResult(true,  fan,  "Thanh cong"); }
        public static AuthResult fail(String msg) { return new AuthResult(false, null, msg); }

        public boolean isSuccess() { return success; }
        public Fan     getFan()    { return fan; }
        public String  getMessage(){ return message; }
    }

    // ================================================================
    // ROLE – Phan biet Fan va Admin
    // ================================================================

    public enum Role { FAN, ADMIN }

    /**
     * AuthSession – Luu thong tin phien lam viec sau khi dang nhap.
     *   role = FAN   → fan thuong, chi xem ghe / dat ve / huy ve
     *   role = ADMIN → quan tri vien, xem bao cao toan he thong
     */
    public static class AuthSession {
        private final Role   role;
        private final Fan    fan;    // null neu la Admin
        private final String displayName;

        public AuthSession(Role role, Fan fan, String displayName) {
            this.role        = role;
            this.fan         = fan;
            this.displayName = displayName;
        }

        public Role   getRole()        { return role; }
        public Fan    getFan()         { return fan; }
        public String getDisplayName() { return displayName; }
        public boolean isAdmin()       { return role == Role.ADMIN; }
        public String  getId()         { return fan != null ? fan.getId() : "ADMIN"; }
    }

    /**
     * Đăng ký tài khoản mới.
     *
     * Quy tắc:
     * - Name, email, password không được để trống
     * - Email chưa được sử dụng
     * - FanID tự động sinh (FAN + timestamp)
     *
     * @return AuthResult.ok(fan) nếu thành công, AuthResult.fail(msg) nếu không
     */
    public AuthResult register(String name, String email, String phone, String password) {
        // Kiểm tra input
        if (isBlank(name))
            return AuthResult.fail("Ten khong duoc de trong!");
        if (isBlank(email))
            return AuthResult.fail("Email khong duoc de trong!");
        if (isBlank(password))
            return AuthResult.fail("Mat khau khong duoc de trong!");
        if (password.length() < 4)
            return AuthResult.fail("Mat khau phai tu 4 ky tu tro len!");

        // Kiểm tra email trùng
        if (fanRepo.findByEmail(email).isPresent()) {
            return AuthResult.fail("Email '" + email + "' da duoc dung. Vui long dung email khac!");
        }

        // Tự sinh fanId dựa trên timestamp (đảm bảo unique)
        String fanId = "FAN" + System.currentTimeMillis() % 1_000_000;
        String hash = simpleHash(password);

        Fan newFan = new Fan(fanId, name.trim(), email.trim().toLowerCase(),
                phone == null ? "" : phone.trim(), hash);

        if (!fanRepo.save(newFan)) {
            return AuthResult.fail("Loi he thong: Khong the luu tai khoan!");
        }
        return AuthResult.ok(newFan);
    }

    // ================================================================
    // ĐĂNG NHẬP
    // ================================================================

    /**
     * Đăng nhập bằng email và password.
     *
     * @return AuthResult.ok(fan) nếu đúng, AuthResult.fail(msg) nếu sai
     */
    public AuthResult login(String email, String password) {
        if (isBlank(email) || isBlank(password)) {
            return AuthResult.fail("Email va mat khau khong duoc de trong!");
        }

        Optional<Fan> found = fanRepo.findByEmail(email.trim().toLowerCase());
        if (found.isEmpty()) {
            return AuthResult.fail("Email khong ton tai trong he thong!");
        }

        Fan fan = found.get();
        if (!fan.getPasswordHash().equals(simpleHash(password))) {
            return AuthResult.fail("Mat khau khong chinh xac!");
        }

        return AuthResult.ok(fan);
    }

    // ================================================================
    // DANG NHAP ADMIN
    // ================================================================

    /**
     * Dang nhap admin bang password co dinh.
     * Password mac dinh: "admin123" (chi dung cho demo/lab).
     *
     * @return AuthSession voi role=ADMIN neu dung, null neu sai
     */
    public AuthSession loginAdmin(String password) {
        final String ADMIN_PASSWORD = "admin123";
        if (ADMIN_PASSWORD.equals(password)) {
            // Admin khong co Fan object, dung displayName = "Admin"
            return new AuthSession(Role.ADMIN, null, "Admin");
        }
        return null; // Sai mat khau
    }

    // ================================================================
    // NỘI BỘ
    // ================================================================

    /**
     * Hash đơn giản dùng cho demo (không dùng BCrypt như production).
     * Định dạng: "APP_" + hashCode để phân biệt với hash BCrypt ($2a$...) của data
     * seed.
     */
    private String simpleHash(String password) {
        return "APP_" + Math.abs(password.hashCode());
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
