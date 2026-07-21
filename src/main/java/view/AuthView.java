package view;

import controller.AuthController;
import controller.AuthController.AuthResult;
import controller.AuthController.AuthSession;
import controller.AuthController.Role;
import model.Fan;

import java.util.Scanner;

/**
 * AuthView – Man hinh dang ky / dang nhap tren console.
 *
 * LUONG:
 *   [1] Dang nhap Fan   -> email + password -> AuthSession(FAN)
 *   [2] Dang nhap Admin -> nhap password admin -> AuthSession(ADMIN)
 *   [3] Dang ky Fan     -> nhap thong tin -> tao tai khoan -> AuthSession(FAN)
 *   [0] Thoat           -> tra ve null
 *
 * TRA VE:
 *   AuthSession chua role (FAN/ADMIN) va Fan object (null neu Admin)
 *   MainView dung role nay de hien thi menu tuong ung.
 *
 * GHI CHU:
 *   Admin password mac dinh: "admin123"
 *   500 fan co san trong fans.csv dung BCrypt hash khac format,
 *   nen chi login duoc bang tai khoan TU DANG KY qua app nay.
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
    // RUN – Tra ve AuthSession, null neu thoat
    // ================================================================

    /**
     * Hien thi man hinh auth va lap cho den khi login thanh cong hoac thoat.
     *
     * @return AuthSession neu dang nhap thanh cong, null neu thoat
     */
    public AuthSession run() {
        printBanner();
        while (true) {
            printAuthMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> {
                    AuthSession session = handleFanLogin();
                    if (session != null) return session;
                }
                case "2" -> {
                    AuthSession session = handleAdminLogin();
                    if (session != null) return session;
                }
                case "3" -> {
                    AuthSession session = handleRegister();
                    if (session != null) return session;
                }
                case "0" -> { return null; }
                default -> System.out.println("  [!] Lua chon khong hop le!\n");
            }
        }
    }

    // ================================================================
    // DANG NHAP FAN
    // ================================================================

    private AuthSession handleFanLogin() {
        System.out.println();
        System.out.println("  === DANG NHAP (Fan) ===");

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
            // Tao AuthSession voi role = FAN
            return new AuthSession(Role.FAN, fan, fan.getName());
        } else {
            System.out.println("  [FAIL] " + result.getMessage());
            System.out.println();
            return null;
        }
    }

    // ================================================================
    // DANG NHAP ADMIN
    // ================================================================

    private AuthSession handleAdminLogin() {
        System.out.println();
        System.out.println("  === DANG NHAP QUAN TRI VIEN (Admin) ===");
        System.out.print("  Mat khau admin: ");
        String password = scanner.nextLine().trim();

        AuthSession session = auth.loginAdmin(password);

        if (session != null) {
            System.out.println();
            System.out.println("  [OK] Chao mung, Admin!");
            System.out.println();
            return session;
        } else {
            System.out.println("  [FAIL] Sai mat khau admin!");
            System.out.println();
            return null;
        }
    }

    // ================================================================
    // DANG KY FAN MOI
    // ================================================================

    private AuthSession handleRegister() {
        System.out.println();
        System.out.println("  === DANG KY TAI KHOAN FAN ===");

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
            System.out.println("  Ma tai khoan: " + fan.getId());
            System.out.println("  Dang nhap tu dong...");
            System.out.println();
            return new AuthSession(Role.FAN, fan, fan.getName());
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
        System.out.println("  Luu y: Dang ky tai khoan moi de su dung.");
        System.out.println();
    }

    private void printAuthMenu() {
        System.out.println("  +---------------------------------------+");
        System.out.println("  |  [1] Dang nhap (Fan)                  |");
        System.out.println("  |  [2] Dang nhap (Admin / Quan li)      |");
        System.out.println("  |  [3] Dang ky tai khoan Fan moi        |");
        System.out.println("  |  [0] Thoat                            |");
        System.out.println("  +---------------------------------------+");
        System.out.print("  Chon: ");
    }
}
