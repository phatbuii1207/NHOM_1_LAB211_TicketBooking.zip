package view;

import model.Seat;
import model.SeatStatus;
import model.Ticket;
import model.BookingTransaction;
import repository.SeatRepository;
import repository.TicketRepository;
import repository.TransactionRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ReportView – Hiển thị báo cáo thống kê trên console.
 *
 * BÁO CÁO BAO GỒM:
 *   1. Tổng quan hệ thống (tổng vé, doanh thu, ghế trống/đã đặt)
 *   2. Top fan mua nhiều vé nhất
 *   3. Thống kê theo khu vực (ghế trống/đã đặt)
 *   4. Danh sách giao dịch gần đây
 *
 * KẾT NỐI MVC:
 *   ReportView (View) → TicketRepository / SeatRepository / TransactionRepository (Model)
 *   View chỉ đọc dữ liệu và format để in ra, KHÔNG sửa gì cả
 */
public class ReportView {

    private final TicketRepository      ticketRepo;
    private final TransactionRepository transRepo;
    private final SeatRepository        seatRepo;

    public ReportView() {
        this.ticketRepo = new TicketRepository();
        this.transRepo  = new TransactionRepository();
        this.seatRepo   = new SeatRepository();
    }

    // ================================================================
    // MENU BÁO CÁO
    // ================================================================

    /**
     * Hiển thị toàn bộ báo cáo.
     */
    public void run() {
        printHeader("BAO CAO HE THONG TICKET BOOKING");
        reportOverview();
        reportSeatsByStatus();
        reportTopFans();
        reportRecentTransactions(5);
    }

    // ================================================================
    // BÁO CÁO 1 – TỔNG QUAN
    // ================================================================

    /**
     * In tổng quan: tổng vé, doanh thu, số ghế trống/đã đặt.
     */
    public void reportOverview() {
        List<Ticket>            tickets = ticketRepo.findAll();
        List<BookingTransaction> txns   = transRepo.findAll();
        List<Seat>              seats   = seatRepo.findAll();

        long totalTickets   = tickets.size();
        double totalRevenue = txns.stream()
                .filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus()))
                .mapToDouble(BookingTransaction::getAmount)
                .sum();
        long available = seats.stream().filter(s -> s.getStatus() == SeatStatus.AVAILABLE).count();
        long booked    = seats.stream().filter(s -> s.getStatus() == SeatStatus.BOOKED).count();
        long locked    = seats.stream().filter(s -> s.getStatus() == SeatStatus.LOCKED).count();

        System.out.println("  --- TONG QUAN ---");
        System.out.printf("  Tong so ve da ban    : %,d ve%n",    totalTickets);
        System.out.printf("  Tong doanh thu       : %,.0f VND%n", totalRevenue);
        System.out.printf("  Tong so ghe          : %,d%n",       seats.size());
        System.out.printf("  Ghe trong (AVAILABLE): %,d%n",       available);
        System.out.printf("  Ghe da dat (BOOKED)  : %,d%n",       booked);
        System.out.printf("  Ghe bi khoa (LOCKED) : %,d%n",       locked);
        double occupancy = seats.isEmpty() ? 0 : (booked * 100.0 / seats.size());
        System.out.printf("  Ti le lap day        : %.1f%%%n",    occupancy);
        System.out.println();
    }

    // ================================================================
    // BÁO CÁO 2 – THỐNG KÊ GHẾ THEO KHU VỰC
    // ================================================================

    /**
     * In số ghế trống/đã đặt theo từng khu vực.
     */
    public void reportSeatsByStatus() {
        List<Seat> seats = seatRepo.findAll();

        // Nhóm theo sectionId
        Map<String, List<Seat>> bySec = seats.stream()
                .collect(Collectors.groupingBy(Seat::getSectionId));

        System.out.println("  --- GHE THEO KHU VUC ---");
        System.out.printf("  %-15s %8s %8s %8s %8s%n",
                "Khu vuc", "Tong", "Trong", "Da dat", "Khoa");
        System.out.println("  " + "-".repeat(55));

        bySec.entrySet().stream()
             .sorted(Map.Entry.comparingByKey())
             .forEach(e -> {
                 String sec   = e.getKey();
                 List<Seat> ss = e.getValue();
                 long total  = ss.size();
                 long avail  = ss.stream().filter(s -> s.getStatus() == SeatStatus.AVAILABLE).count();
                 long bkd    = ss.stream().filter(s -> s.getStatus() == SeatStatus.BOOKED).count();
                 long lkd    = ss.stream().filter(s -> s.getStatus() == SeatStatus.LOCKED).count();
                 System.out.printf("  %-15s %8d %8d %8d %8d%n", sec, total, avail, bkd, lkd);
             });
        System.out.println();
    }

    // ================================================================
    // BÁO CÁO 3 – TOP FAN MUA NHIỀU VÉ NHẤT
    // ================================================================

    /**
     * In top N fan có nhiều vé nhất.
     */
    public void reportTopFans() {
        List<Ticket> tickets = ticketRepo.findAll();
        if (tickets.isEmpty()) {
            System.out.println("  --- TOP FAN ---");
            System.out.println("  [!] Chua co ve nao duoc ban.");
            System.out.println();
            return;
        }

        // Đếm vé theo fanId
        Map<String, Long> countByFan = tickets.stream()
                .collect(Collectors.groupingBy(Ticket::getFanId, Collectors.counting()));

        System.out.println("  --- TOP FAN MUA NHIEU VE NHAT ---");
        System.out.printf("  %-3s %-12s %8s %12s%n", "STT", "Fan ID", "So ve", "Tong chi");
        System.out.println("  " + "-".repeat(40));

        // Tính tổng tiền từng fan (từ transRepo)
        Map<String, Double> totalByFan = transRepo.findAll().stream()
                .filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus()))
                .collect(Collectors.groupingBy(
                        BookingTransaction::getFanId,
                        Collectors.summingDouble(BookingTransaction::getAmount)));

        int[] rank = {1};
        countByFan.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue())) // giảm dần
                .limit(5)
                .forEach(e -> {
                    String fanId = e.getKey();
                    long   count = e.getValue();
                    double total = totalByFan.getOrDefault(fanId, 0.0);
                    System.out.printf("  %-3d %-12s %8d %,12.0f VND%n",
                            rank[0]++, fanId, count, total);
                });
        System.out.println();
    }

    // ================================================================
    // BÁO CÁO 4 – GIAO DỊCH GẦN ĐÂY
    // ================================================================

    /**
     * In N giao dịch gần đây nhất.
     *
     * @param limit Số giao dịch muốn xem
     */
    public void reportRecentTransactions(int limit) {
        List<BookingTransaction> all = transRepo.findAll();
        if (all.isEmpty()) {
            System.out.println("  --- GIAO DICH GAN DAY ---");
            System.out.println("  [!] Chua co giao dich nao.");
            System.out.println();
            return;
        }

        System.out.println("  --- GIAO DICH GAN DAY (toi da " + limit + ") ---");
        System.out.printf("  %-12s %-12s %-12s %12s %10s%n",
                "TXN ID", "Ticket ID", "Fan ID", "So tien", "Trang thai");
        System.out.println("  " + "-".repeat(65));

        // Lấy N giao dịch cuối cùng (mới nhất ở dưới → reverse)
        int start = Math.max(0, all.size() - limit);
        all.subList(start, all.size()).forEach(t ->
            System.out.printf("  %-12s %-12s %-12s %,12.0f %10s%n",
                t.getId(), t.getTicketId(), t.getFanId(),
                t.getAmount(), t.getStatus()));
        System.out.println();
    }

    // ================================================================
    // NỘI BỘ
    // ================================================================

    private void printHeader(String title) {
        System.out.println();
        System.out.println("  " + "=".repeat(60));
        System.out.printf ("  %-60s%n", "  " + title);
        System.out.println("  " + "=".repeat(60));
    }
}
