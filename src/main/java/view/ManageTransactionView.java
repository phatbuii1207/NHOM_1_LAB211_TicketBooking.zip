package view;

import model.BookingTransaction;
import repository.TransactionRepository;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * ManageTransactionView – Quan ly Giao dich (Admin).
 * Use case: Create Transaction, View Transaction, Update Transaction, Delete Transaction.
 */
public class ManageTransactionView {

    private final TransactionRepository transRepo;
    private final Scanner               scanner;

    public ManageTransactionView() {
        this.transRepo = new TransactionRepository();
        this.scanner   = new Scanner(System.in);
    }

    public ManageTransactionView(Scanner scanner) {
        this.transRepo = new TransactionRepository();
        this.scanner   = scanner;
    }

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> viewAllTransactions();
                case "2" -> viewTransactionById();
                case "3" -> viewByFanId();
                case "4" -> updateTransactionStatus();
                case "5" -> deleteTransaction();
                case "0" -> running = false;
                default  -> System.out.println("  [!] Lua chon khong hop le!");
            }
        }
    }

    private void viewAllTransactions() {
        List<BookingTransaction> all = transRepo.findAll();
        System.out.println();
        System.out.println("  === DANH SACH GIAO DICH (" + all.size() + " giao dich) ===");
        System.out.printf("  %-12s %-12s %-10s %12s %-12s %-20s%n",
                "Trans ID", "Ticket ID", "Fan ID", "So tien", "Trang thai", "Ngay gio");
        System.out.println("  " + "-".repeat(80));

        int shown = 0;
        for (BookingTransaction t : all) {
            System.out.printf("  %-12s %-12s %-10s %,12.0f %-12s %-20s%n",
                    t.getId(), t.getTicketId(), t.getFanId(),
                    t.getAmount(), t.getStatus(), t.getCreatedAt());
            if (++shown >= 20) {
                System.out.print("  ... Xem tiep? (y/n): ");
                if (!"y".equalsIgnoreCase(scanner.nextLine().trim())) break;
                shown = 0;
            }
        }
    }

    private void viewTransactionById() {
        System.out.print("  Nhap Transaction ID: ");
        String id = scanner.nextLine().trim();
        Optional<BookingTransaction> opt = transRepo.findById(id);
        if (opt.isEmpty()) { System.out.println("  [!] Khong tim thay: " + id); return; }
        BookingTransaction t = opt.get();
        System.out.println("  ID         : " + t.getId());
        System.out.println("  Ticket ID  : " + t.getTicketId());
        System.out.println("  Fan ID     : " + t.getFanId());
        System.out.printf ("  So tien    : %,.0f VND%n", t.getAmount());
        System.out.println("  Trang thai : " + t.getStatus());
        System.out.println("  Ngay tao   : " + t.getCreatedAt());
    }

    private void viewByFanId() {
        System.out.print("  Nhap Fan ID: ");
        String fanId = scanner.nextLine().trim();
        List<BookingTransaction> result = transRepo.findByCondition(
                t -> fanId.equals(t.getFanId()));
        System.out.println("  Tim thay " + result.size() + " giao dich cua " + fanId + ":");
        double total = 0;
        for (BookingTransaction t : result) {
            System.out.printf("  %-12s %-12s %,12.0f %-12s%n",
                    t.getId(), t.getTicketId(), t.getAmount(), t.getStatus());
            if ("COMPLETED".equals(t.getStatus())) total += t.getAmount();
        }
        System.out.printf("  Tong chi tieu: %,.0f VND%n", total);
    }

    private void updateTransactionStatus() {
        System.out.print("  Nhap Transaction ID: ");
        String id = scanner.nextLine().trim();
        Optional<BookingTransaction> opt = transRepo.findById(id);
        if (opt.isEmpty()) { System.out.println("  [!] Khong tim thay: " + id); return; }
        BookingTransaction t = opt.get();
        System.out.println("  Trang thai hien tai: " + t.getStatus());
        System.out.println("  Trang thai moi: PENDING / COMPLETED / CANCELLED / FAILED");
        System.out.print("  Nhap: ");
        String status = scanner.nextLine().trim().toUpperCase();
        t.setStatus(status);
        if (transRepo.save(t)) System.out.println("  [OK] Da cap nhat trang thai → " + status);
        else                   System.out.println("  [!] Loi cap nhat!");
    }

    private void deleteTransaction() {
        System.out.print("  Nhap Transaction ID can xoa: ");
        String id = scanner.nextLine().trim();
        System.out.print("  Xac nhan xoa " + id + "? (y/n): ");
        if (!"y".equalsIgnoreCase(scanner.nextLine().trim())) { System.out.println("  Huy."); return; }
        if (transRepo.deleteById(id)) System.out.println("  [OK] Da xoa giao dich: " + id);
        else                          System.out.println("  [!] Khong tim thay hoac loi xoa!");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("  +---------------------------------------+");
        System.out.println("  |       MANAGE TRANSACTION              |");
        System.out.println("  +---------------------------------------+");
        System.out.println("  |  [1] Xem tat ca giao dich            |");
        System.out.println("  |  [2] Xem giao dich theo ID           |");
        System.out.println("  |  [3] Xem giao dich theo Fan          |");
        System.out.println("  |  [4] Cap nhat trang thai giao dich   |");
        System.out.println("  |  [5] Xoa giao dich                   |");
        System.out.println("  |  [0] Quay lai                        |");
        System.out.println("  +---------------------------------------+");
        System.out.print("  Chon: ");
    }
}
