package view;

import model.Seat;
import model.SeatStatus;
import repository.SeatRepository;

import java.util.List;

/**
 * SeatMapView – Hiển thị bản đồ ghế ASCII trên console.
 *
 * Ký hiệu:
 * [ ] = AVAILABLE (còn trống)
 * [X] = BOOKED (đã đặt)
 * [L] = LOCKED (đang bị khoá)
 *
 * VÍ DỤ OUTPUT:
 * ============================
 * KHU VỰC: ST001_S01 | VIP A
 * ============================
 * 01 02 03 04 05 ...
 * 01 [ ] [ ] [X] [ ] [L]
 * 02 [ ] [ ] [ ] [ ] [ ]
 * ============================
 * Trống: 498 | Đặt: 1 | Khoá: 1
 */
public class SeatMapView {

    private final SeatRepository seatRepo;

    public SeatMapView() {
        this.seatRepo = new SeatRepository();
    }

    // ================================================================
    // HIỂN THỊ BẢN ĐỒ GHẾ CHO 1 KHU VỰC
    // ================================================================

    /**
     * In bản đồ ghế ASCII cho 1 khu vực (sectionId).
     * Dùng SeatRepository đọc tất cả ghế → xếp vào lưới 2D → in ra console.
     *
     * @param sectionId   Mã khu vực (VD: "ST001_S01")
     * @param sectionName Tên đẹp để hiển thị (VD: "VIP A")
     */
    public void render(String sectionId, String sectionName) {
        List<Seat> seats = seatRepo.findBySectionId(sectionId);
        if (seats.isEmpty()) {
            System.out.println("  [!] Khong co ghe nao trong khu: " + sectionId);
            return;
        }

        // Tìm kích thước lưới (số hàng + số cột lớn nhất)
        int maxRow = seats.stream().mapToInt(Seat::getRowNumber).max().orElse(0);
        int maxCol = seats.stream().mapToInt(Seat::getSeatNumber).max().orElse(0);

        // Xây dựng bảng 2D trạng thái
        String[][] grid = new String[maxRow + 1][maxCol + 1];
        for (String[] row : grid)
            java.util.Arrays.fill(row, "   "); // ô trống
        for (Seat s : seats) {
            grid[s.getRowNumber()][s.getSeatNumber()] = symbolOf(s.getStatus());
        }

        // Đếm theo trạng thái
        long available = seats.stream().filter(s -> s.getStatus() == SeatStatus.AVAILABLE).count();
        long booked = seats.stream().filter(s -> s.getStatus() == SeatStatus.BOOKED).count();
        long locked = seats.stream().filter(s -> s.getStatus() == SeatStatus.LOCKED).count();

        // === IN RA ===
        String border = "=".repeat(60);
        System.out.println(border);
        System.out.printf("  KHU VUC: %-12s | %s%n", sectionId, sectionName);
        System.out.println(border);

        // In header cột (chỉ hiển thị tối đa 20 cột để vừa màn hình)
        int displayCols = Math.min(maxCol, 20);
        System.out.print("     ");
        for (int c = 1; c <= displayCols; c++)
            System.out.printf(" %02d ", c);
        if (maxCol > displayCols)
            System.out.print(" ...");
        System.out.println();

        // In từng hàng
        for (int r = 1; r <= maxRow; r++) {
            System.out.printf("  %02d ", r); // số hàng
            for (int c = 1; c <= displayCols; c++) {
                System.out.print(grid[r][c]);
            }
            if (maxCol > displayCols)
                System.out.print(" ...");
            System.out.println();
        }

        // In chú thích
        System.out.println(border);
        System.out.printf("  [ ] Trong: %-5d  [X] Da dat: %-5d  [L] Khoa: %d%n",
                available, booked, locked);
        System.out.println("  CHU THICH: [ ]=Con trong  [X]=Da dat  [L]=Dang khoa");
        System.out.println(border);
    }

    /**
     * In danh sách vài ghế trống đầu tiên (giúp user chọn nhanh).
     *
     * @param sectionId Mã khu vực
     * @param limit     Số ghế tối đa hiển thị
     */
    public void renderAvailableList(String sectionId, int limit) {
        List<Seat> available = seatRepo.findAvailableInSection(sectionId);
        System.out.println("  --- Ghe trong trong khu " + sectionId + " ---");
        if (available.isEmpty()) {
            System.out.println("  [!] Khong con ghe trong!");
            return;
        }
        available.stream().limit(limit).forEach(s -> System.out.printf("  %-12s (Hang %02d, Ghe %02d)%n",
                s.getId(), s.getRowNumber(), s.getSeatNumber()));
        if (available.size() > limit) {
            System.out.printf("  ... va %d ghe khac%n", available.size() - limit);
        }
    }

    // ================================================================
    // NỘI BỘ
    // ================================================================

    private String symbolOf(SeatStatus status) {
        return switch (status) {
            case AVAILABLE -> "[ ]";
            case BOOKED -> "[X]";
            case LOCKED -> "[L]";
        };
    }
}
