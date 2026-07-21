package view;

import model.Fan;
import repository.FanRepository;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * ManageFanView – Quan ly Fan / Nguoi dung (Admin).
 * Use case: Read Users Info, Update Users, Delete Users.
 */
public class ManageFanView {

    private final FanRepository fanRepo;
    private final Scanner       scanner;

    public ManageFanView() {
        this.fanRepo  = new FanRepository();
        this.scanner  = new Scanner(System.in);
    }

    public ManageFanView(Scanner scanner) {
        this.fanRepo  = new FanRepository();
        this.scanner  = scanner;
    }

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> listAllFans();
                case "2" -> searchFan();
                case "3" -> updateFan();
                case "4" -> deleteFan();
                case "0" -> running = false;
                default  -> System.out.println("  [!] Lua chon khong hop le!");
            }
        }
    }

    private void listAllFans() {
        List<Fan> fans = fanRepo.findAll();
        System.out.println();
        System.out.println("  === DANH SACH FAN (" + fans.size() + " nguoi dung) ===");
        System.out.printf("  %-10s %-20s %-30s %-12s%n", "ID", "Ho ten", "Email", "Phone");
        System.out.println("  " + "-".repeat(74));
        int count = 0;
        for (Fan f : fans) {
            System.out.printf("  %-10s %-20s %-30s %-12s%n",
                    f.getId(), f.getName(), f.getEmail(), f.getPhone());
            if (++count >= 20) {
                System.out.print("  ... [" + (fans.size() - 20) + " fan khac] Xem tiep? (y/n): ");
                if (!"y".equalsIgnoreCase(scanner.nextLine().trim())) break;
            }
        }
    }

    private void searchFan() {
        System.out.print("  Nhap Fan ID hoac email de tim: ");
        String query = scanner.nextLine().trim().toLowerCase();
        List<Fan> found = fanRepo.findByCondition(
                f -> f.getId().toLowerCase().contains(query)
                  || f.getEmail().toLowerCase().contains(query));
        if (found.isEmpty()) { System.out.println("  [!] Khong tim thay fan nao."); return; }
        System.out.println("  Tim thay " + found.size() + " ket qua:");
        for (Fan f : found) {
            System.out.printf("  %-10s %-20s %-30s%n", f.getId(), f.getName(), f.getEmail());
        }
    }

    private void updateFan() {
        System.out.print("  Nhap Fan ID can sua: ");
        String id = scanner.nextLine().trim();
        Optional<Fan> opt = fanRepo.findById(id);
        if (opt.isEmpty()) { System.out.println("  [!] Khong tim thay: " + id); return; }
        Fan f = opt.get();

        System.out.println("  [Enter de giu nguyen]");
        System.out.print("  Ho ten (" + f.getName() + "): ");
        String name = scanner.nextLine().trim();
        if (!name.isBlank()) f.setName(name);

        System.out.print("  Phone (" + f.getPhone() + "): ");
        String phone = scanner.nextLine().trim();
        if (!phone.isBlank()) f.setPhone(phone);

        if (fanRepo.save(f)) System.out.println("  [OK] Da cap nhat fan: " + id);
        else                 System.out.println("  [!] Loi cap nhat!");
    }

    private void deleteFan() {
        System.out.print("  Nhap Fan ID can xoa: ");
        String id = scanner.nextLine().trim();
        System.out.print("  Xac nhan xoa fan " + id + "? (y/n): ");
        if (!"y".equalsIgnoreCase(scanner.nextLine().trim())) { System.out.println("  Huy."); return; }
        if (fanRepo.deleteById(id)) System.out.println("  [OK] Da xoa fan: " + id);
        else                        System.out.println("  [!] Khong tim thay hoac loi xoa!");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("  +---------------------------------------+");
        System.out.println("  |         MANAGE FAN (USERS)            |");
        System.out.println("  +---------------------------------------+");
        System.out.println("  |  [1] Xem danh sach tat ca fan         |");
        System.out.println("  |  [2] Tim kiem fan (ID / email)        |");
        System.out.println("  |  [3] Cap nhat thong tin fan           |");
        System.out.println("  |  [4] Xoa fan                          |");
        System.out.println("  |  [0] Quay lai                         |");
        System.out.println("  +---------------------------------------+");
        System.out.print("  Chon: ");
    }
}
