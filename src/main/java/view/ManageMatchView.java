package view;

import model.Match;
import repository.MatchRepository;
import repository.StadiumRepository;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * ManageMatchView – Quan ly Tran dau (Admin).
 * Use case: Create Match, Read Match Info, Update Match, Delete Match.
 */
public class ManageMatchView {

    private final MatchRepository   matchRepo;
    private final StadiumRepository stadiumRepo;
    private final Scanner           scanner;

    public ManageMatchView() {
        this.matchRepo   = new MatchRepository();
        this.stadiumRepo = new StadiumRepository();
        this.scanner     = new Scanner(System.in);
    }

    public ManageMatchView(Scanner scanner) {
        this.matchRepo   = new MatchRepository();
        this.stadiumRepo = new StadiumRepository();
        this.scanner     = scanner;
    }

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> viewAllMatches();
                case "2" -> viewMatchById();
                case "3" -> createMatch();
                case "4" -> updateMatch();
                case "5" -> deleteMatch();
                case "0" -> running = false;
                default  -> System.out.println("  [!] Lua chon khong hop le!");
            }
        }
    }

    private void viewAllMatches() {
        List<Match> matches = matchRepo.findAll();
        System.out.println();
        System.out.println("  === DANH SACH TRAN DAU (" + matches.size() + " tran) ===");
        System.out.printf("  %-10s %-20s %-20s %-22s %-8s%n",
                "ID", "Doi chu", "Doi khach", "Ngay gio", "San");
        System.out.println("  " + "-".repeat(82));
        for (Match m : matches) {
            System.out.printf("  %-10s %-20s %-20s %-22s %-8s%n",
                    m.getId(), m.getHomeTeam(), m.getAwayTeam(),
                    m.getMatchDate(), m.getStadiumId());
        }
    }

    private void viewMatchById() {
        System.out.print("  Nhap Match ID: ");
        String id = scanner.nextLine().trim();
        Optional<Match> opt = matchRepo.findById(id);
        if (opt.isEmpty()) { System.out.println("  [!] Khong tim thay tran: " + id); return; }
        Match m = opt.get();
        System.out.println("  ID       : " + m.getId());
        System.out.println("  Doi chu  : " + m.getHomeTeam());
        System.out.println("  Doi khach: " + m.getAwayTeam());
        System.out.println("  Ngay gio : " + m.getMatchDate());
        System.out.println("  San      : " + m.getStadiumId());
        System.out.println("  Kick-off : " + m.getKickoffTime());
    }

    private void createMatch() {
        System.out.println("  === TAO TRAN MO'I ===");
        System.out.print("  Match ID (VD: MATCH011): ");
        String id = scanner.nextLine().trim();
        if (matchRepo.findById(id).isPresent()) {
            System.out.println("  [!] ID da ton tai!"); return;
        }
        System.out.print("  Doi chu nha: ");
        String home = scanner.nextLine().trim();
        System.out.print("  Doi khach : ");
        String away = scanner.nextLine().trim();
        System.out.print("  Ngay gio (YYYY-MM-DD HH:mm:ss): ");
        String date = scanner.nextLine().trim();
        System.out.print("  Stadium ID (ST001/ST002/ST003): ");
        String stadium = scanner.nextLine().trim();
        System.out.print("  Gio kick-off (HH:mm:ss): ");
        String kickoff = scanner.nextLine().trim();

        Match m = new Match(id, home, away, date, stadium, kickoff);
        if (matchRepo.save(m)) {
            System.out.println("  [OK] Da tao tran: " + id);
        } else {
            System.out.println("  [!] Loi khi tao tran!");
        }
    }

    private void updateMatch() {
        System.out.print("  Nhap Match ID can sua: ");
        String id = scanner.nextLine().trim();
        Optional<Match> opt = matchRepo.findById(id);
        if (opt.isEmpty()) { System.out.println("  [!] Khong tim thay: " + id); return; }
        Match m = opt.get();

        System.out.println("  [Enter de giu nguyen gia tri cu]");
        System.out.print("  Doi chu (" + m.getHomeTeam() + "): ");
        String home = scanner.nextLine().trim();
        if (!home.isBlank()) m.setHomeTeam(home);

        System.out.print("  Doi khach (" + m.getAwayTeam() + "): ");
        String away = scanner.nextLine().trim();
        if (!away.isBlank()) m.setAwayTeam(away);

        System.out.print("  Ngay gio (" + m.getMatchDate() + "): ");
        String date = scanner.nextLine().trim();
        if (!date.isBlank()) m.setMatchDate(date);

        System.out.print("  Stadium ID (" + m.getStadiumId() + "): ");
        String stadium = scanner.nextLine().trim();
        if (!stadium.isBlank()) m.setStadiumId(stadium);

        if (matchRepo.save(m)) System.out.println("  [OK] Da cap nhat: " + id);
        else                   System.out.println("  [!] Loi cap nhat!");
    }

    private void deleteMatch() {
        System.out.print("  Nhap Match ID can xoa: ");
        String id = scanner.nextLine().trim();
        System.out.print("  Xac nhan xoa " + id + "? (y/n): ");
        if (!"y".equalsIgnoreCase(scanner.nextLine().trim())) { System.out.println("  Huy."); return; }
        if (matchRepo.deleteById(id)) System.out.println("  [OK] Da xoa: " + id);
        else                          System.out.println("  [!] Khong tim thay hoac loi xoa!");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("  +---------------------------------------+");
        System.out.println("  |         MANAGE MATCH                  |");
        System.out.println("  +---------------------------------------+");
        System.out.println("  |  [1] Xem danh sach tran dau           |");
        System.out.println("  |  [2] Xem chi tiet tran theo ID        |");
        System.out.println("  |  [3] Tao tran moi                     |");
        System.out.println("  |  [4] Sua thong tin tran               |");
        System.out.println("  |  [5] Xoa tran                         |");
        System.out.println("  |  [0] Quay lai                         |");
        System.out.println("  +---------------------------------------+");
        System.out.print("  Chon: ");
    }
}
