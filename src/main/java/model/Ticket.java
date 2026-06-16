package model;

/**
 * Ticket – Vé xem trận đấu.
 *
 * CSV header: id,matchId,seatId,fanId,purchasedAt,price
 * Ví dụ dòng: TKT00001,MATCH001,SEAT000001,FAN0001,2026-06-05 10:30:00,250000.0
 */
public class Ticket extends BaseEntity {

    private String matchId;       // Trận đấu (VD: MATCH001)
    private String seatId;        // Ghế ngồi (VD: SEAT000001)
    private String fanId;         // Khán giả mua vé (VD: FAN0001)
    private String purchasedAt;   // Thời điểm mua (VD: 2026-06-05 10:30:00)
    private double price;         // Giá vé (VD: 250000.0)

    // Constructor rỗng – JUnit và CsvRepository dùng Factory: Ticket::new
    public Ticket() {}

    // Constructor đầy đủ – dùng khi tạo vé mới trong code
    public Ticket(String id, String matchId, String seatId,
                  String fanId, String purchasedAt, double price) {
        super(id);
        this.matchId    = matchId;
        this.seatId     = seatId;
        this.fanId      = fanId;
        this.purchasedAt = purchasedAt;
        this.price      = price;
    }

    // ================================================================
    // CSV CONTRACT (bắt buộc implement vì BaseEntity yêu cầu)
    // ================================================================

    @Override
    public String toCsvHeader() {
        return "id,matchId,seatId,fanId,purchasedAt,price";
    }

    @Override
    public String toCsvLine() {
        return String.format("%s,%s,%s,%s,%s,%.1f",
                id, matchId, seatId, fanId, purchasedAt, price);
    }

    @Override
    public void fromCsvLine(String csvLine) {
        if (csvLine == null || csvLine.isBlank()) {
            throw new IllegalArgumentException("CSV line must not be null or empty");
        }
        String[] parts = csvLine.split(",");
        if (parts.length < 6) {
            throw new IllegalArgumentException(
                "Ticket CSV needs 6 fields, got " + parts.length + ": [" + csvLine + "]");
        }
        try {
            this.id          = parts[0].trim();
            this.matchId     = parts[1].trim();
            this.seatId      = parts[2].trim();
            this.fanId       = parts[3].trim();
            this.purchasedAt = parts[4].trim();
            this.price       = Double.parseDouble(parts[5].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid price value: " + parts[5].trim(), e);
        }
    }

    // ================================================================
    // GETTERS & SETTERS
    // ================================================================

    public String getMatchId()     { return matchId; }
    public String getSeatId()      { return seatId; }
    public String getFanId()       { return fanId; }
    public String getPurchasedAt() { return purchasedAt; }
    public double getPrice()       { return price; }

    public void setMatchId(String matchId)         { this.matchId = matchId; }
    public void setSeatId(String seatId)           { this.seatId = seatId; }
    public void setFanId(String fanId)             { this.fanId = fanId; }
    public void setPurchasedAt(String purchasedAt) { this.purchasedAt = purchasedAt; }
    public void setPrice(double price)             { this.price = price; }

    @Override
    public String toString() {
        return String.format("Ticket{id=%s, match=%s, seat=%s, fan=%s, price=%.0f}",
                id, matchId, seatId, fanId, price);
    }
}
