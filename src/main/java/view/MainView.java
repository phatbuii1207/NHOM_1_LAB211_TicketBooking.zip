package view;

import controller.AuthController.AuthSession;
import model.Match;
import model.Ticket;
import repository.MatchRepository;
import repository.TicketRepository;
import java.util.List;
import java.util.Scanner;

/**
 * MainView – Menu chinh ket noi toan bo he thong Ticket Booking.
 *
 * KET NOI MVC:
 *   MainView
 *     +-- AuthView         -> AuthController   -> FanRepository
 *     +-- AdminView        -> 7 sub-views (Seat/Trans/Fan/Match/Stadium/Report/Simulator)
 *     +-- BookingView      -> BookingController -> SeatRepo/TicketRepo/TransRepo
 *     +-- SeatMapView      -> SeatRepository
 *     +-- MatchRepository  -> matches.csv
 *
 * FAN MENU (theo Use Case):
 *   [1] Xem danh sach tran dau  – View Matches List
 *   [2] Xem ban do ghe          – Render ASCII Seat Map
 *   [3] Dat ve                  – Book Tickets
 *   [4] Xem ve cua toi          – View My Tickets
 *   [5] Huy ve                  – Cancel Booking
 *   [6] Doi tai khoan           – Logout / Switch
 *   [0] Thoat
 *
 * ADMIN MENU -> delegated to AdminView (7 modules theo use case)
 */
public class MainView {

    private final AuthView      authView;
    private final AdminView     adminView;
    private final BookingView   bookingView;
    private final SeatMapView   seatMapView;
    private final MatchRepository matchRepo;
    private final Scanner       scanner;

    private AuthSession currentSession = null;

    public MainView() {
        this.scanner     = new Scanner(System.in);
        this.authView    = new AuthView();
        this.adminView   = new AdminView();
        this.bookingView = new BookingView();
        this.seatMapView = new SeatMapView();
        this.matchRepo   = new MatchRepository();
    }

    // ================================================================
    // MAIN ENTRY POINT
    // ================================================================

    public static void main(String[] args) {
        new MainView().run();
    }

    public void run() {
        currentSession = authView.run();
        if (currentSession == null) {
            System.out.println("  Tam biet!");
            return;
        }

        if (currentSession.isAdmin()) {
            runAdminLoop();
        } else {
            runFanLoop();
        }

        System.out.println();
        System.out.println("  Cam on da su dung Ticket Booking System. Tam biet!");
        System.out.println("  " + "=".repeat(50));
        scanner.close();
    }

    // ================================================================
    // VONG LAP FAN (theo Use Case)
    // ================================================================

    private void runFanLoop() {
        boolean running = true;
        while (running) {
            printFanMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> handleViewMatches();           // View Matches List
                case "2" -> handleSeatMap();               // Render ASCII Seat Map
                case "3" -> bookingView.run(currentSession.getId());  // Book Tickets
                case "4" -> handleViewMyTickets();          // View My Tickets
                case "5" -> bookingView.runCancel(currentSession.getId()); // Cancel Booking
                case "6" -> handleSwitchAccount();
                case "0" -> running = false;
                default  -> System.out.println("  [!] Lua chon khong hop le.");
            }
        }
    }

    // ================================================================
    // VONG LAP ADMIN
    // ================================================================

    private void runAdminLoop() {
        adminView.run();
    }

    // ================================================================
    // [1] XEM DANH SACH TRAN DAU – View Matches List
    // ================================================================

    private void handleViewMatches() {
        List<Match> matches = matchRepo.findAll();
        System.out.println();
        System.out.println("  === DANH SACH TRAN DAU (" + matches.size() + " tran) ===");
        System.out.println("  " + "-".repeat(78));
        System.out.printf("  %-10s %-18s %-18s %-22s %-8s %s%n",
                "Ma tran", "Doi chu nha", "Doi khach", "Ngay gio", "San", "Kick-off");
        System.out.println("  " + "-".repeat(78));
        for (Match m : matches) {
            System.out.printf("  %-10s %-18s %-18s %-22s %-8s %s%n",
                    m.getId(), m.getHomeTeam(), m.getAwayTeam(),
                    m.getMatchDate(), m.getStadiumId(), m.getKickoffTime());
        }
        System.out.println("  " + "-".repeat(78));
        System.out.println("  [Ghi chu] Dung ma tran (VD: MATCH001) khi dat ve.");
        System.out.println();
    }

    // ================================================================
    // [2] XEM BAN DO GHE
    // ================================================================

    private void handleSeatMap() {
        System.out.println();
        System.out.println("  --- XEM BAN DO GHE ---");
        System.out.println("  Vi du khu vuc: ST001_S01, ST002_S01, ST003_S01 ...");
        System.out.print("  Nhap ma khu vuc: ");
        String sectionId = scanner.nextLine().trim();
        if (sectionId.isBlank()) {
            System.out.println("  [!] Ma khu vuc khong duoc de trong.");
            return;
        }
        System.out.print("  Ten khu vuc (co the bo trong): ");
        String sectionName = scanner.nextLine().trim();
        if (sectionName.isBlank()) sectionName = sectionId;
        seatMapView.render(sectionId, sectionName);
    }

    // ================================================================
    // [4] XEM VE CUA TOI – View My Tickets
    // ================================================================

    private void handleViewMyTickets() {
        String fanId = currentSession.getId();
        TicketRepository ticketRepo = new TicketRepository();
        List<Ticket> myTickets = ticketRepo.findByFanId(fanId);

        System.out.println();
        System.out.println("  === VE CUA TOI (" + currentSession.getDisplayName() + ") ===");

        if (myTickets.isEmpty()) {
            System.out.println("  [!] Ban chua mua ve nao.");
            System.out.println("  Chon [3] Dat ve de mua ve.");
        } else {
            System.out.printf("  Co %d ve:%n", myTickets.size());
            System.out.println("  " + "-".repeat(68));
            System.out.printf("  %-14s %-10s %-16s %12s  %s%n",
                    "Ma ve", "Tran", "Ghe", "Gia (VND)", "Ngay mua");
            System.out.println("  " + "-".repeat(68));
            myTickets.forEach(t ->
                System.out.printf("  %-14s %-10s %-16s %,12.0f  %s%n",
                    t.getId(), t.getMatchId(), t.getSeatId(),
                    t.getPrice(), t.getPurchasedAt()));
            System.out.println("  " + "-".repeat(68));
            double total = myTickets.stream().mapToDouble(Ticket::getPrice).sum();
            System.out.printf("  Tong ve: %d  |  Tong tien: %,.0f VND%n",
                    myTickets.size(), total);
        }
        System.out.println();
    }

    // ================================================================
    // [6] DOI TAI KHOAN / DANG XUAT
    // ================================================================

    private void handleSwitchAccount() {
        System.out.println();
        System.out.println("  Dang xuat: " + currentSession.getDisplayName());
        System.out.print("  Ban co chac muon dang xuat? (y/n): ");
        String confirm = scanner.nextLine().trim();
        if (confirm.equalsIgnoreCase("y")) {
            currentSession = authView.run();
            if (currentSession == null) {
                System.exit(0);
            }
            if (currentSession.isAdmin()) {
                adminView.run();
                currentSession = authView.run();
                if (currentSession == null) System.exit(0);
            }
        }
    }

    // ================================================================
    // UI HELPERS
    // ================================================================

    private void printFanMenu() {
        System.out.println();
        System.out.println("  Xin chao, " + currentSession.getDisplayName()
                + "! (" + currentSession.getId() + ")");
        System.out.println("  +-------------------------------------------+");
        System.out.println("  |               MENU FAN                    |");
        System.out.println("  +-------------------------------------------+");
        System.out.println("  |  [1] Xem danh sach tran dau               |");
        System.out.println("  |  [2] Xem ban do ghe                       |");
        System.out.println("  |  [3] Dat ve                               |");
        System.out.println("  |  [4] Xem ve cua toi                       |");
        System.out.println("  |  [5] Huy ve                               |");
        System.out.println("  |  [6] Doi tai khoan / Dang xuat            |");
        System.out.println("  |  [0] Thoat                                |");
        System.out.println("  +-------------------------------------------+");
        System.out.print("  Chon: ");
    }
}
