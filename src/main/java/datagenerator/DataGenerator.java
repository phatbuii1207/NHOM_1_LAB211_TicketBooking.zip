package datagenerator;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

// ============================================================
//  DataGenerator.java
//  Mục đích : Sinh dữ liệu mẫu cho hệ thống đặt vé bóng đá
//             và ghi ra các file CSV.
//  Yêu cầu  : Tổng số dòng dữ liệu (không tính header) >= 10.000
//  Tác giả  : LAB211 – nhóm bài tập lớn
// ============================================================

// ============================================================
// PHẦN 1 – CÁC ENUM (kiểu liệt kê)
// ============================================================

/** Trạng thái của một ghế trong sân */
enum SeatStatus {
    AVAILABLE, // Còn trống
    LOCKED, // Đang bị khoá (người dùng đang chọn)
    BOOKED // Đã được đặt
}

/** Loại khu vực trong sân */
enum SectionType {
    VIP, // Khu VIP
    NORMAL, // Khu thường
    STANDING // Khu đứng
}

// ============================================================
// PHẦN 2 – LỚP CƠ SỞ (Base class)
// ============================================================

/**
 * BaseEntity – lớp cha chung cho tất cả các đối tượng dữ liệu.
 *
 * Tại sao dùng abstract?
 * Vì mỗi lớp con (Seat, Section, …) có cách ghi CSV khác nhau,
 * nhưng chúng ta muốn ép buộc tất cả đều phải cài đặt toCsvLine().
 */
abstract class BaseEntity {

    protected String id;

    // Mỗi lớp con PHẢI cài đặt phương thức này để trả về 1 dòng CSV
    public abstract String toCsvLine();

    // Mỗi lớp con PHẢI cài đặt phương thức này để trả về dòng header CSV
    public abstract String getCsvHeader();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

// ============================================================
// PHẦN 3 – CÁC LỚP MODEL
// ============================================================

/**
 * Stadium – Sân vận động
 * CSV: id, name, location, capacity
 */
class Stadium extends BaseEntity {

    private String name; // Tên sân
    private String location; // Địa điểm
    private int capacity; // Sức chứa tối đa

    public Stadium(String id, String name, String location, int capacity) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.capacity = capacity;
    }

    @Override
    public String getCsvHeader() {
        return "id,name,location,capacity";
    }

    @Override
    public String toCsvLine() {
        return String.format("%s,%s,%s,%d", id, name, location, capacity);
    }
}

/**
 * Section – Khu vực (block) bên trong một sân vận động
 * CSV: id, stadiumId, name, type, price, totalRows, seatsPerRow
 */
class Section extends BaseEntity {

    private String stadiumId; // Thuộc sân nào
    private String name; // Tên khu (VIP A, Standard B, …)
    private SectionType type; // Loại khu
    private double price; // Giá vé (VND)
    private int totalRows; // Tổng số hàng ghế
    private int seatsPerRow; // Số ghế mỗi hàng

    public Section(String id, String stadiumId, String name,
            SectionType type, double price,
            int totalRows, int seatsPerRow) {
        this.id = id;
        this.stadiumId = stadiumId;
        this.name = name;
        this.type = type;
        this.price = price;
        this.totalRows = totalRows;
        this.seatsPerRow = seatsPerRow;
    }

    // Getter dùng trong DataGenerator.generateSeats()
    public int getTotalRows() {
        return totalRows;
    }

    public int getSeatsPerRow() {
        return seatsPerRow;
    }

    @Override
    public String getCsvHeader() {
        return "id,stadiumId,name,type,price,totalRows,seatsPerRow";
    }

    @Override
    public String toCsvLine() {
        return String.format("%s,%s,%s,%s,%.0f,%d,%d",
                id, stadiumId, name, type.name(), price, totalRows, seatsPerRow);
    }
}

/**
 * Seat – Một ghế ngồi cụ thể trong một khu vực
 * CSV: id, sectionId, rowNumber, seatNumber, status, version
 *
 * version: dùng cho Optimistic Locking (tránh 2 người đặt cùng 1 ghế)
 */
class Seat extends BaseEntity {

    private String sectionId; // Thuộc khu nào
    private int rowNumber; // Số hàng
    private int seatNumber; // Số ghế trong hàng
    private SeatStatus status; // Trạng thái ghế
    private int version; // Phiên bản (dùng cho optimistic lock)

    public Seat(String id, String sectionId,
            int rowNumber, int seatNumber) {
        this.id = id;
        this.sectionId = sectionId;
        this.rowNumber = rowNumber;
        this.seatNumber = seatNumber;
        this.status = SeatStatus.AVAILABLE; // Mặc định: còn trống
        this.version = 0;
    }

    @Override
    public String getCsvHeader() {
        return "id,sectionId,rowNumber,seatNumber,status,version";
    }

    @Override
    public String toCsvLine() {
        return String.format("%s,%s,%d,%d,%s,%d",
                id, sectionId, rowNumber, seatNumber, status.name(), version);
    }
}

/**
 * Match – Một trận đấu bóng đá
 * CSV: id, homeTeam, awayTeam, matchDate, stadiumId, kickoffTime
 */
class Match extends BaseEntity {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String homeTeam; // Đội nhà
    private String awayTeam; // Đội khách
    private LocalDateTime matchDate; // Ngày giờ thi đấu
    private String stadiumId; // Thi đấu ở sân nào
    private String kickoffTime; // Giờ bắt đầu (HH:mm:ss)

    public Match(String id, String homeTeam, String awayTeam,
            LocalDateTime matchDate, String stadiumId) {
        this.id = id;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.matchDate = matchDate;
        this.stadiumId = stadiumId;
        this.kickoffTime = matchDate.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    @Override
    public String getCsvHeader() {
        return "id,homeTeam,awayTeam,matchDate,stadiumId,kickoffTime";
    }

    @Override
    public String toCsvLine() {
        return String.format("%s,%s,%s,%s,%s,%s",
                id, homeTeam, awayTeam,
                matchDate.format(FORMATTER),
                stadiumId, kickoffTime);
    }
}

/**
 * Fan – Người dùng (khán giả) đăng ký trên hệ thống
 * CSV: id, name, email, phone, passwordHash
 *
 * Lưu ý: KHÔNG lưu mật khẩu thô. Dùng BCrypt hash trong thực tế.
 * Ở đây dùng giá trị giả lập cho mục đích test.
 */
class Fan extends BaseEntity {

    private String name;
    private String email;
    private String phone;
    private String passwordHash; // Hash của mật khẩu (không phải plain text)

    public Fan(String id, String name, String email,
            String phone, String passwordHash) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
    }

    @Override
    public String getCsvHeader() {
        return "id,name,email,phone,passwordHash";
    }

    @Override
    public String toCsvLine() {
        return String.format("%s,%s,%s,%s,%s",
                id, name, email, phone, passwordHash);
    }
}

/**
 * Ticket – Vé đã mua (sẽ được sinh khi có giao dịch thực tế)
 * CSV: id, matchId, seatId, fanId, purchasedAt, price
 */
class Ticket extends BaseEntity {

    @Override
    public String getCsvHeader() {
        return "id,matchId,seatId,fanId,purchasedAt,price";
    }

    @Override
    public String toCsvLine() {
        return ""; // Chưa có dữ liệu – file sẽ chỉ có header
    }
}

/**
 * Transaction – Giao dịch thanh toán
 * CSV: id, ticketId, fanId, amount, status, createdAt
 */
class Transaction extends BaseEntity {

    @Override
    public String getCsvHeader() {
        return "id,ticketId,fanId,amount,status,createdAt";
    }

    @Override
    public String toCsvLine() {
        return ""; // Chưa có dữ liệu – file sẽ chỉ có header
    }
}

// ============================================================
// PHẦN 4 – LỚP CHÍNH: DataGenerator
// ============================================================

/**
 * DataGenerator – Lớp chính điều phối toàn bộ quá trình sinh data.
 *
 * Luồng hoạt động:
 * main()
 * → generateStadiums() → ghi stadiums.csv
 * → generateSections() → ghi sections.csv
 * → generateSeats() → ghi seats.csv (>=10.000 dòng)
 * → generateMatches() → ghi matches.csv
 * → generateFans() → ghi fans.csv
 * → writeHeaderOnly() → ghi tickets.csv (chỉ header)
 * → writeHeaderOnly() → ghi transactions.csv (chỉ header)
 */
public class DataGenerator {

    // ---- Cấu hình chung ----------------------------------------
    private static final String OUTPUT_DIR = "data/";
    private static final int TARGET_SEATS = 10_000; // Mục tiêu tối thiểu
    private static final int TOTAL_FANS = 500;

    // Formatter dùng để parse ngày giờ từ chuỗi String
    private static final DateTimeFormatter DATE_PARSER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ---- Điểm vào chương trình ---------------------------------
    public static void main(String[] args) {

        // Tạo thư mục output nếu chưa có
        new File(OUTPUT_DIR).mkdirs();

        DataGenerator gen = new DataGenerator();

        System.out.println("=== START GENERATING DATA ===\n");

        // 1. San van dong (Stadiums)
        List<Stadium> stadiums = gen.generateStadiums();
        gen.writeCsv(OUTPUT_DIR + "stadiums.csv", stadiums, new Stadium("", "", "", 0));
        System.out.printf("[OK] Stadiums     : %d rows%n", stadiums.size());

        // 2. Khu vuc (Sections) - sinh cho tat ca cac san
        List<Section> sections = gen.generateSections(stadiums);
        gen.writeCsv(OUTPUT_DIR + "sections.csv", sections, new Section("", "", "", SectionType.NORMAL, 0, 0, 0));
        System.out.printf("[OK] Sections     : %d rows%n", sections.size());

        // 3. Ghe ngoi (Seats) - dam bao >= TARGET_SEATS rows
        // Chien luoc: sinh day du tung section, khong cat giua chung.
        // Neu tong ghe >= TARGET_SEATS thi dung them section moi.
        List<Seat> seats = gen.generateSeats(sections);
        gen.writeCsv(OUTPUT_DIR + "seats.csv", seats, new Seat("", "", 0, 0));
        System.out.printf("[OK] Seats        : %d rows (required >= %d)%n",
                seats.size(), TARGET_SEATS);

        // 4. Tran dau (Matches)
        List<Match> matches = gen.generateMatches(stadiums);
        gen.writeCsv(OUTPUT_DIR + "matches.csv", matches,
                new Match("", "", "", LocalDateTime.now(), ""));
        System.out.printf("[OK] Matches      : %d rows%n", matches.size());

        // 5. Nguoi dung (Fans)
        List<Fan> fans = gen.generateFans();
        gen.writeCsv(OUTPUT_DIR + "fans.csv", fans, new Fan("", "", "", "", ""));
        System.out.printf("[OK] Fans         : %d rows%n", fans.size());

        // 6. Tickets & Transactions: chỉ ghi header, chưa có dữ liệu
        gen.writeHeaderOnly(OUTPUT_DIR + "tickets.csv", new Ticket());
        gen.writeHeaderOnly(OUTPUT_DIR + "transactions.csv", new Transaction());
        System.out.println("[OK] tickets.csv and transactions.csv (header only)");

        // Tổng kết
        int totalRows = stadiums.size() + sections.size() + seats.size()
                + matches.size() + fans.size();
        System.out.printf("%n=== DONE | Total data rows: %d | Output folder: %s ===%n",
                totalRows, OUTPUT_DIR);
    }

    // ============================================================
    // CÁC PHƯƠNG THỨC SINH DỮ LIỆU
    // ============================================================

    /**
     * Sinh danh sách sân vận động.
     * Mỗi Stadium cần có id duy nhất.
     */
    private List<Stadium> generateStadiums() {
        List<Stadium> list = new ArrayList<>();
        list.add(new Stadium("ST001", "My Dinh Stadium", "Ha Noi", 40000));
        list.add(new Stadium("ST002", "Hang Day Stadium", "Ha Noi", 22500));
        list.add(new Stadium("ST003", "Thong Nhat Stadium", "TP HCM", 25000));
        return list;
    }

    /**
     * Sinh các khu vực (sections) cho TẤT CẢ các sân.
     *
     * Khác bản cũ: bản cũ chỉ sinh cho stadiums.get(0) nên ST002
     * không có section nào → dữ liệu không nhất quán.
     *
     * Cách đặt số hàng/ghế:
     * - VIP : 10 hàng × 50 ghế = 500 ghế/section
     * - NORMAL : 30 hàng × 100 ghế = 3.000 ghế/section
     * - STANDING : 5 hàng × 100 ghế = 500 ghế/section
     *
     * Tổng mỗi sân ≈ 9.000 ghế → 3 sân ≈ 27.000 ghế,
     * đảm bảo vượt mục tiêu 10.000.
     */
    private List<Section> generateSections(List<Stadium> stadiums) {
        List<Section> list = new ArrayList<>();

        for (Stadium stadium : stadiums) {
            String sid = stadium.getId();

            // Các section VIP
            list.add(new Section(sid + "_S01", sid, "VIP A", SectionType.VIP, 1_200_000, 10, 50));
            list.add(new Section(sid + "_S02", sid, "VIP B", SectionType.VIP, 1_200_000, 10, 50));

            // Các section thường
            list.add(new Section(sid + "_S03", sid, "Standard A", SectionType.NORMAL, 700_000, 30, 100));
            list.add(new Section(sid + "_S04", sid, "Standard B", SectionType.NORMAL, 500_000, 30, 100));
            list.add(new Section(sid + "_S05", sid, "Standard C", SectionType.NORMAL, 350_000, 30, 100));
            list.add(new Section(sid + "_S06", sid, "Economy A", SectionType.NORMAL, 250_000, 20, 100));
            list.add(new Section(sid + "_S07", sid, "Economy B", SectionType.NORMAL, 250_000, 20, 100));

            // Khu đứng
            list.add(new Section(sid + "_S08", sid, "Standing", SectionType.STANDING, 150_000, 5, 100));
        }

        return list;
    }

    /**
     * Sinh ghế ngồi cho tất cả sections.
     *
     * Thuật toán:
     * for mỗi section:
     * for row = 1 → totalRows:
     * for seatNum = 1 → seatsPerRow:
     * tạo 1 Seat mới
     * // Không cắt giữa section – luôn hoàn thành đủ 1 section
     * if tổng ghế >= TARGET_SEATS → dừng thêm section mới
     *
     * → Kết quả: số ghế luôn là bội số của seatsPerRow,
     * không bao giờ bị cắt dở giữa 1 hàng hay 1 section.
     */
    private List<Seat> generateSeats(List<Section> sections) {
        List<Seat> seats = new ArrayList<>();
        int counter = 1; // Bộ đếm để tạo id duy nhất: SEAT000001, SEAT000002, …

        for (Section section : sections) {

            // Sinh toàn bộ ghế của section này trước
            for (int row = 1; row <= section.getTotalRows(); row++) {
                for (int seatNum = 1; seatNum <= section.getSeatsPerRow(); seatNum++) {

                    String seatId = String.format("SEAT%06d", counter++);
                    seats.add(new Seat(seatId, section.getId(), row, seatNum));
                }
            }

            // Sau khi hoàn thành 1 section, kiểm tra xem đã đủ chưa
            if (seats.size() >= TARGET_SEATS) {
                System.out.printf("  -> Reached %d seats, stopped after completing section %s%n",
                        seats.size(), section.getId());
                break; // Dừng – không thêm section tiếp theo
            }
        }

        return seats;
    }

    /**
     * Sinh danh sách các trận đấu.
     * Mỗi trận được gán vào một sân cụ thể (xoay vòng giữa các sân).
     */
    private List<Match> generateMatches(List<Stadium> stadiums) {
        // Dữ liệu trận: {đội nhà, đội khách, ngày giờ}
        String[][] matchData = {
                { "Viet Nam", "Thai Lan", "2026-06-05 19:30:00" },
                { "Viet Nam", "Indonesia", "2026-06-12 19:00:00" },
                { "Ha Noi FC", "Hai Phong FC", "2026-06-20 18:00:00" },
                { "CLB CAHN", "Nam Dinh FC", "2026-06-27 18:30:00" },
                { "Viet Nam", "Malaysia", "2026-07-03 19:00:00" },
                { "Binh Dinh FC", "Thanh Hoa FC", "2026-07-10 17:30:00" },
                { "Viet Nam", "Singapore", "2026-07-17 19:00:00" },
                { "Ha Noi FC", "Cong An HN", "2026-07-24 18:00:00" },
                { "TP HCM FC", "Da Nang FC", "2026-07-31 17:30:00" },
                { "Viet Nam", "Philippines", "2026-08-07 19:00:00" },
        };

        List<Match> list = new ArrayList<>();

        for (int i = 0; i < matchData.length; i++) {
            // Xoay vòng sân: trận 0 → ST001, trận 1 → ST002, trận 2 → ST003, …
            String stadiumId = stadiums.get(i % stadiums.size()).getId();

            list.add(new Match(
                    String.format("MATCH%03d", i + 1),
                    matchData[i][0],
                    matchData[i][1],
                    LocalDateTime.parse(matchData[i][2], DATE_PARSER),
                    stadiumId));
        }

        return list;
    }

    /**
     * Sinh danh sách fan (người dùng).
     *
     * Khác bản cũ:
     * - Tên được tổ hợp ngẫu nhiên hơn (3 mảng khác nhau, không cùng index)
     * - passwordHash thể hiện rõ đây là hash, không phải plain text
     *
     * Trong thực tế: dùng BCrypt.hashpw("password123", BCrypt.gensalt())
     */
    private List<Fan> generateFans() {
        // Các mảng từ để ghép tên ngẫu nhiên
        String[] ho = { "Nguyen", "Tran", "Le", "Pham", "Hoang",
                "Vu", "Dang", "Bui", "Do", "Ho" };
        String[] tenLot = { "Van", "Thi", "Huu", "Duc", "Minh",
                "Thanh", "Hai", "Quang", "Tuan", "Hung" };
        String[] ten = { "Hong", "Xuan", "Ngoc", "Anh", "Thanh",
                "Cong", "Hoai", "Duy", "Khanh", "Bao" };

        // Mật khẩu giả lập: bcrypt hash của "password123"
        // (Trong production: không hardcode như này)
        String fakeHash = "$2a$10$FAKEHASHFORLAB211PURPOSEONLYXXXXXX";

        List<Fan> list = new ArrayList<>();

        for (int i = 1; i <= TOTAL_FANS; i++) {
            // Dùng 3 modulo KHÁC NHAU để tên đa dạng hơn
            String name = ho[i % ho.length]
                    + " " + tenLot[(i * 3) % tenLot.length]
                    + " " + ten[(i * 7) % ten.length];

            list.add(new Fan(
                    String.format("FAN%04d", i),
                    name,
                    String.format("fan%d@example.com", i),
                    String.format("09%08d", i),
                    fakeHash));
        }

        return list;
    }

    // ============================================================
    // CÁC PHƯƠNG THỨC GHI FILE CSV
    // ============================================================

    /**
     * Ghi danh sách đối tượng ra file CSV.
     *
     * @param filePath Đường dẫn file output
     * @param data     Danh sách dữ liệu cần ghi
     * @param sample   Một đối tượng mẫu (chỉ để lấy header – không ghi ra file)
     *
     *                 Tại sao cần tham số sample?
     *                 Vì data có thể là list rỗng, nên không thể gọi data.get(0).
     *                 Truyền sample đảm bảo header luôn được ghi dù list rỗng.
     */
    private <T extends BaseEntity> void writeCsv(
            String filePath, List<T> data, BaseEntity sample) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {

            // Luôn ghi header trước
            writer.write(sample.getCsvHeader());
            writer.newLine();

            // Ghi từng dòng dữ liệu
            for (T entity : data) {
                writer.write(entity.toCsvLine());
                writer.newLine();
            }

        } catch (IOException e) {
            System.err.println("Lỗi ghi file " + filePath + ": " + e.getMessage());
        }
    }

    /**
     * Ghi file CSV chỉ có dòng header (không có dữ liệu).
     * Dùng cho tickets.csv và transactions.csv lúc khởi tạo hệ thống.
     *
     * @param filePath Đường dẫn file
     * @param sample   Đối tượng mẫu để lấy header
     */
    private void writeHeaderOnly(String filePath, BaseEntity sample) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(sample.getCsvHeader());
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Lỗi ghi file " + filePath + ": " + e.getMessage());
        }
    }
}