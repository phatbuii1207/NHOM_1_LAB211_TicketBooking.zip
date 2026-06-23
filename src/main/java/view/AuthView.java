package view;

import controller.AuthController;
import controller.AuthController.AuthResult;
import model.Fan;

import java.util.Scanner;

/**
 * AuthView – Màn hình đăng ký / đăng nhập trên console.
 *
 * LUỒNG:
 *   Khi app khởi động → AuthView.run() → user chọn:
 *     [1] Dang nhap  → nhập email + password → trả về Fan
 *     [2] Dang ky    → nhập name, email, phone, password → tạo tài khoản → tự động đăng nhập
 *     [0] Thoat      → thoát app
 *
 *   Sau khi đăng nhập thành công → MainView.run(fanId) chạy tiếp
 *
 * GHI CHÚ:
 *   500 fan có sẵn trong fans.csv dùng BCrypt hash khác format →
 *   Không đăng nhập được bằng tài khoản đó. Hãy TỰ ĐĂNG KÝ tài khoản mới.
 */
public class AuthView {

    private final AuthController auth;
    private final Scanner        scanner;

    public AuthView() {
        this.auth    = new AuthController();
        this.scanner = new Scanner(System.in);
    }

    public AuthView(AuthController auth, Scanner scanner) {
        this.auth    = auth;
        this.scanner = scanner;
    }

    // ================================================================
    // RUN – Trả về Fan sau khi login thành công, null nếu thoát
    // ================================================================

    /**
     * Hiển thị màn hình auth và lặp cho đến khi đăng nhập thành công hoặc user thoát.
     *
     * @return Fan đã đăng nhập, hoặc null nếu user chọn thoát
     */
    public Fan run() {
        printBanner();
        while (true) {
            printAuthMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> {
                    Fan fan = handleLogin();
                    if (fan != null) return fan; // đăng nhập thành công → vào app
                }
                case "2" -> {
                    Fan fan = handleRegister();
                    if (fan != null) return fan; // đăng ký + tự login → vào app
                }
                case "0" -> { return null; } // thoát
                default  -> System.out.println("  [!] Lua chon khong hop le!\n");
            }
        }
    }

    // ================================================================
    // ĐĂNG NHẬP
    // ================================================================

    private Fan handleLogin() {
        System.out.println();
        System.out.println("  === DANG NHAP ===");

        System.out.print("  Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("  Mat khau: ");
        String password = scanner.nextLine().trim();

        AuthResult result = auth.login(email, password);

        if (result.isSuccess()) {
            Fan fan = result.getFan();
            System.out.println();
            System.out.println("  [OK] Chao mung, " + fan.getName() + "! (ID: " + fan.getId() + ")");
            System.out.println();
            return fan;
        } else {
            System.out.println("  [FAIL] " + result.getMessage());
            System.out.println();
            return null;
        }
    }

    // ================================================================
    // ĐĂNG KÝ
    // ================================================================

    private Fan handleRegister() {
        System.out.println();
        System.out.println("  === DANG KY TAI KHOAN ===");

        System.out.print("  Ho va ten: ");
        String name = scanner.nextLine().trim();

        System.out.print("  Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("  So dien thoai (co the bo trong): ");
        String phone = scanner.nextLine().trim();

        System.out.print("  Mat khau (toi thieu 4 ky tu): ");
        String password = scanner.nextLine().trim();

        System.out.print("  Nhap lai mat khau: ");
        String confirm = scanner.nextLine().trim();

        // Kiểm tra mật khẩu khớp nhau
        if (!password.equals(confirm)) {
            System.out.println("  [FAIL] Mat khau nhap lai khong khop!");
            System.out.println();
            return null;
        }

        AuthResult result = auth.register(name, email, phone, password);

        if (result.isSuccess()) {
            Fan fan = result.getFan();
            System.out.println();
            System.out.println("  [OK] Dang ky thanh cong!");
            System.out.println("  Ma tai khoan cua ban: " + fan.getId());
            System.out.println("  Dang nhap tu dong...");
            System.out.println();
            return fan;
        } else {
            System.out.println("  [FAIL] " + result.getMessage());
            System.out.println();
            return null;
        }
    }

    // ================================================================
    // UI HELPERS
    // ================================================================

    private void printBanner() {
        System.out.println();
        System.out.println("  " + "=".repeat(50));
        System.out.println("  ||   TICKET BOOKING SYSTEM - LAB211      ||");
        System.out.println("  ||   NHOM 1 - HE THONG DAT VE BONG DA    ||");
        System.out.println("  " + "=".repeat(50));
        System.out.println("  Luu y: Vui long DANG KY tai khoan moi de su dung.");
        System.out.println("  (500 fan co san trong file chi danh cho du lieu mau)");
        System.out.println();
    }

    private void printAuthMenu() {
        System.out.println("  +-------------------------------+");
        System.out.println("  |  [1] Dang nhap                |");
        System.out.println("  |  [2] Dang ky tai khoan moi    |");
        System.out.println("  |  [0] Thoat                    |");
        System.out.println("  +-------------------------------+");
        System.out.print("  Chon: ");
    }
}
