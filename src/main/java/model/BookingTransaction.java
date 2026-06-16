package model;

/**
 * BookingTransaction – Giao dịch thanh toán vé.
 *
 * CSV header: id,ticketId,fanId,amount,status,createdAt
 * Ví dụ dòng: TXN00001,TKT00001,FAN0001,250000.0,COMPLETED,2026-06-05 10:31:00
 *
 * Status có thể là: PENDING, COMPLETED, CANCELLED, FAILED
 */
public class BookingTransaction extends BaseEntity {

    private String ticketId;   // Vé liên quan (VD: TKT00001)
    private String fanId;      // Khán giả (VD: FAN0001)
    private double amount;     // Số tiền (VD: 250000.0)
    private String status;     // Trạng thái: PENDING/COMPLETED/CANCELLED/FAILED
    private String createdAt;  // Thời điểm tạo (VD: 2026-06-05 10:31:00)

    // Constructor rỗng – dùng bởi CsvRepository factory: BookingTransaction::new
    public BookingTransaction() {}

    // Constructor đầy đủ
    public BookingTransaction(String id, String ticketId, String fanId,
                              double amount, String status, String createdAt) {
        super(id);
        this.ticketId  = ticketId;
        this.fanId     = fanId;
        this.amount    = amount;
        this.status    = status;
        this.createdAt = createdAt;
    }

    // ================================================================
    // CSV CONTRACT
    // ================================================================

    @Override
    public String toCsvHeader() {
        return "id,ticketId,fanId,amount,status,createdAt";
    }

    @Override
    public String toCsvLine() {
        return String.format("%s,%s,%s,%.1f,%s,%s",
                id, ticketId, fanId, amount, status, createdAt);
    }

    @Override
    public void fromCsvLine(String csvLine) {
        if (csvLine == null || csvLine.isBlank()) {
            throw new IllegalArgumentException("CSV line must not be null or empty");
        }
        String[] parts = csvLine.split(",");
        if (parts.length < 6) {
            throw new IllegalArgumentException(
                "BookingTransaction CSV needs 6 fields, got " + parts.length + ": [" + csvLine + "]");
        }
        try {
            this.id        = parts[0].trim();
            this.ticketId  = parts[1].trim();
            this.fanId     = parts[2].trim();
            this.amount    = Double.parseDouble(parts[3].trim());
            this.status    = parts[4].trim();
            this.createdAt = parts[5].trim();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount value: " + parts[3].trim(), e);
        }
    }

    // ================================================================
    // GETTERS & SETTERS
    // ================================================================

    public String getTicketId()  { return ticketId; }
    public String getFanId()     { return fanId; }
    public double getAmount()    { return amount; }
    public String getStatus()    { return status; }
    public String getCreatedAt() { return createdAt; }

    public void setTicketId(String ticketId)   { this.ticketId = ticketId; }
    public void setFanId(String fanId)         { this.fanId = fanId; }
    public void setAmount(double amount)       { this.amount = amount; }
    public void setStatus(String status)       { this.status = status; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format("Transaction{id=%s, ticket=%s, amount=%.0f, status=%s}",
                id, ticketId, amount, status);
    }
}
