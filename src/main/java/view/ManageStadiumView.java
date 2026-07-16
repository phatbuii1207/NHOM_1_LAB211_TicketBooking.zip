package view;

import model.Section;
import model.Stadium;
import repository.SectionRepository;
import repository.StadiumRepository;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * ManageStadiumView – Quan ly San van dong va Khu vuc (Admin).
 * Use case: Add Stadium, Read Stadium Info, Update Stadium, Delete Stadium,
 *           Create Section, Read Section Info, Update Section, Delete Section.
 */
public class ManageStadiumView {

    private final StadiumRepository stadiumRepo;
    private final SectionRepository sectionRepo;
    private final Scanner           scanner;

    public ManageStadiumView() {
        this.stadiumRepo = new StadiumRepository();
        this.sectionRepo = new SectionRepository();
        this.scanner     = new Scanner(System.in);
    }

    public ManageStadiumView(Scanner scanner) {
        this.stadiumRepo = new StadiumRepository();
        this.sectionRepo = new SectionRepository();
        this.scanner     = scanner;
    }

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> viewAllStadiums();
                case "2" -> createStadium();
                case "3" -> updateStadium();
                case "4" -> deleteStadium();
                case "5" -> viewSectionsByStadium();
                case "6" -> createSection();
                case "0" -> running = false;
                default  -> System.out.println("  [!] Lua chon khong hop le!");
            }
        }
    }

    // ================================================================
    // STADIUM
    // ================================================================

    private void viewAllStadiums() {
        List<Stadium> stadiums = stadiumRepo.findAll();
        System.out.println();
        System.out.println("  === DANH SACH SAN VAN DONG (" + stadiums.size() + " san) ===");
        System.out.printf("  %-8s %-25s %-20s %10s%n", "ID", "Ten san", "Dia diem", "Suc chua");
        System.out.println("  " + "-".repeat(65));
        for (Stadium s : stadiums) {
            System.out.printf("  %-8s %-25s %-20s %,10d%n",
                    s.getId(), s.getName(), s.getLocation(), s.getCapacity());
        }
    }

    private void createStadium() {
        System.out.println("  === THEM SAN VAN DONG MOI ===");
        System.out.print("  Stadium ID (VD: ST004): ");
        String id = scanner.nextLine().trim();
        if (stadiumRepo.findById(id).isPresent()) {
            System.out.println("  [!] ID da ton tai!"); return;
        }
        System.out.print("  Ten san: ");
        String name = scanner.nextLine().trim();
        System.out.print("  Dia diem: ");
        String location = scanner.nextLine().trim();
        System.out.print("  Suc chua (so nguoi): ");
        int capacity;
        try { capacity = Integer.parseInt(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { System.out.println("  [!] So khong hop le!"); return; }

        Stadium s = new Stadium(id, name, location, capacity);
        if (stadiumRepo.save(s)) System.out.println("  [OK] Da them san: " + id);
        else                     System.out.println("  [!] Loi khi them san!");
    }

    private void updateStadium() {
        System.out.print("  Nhap Stadium ID can sua: ");
        String id = scanner.nextLine().trim();
        Optional<Stadium> opt = stadiumRepo.findById(id);
        if (opt.isEmpty()) { System.out.println("  [!] Khong tim thay: " + id); return; }
        Stadium s = opt.get();

        System.out.println("  [Enter de giu nguyen]");
        System.out.print("  Ten san (" + s.getName() + "): ");
        String name = scanner.nextLine().trim();
        if (!name.isBlank()) s.setName(name);

        System.out.print("  Dia diem (" + s.getLocation() + "): ");
        String loc = scanner.nextLine().trim();
        if (!loc.isBlank()) s.setLocation(loc);

        if (stadiumRepo.save(s)) System.out.println("  [OK] Da cap nhat: " + id);
        else                     System.out.println("  [!] Loi cap nhat!");
    }

    private void deleteStadium() {
        System.out.print("  Nhap Stadium ID can xoa: ");
        String id = scanner.nextLine().trim();
        System.out.print("  Xac nhan xoa san " + id + "? (y/n): ");
        if (!"y".equalsIgnoreCase(scanner.nextLine().trim())) { System.out.println("  Huy."); return; }
        if (stadiumRepo.deleteById(id)) System.out.println("  [OK] Da xoa san: " + id);
        else                            System.out.println("  [!] Khong tim thay hoac loi!");
    }

    // ================================================================
    // SECTION
    // ================================================================

    private void viewSectionsByStadium() {
        System.out.print("  Nhap Stadium ID (VD: ST001): ");
        String stadiumId = scanner.nextLine().trim();
        List<Section> sections = sectionRepo.findByStadiumId(stadiumId);
        if (sections.isEmpty()) {
            System.out.println("  [!] Khong co khu vuc nao trong san: " + stadiumId); return;
        }
        System.out.println();
        System.out.println("  === KHU VUC CUA SAN " + stadiumId + " ===");
        System.out.printf("  %-12s %-15s %-10s %12s %6s %6s%n",
                "Section ID", "Ten khu", "Loai", "Gia ve", "Hang", "Ghe/Hang");
        System.out.println("  " + "-".repeat(65));
        for (Section sec : sections) {
            System.out.printf("  %-12s %-15s %-10s %,12.0f %6d %8d%n",
                    sec.getId(), sec.getName(), sec.getType(),
                    sec.getPrice(), sec.getTotalRows(), sec.getSeatsPerRow());
        }
    }

    private void createSection() {
        System.out.println("  === THEM KHU VUC MOI ===");
        System.out.print("  Section ID (VD: ST001_S08): ");
        String id = scanner.nextLine().trim();
        System.out.print("  Stadium ID: ");
        String stadiumId = scanner.nextLine().trim();
        System.out.print("  Ten khu (VD: VIP A): ");
        String name = scanner.nextLine().trim();
        System.out.print("  Loai (VIP/NORMAL/STANDING): ");
        String type = scanner.nextLine().trim();
        System.out.print("  Gia ve (VND): ");
        double price;
        try { price = Double.parseDouble(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { System.out.println("  [!] Gia khong hop le!"); return; }
        System.out.print("  So hang: ");
        int rows;
        try { rows = Integer.parseInt(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { System.out.println("  [!] So hang khong hop le!"); return; }
        System.out.print("  Ghe moi hang: ");
        int perRow;
        try { perRow = Integer.parseInt(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { System.out.println("  [!] So ghe khong hop le!"); return; }

        Section sec = new Section(id, stadiumId, name, type, price, rows, perRow);
        if (sectionRepo.save(sec)) System.out.println("  [OK] Da them khu vuc: " + id);
        else                       System.out.println("  [!] Loi khi them khu vuc!");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("  +-------------------------------------------+");
        System.out.println("  |       MANAGE STADIUM & SECTION            |");
        System.out.println("  +-------------------------------------------+");
        System.out.println("  |  [1] Xem danh sach san van dong           |");
        System.out.println("  |  [2] Them san van dong moi                |");
        System.out.println("  |  [3] Sua thong tin san van dong           |");
        System.out.println("  |  [4] Xoa san van dong                     |");
        System.out.println("  |  [5] Xem khu vuc cua san (Section)       |");
        System.out.println("  |  [6] Them khu vuc moi                     |");
        System.out.println("  |  [0] Quay lai                             |");
        System.out.println("  +-------------------------------------------+");
        System.out.print("  Chon: ");
    }
}
