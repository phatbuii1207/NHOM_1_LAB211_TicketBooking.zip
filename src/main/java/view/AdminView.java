package view;

import model.Fan;
import repository.FanRepository;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * AdminView - Menu chính dành cho Quản trị viên (Admin).
 * Cung cấp các thao tác thống kê và quản lý mà Fan thường không có quyền truy cập.
 */
public class AdminView {

    private final FanRepository fanRepo;
    private final ReportView reportView;
    private final Scanner scanner;
    private String currentAdminId;

    public AdminView() {
        this.fanRepo = new FanRepository();
        this.reportView = new ReportView();
        this.scanner = new Scanner(System.in);
    }

    /**
     * Vòng lặp menu của Admin.
     */
    public void run(String adminId) {
        this.currentAdminId = adminId;
        printWelcome();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> reportView.run();
                case "2" -> handleViewAllFans();
                case "3" -> handleSearchFan();
                case "0" -> running = false;
                default -> System.out.println("  [!] Lua chon khong hop le. Vui long chon lai.");
            }
        }

        System.out.println();
        System.out.println("  [OK] Admin '" + currentAdminId + "' da dang xuat.");
        System.out.println("  " + "=".repeat(50));
    }

    private void handleViewAllFans() {
        System.out.println();
        System.out.println("  --- DANH SACH FANS REGISTED ---");
        List<Fan> fans = fanRepo.findByCondition(f -> !f.getId().toUpperCase().startsWith("AD"));
        if (fans.isEmpty()) {
            System.out.println("  Khong co fan nao trong he thong.");
            return;
        }

        System.out.printf("  %-10s | %-20s | %-30s | %-12s\n", "ID", "Name", "Email", "Phone");
        System.out.println("  " + "-".repeat(80));
        for (Fan f : fans) {
            System.out.printf("  %-10s | %-20s | %-30s | %-12s\n", 
                f.getId(), f.getName(), f.getEmail(), f.getPhone());
        }
        System.out.println("  Tong cong: " + fans.size() + " fans.");
    }

    private void handleSearchFan() {
        System.out.println();
        System.out.println("  --- TIM KIEM FAN ---");
        System.out.print("  Nhap Fan ID hoac Email can tim: ");
        String query = scanner.nextLine().trim();
        if (query.isBlank()) {
            System.out.println("  [!] Tu khoa tim kiem khong duoc de trong.");
            return;
        }

        // Try searching by ID
        Optional<Fan> fanById = fanRepo.findById(query);
        if (fanById.isPresent()) {
            printFanDetails(fanById.get());
            return;
        }

        // Try searching by Email
        Optional<Fan> fanByEmail = fanRepo.findByEmail(query);
        if (fanByEmail.isPresent()) {
            printFanDetails(fanByEmail.get());
            return;
        }

        System.out.println("  [!] Khong tim thay fan voi thong tin cung cap: " + query);
    }

    private void printFanDetails(Fan fan) {
        System.out.println("\n  === THONG TIN CHI TIET FAN ===");
        System.out.println("  ID        : " + fan.getId());
        System.out.println("  Name      : " + fan.getName());
        System.out.println("  Email     : " + fan.getEmail());
        System.out.println("  Phone     : " + fan.getPhone());
        System.out.println("  Hash Pass : " + fan.getPasswordHash());
        System.out.println("  ===============================");
    }

    private void printWelcome() {
        System.out.println();
        System.out.println("  " + "=".repeat(50));
        System.out.println("  ||   TICKET BOOKING SYSTEM - ADMIN PORTAL  ||");
        System.out.println("  " + "=".repeat(50));
        System.out.println("  Admin dang dang nhap: " + currentAdminId);
    }

    private void printMenu() {
        System.out.println();
        System.out.println("  +---------------------------------------+");
        System.out.println("  |           MENU QUAN TRI               |");
        System.out.println("  +---------------------------------------+");
        System.out.println("  |  [1] Xem bao cao thong ke he thong    |");
        System.out.println("  |  [2] Xem danh sach tat ca Fan         |");
        System.out.println("  |  [3] Tim kiem Fan theo ID/Email       |");
        System.out.println("  |  [0] Dang xuat                        |");
        System.out.println("  +---------------------------------------+");
        System.out.print("  Chon: ");
    }
}
