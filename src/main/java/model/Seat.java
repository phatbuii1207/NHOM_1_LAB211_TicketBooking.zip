package model;

// CSV format: id,sectionId,rowNumber,seatNumber,status,version
public class Seat extends BaseEntity {

    private String sectionId;
    private int rowNumber;
    private int seatNumber;
    private SeatStatus status;
    private int version; // dùng cho Optimistic Locking

    public Seat() {
        this.status = SeatStatus.AVAILABLE;
        this.version = 0;
    }

    public Seat(String id, String sectionId, int rowNumber, int seatNumber, SeatStatus status, int version) {
        super(id);
        this.sectionId = sectionId;
        this.rowNumber = rowNumber;
        this.seatNumber = seatNumber;
        this.status = status;
        this.version = version;
    }

    /**
     * Khoá ghế (AVAILABLE → LOCKED). Dùng khi người dùng đang chọn chỗ, chưa thanh
     * toán.
     */
    public boolean lock() {
        if (status != SeatStatus.AVAILABLE)
            return false;
        this.status = SeatStatus.LOCKED;
        this.version++;
        return true;
    }

    /**
     * Giải phóng ghế (LOCKED → AVAILABLE). Dùng khi timeout hoặc người dùng huỷ.
     */
    public boolean unlock() {
        if (status != SeatStatus.LOCKED)
            return false;
        this.status = SeatStatus.AVAILABLE;
        this.version++;
        return true;
    }

    /**
     * Đặt vé: thành công khi ghế đang AVAILABLE hoặc LOCKED.
     * Luồng bình thường: AVAILABLE → LOCKED → BOOKED.
     * Cũng cho phép AVAILABLE → BOOKED trực tiếp.
     */
    public boolean book() {
        if (status == SeatStatus.BOOKED)
            return false;
        this.status = SeatStatus.BOOKED;
        this.version++;
        return true;
    }

    @Override
    public String toCsvHeader() {
        return "id,sectionId,rowNumber,seatNumber,status,version";
    }

    @Override
    public String toCsvLine() {
        return String.format("%s,%s,%d,%d,%s,%d", id, sectionId, rowNumber, seatNumber, status.name(), version);
    }

    @Override
    public void fromCsvLine(String csvLine) {
        if (csvLine == null || csvLine.isBlank()) {
            throw new IllegalArgumentException("Seat: CSV line rỗng hoặc null");
        }
        String[] p = csvLine.split(",");
        if (p.length < 6) {
            throw new IllegalArgumentException(
                    "Seat CSV phải có đúng 6 trường (id,sectionId,rowNumber,seatNumber,status,version). " +
                            "Thực tế: " + p.length + " trường | Dòng: [" + csvLine + "]");
        }
        try {
            this.id = p[0].trim();
            this.sectionId = p[1].trim();
            this.rowNumber = Integer.parseInt(p[2].trim());
            this.seatNumber = Integer.parseInt(p[3].trim());
            this.status = SeatStatus.valueOf(p[4].trim());
            this.version = Integer.parseInt(p[5].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Seat CSV: rowNumber/seatNumber/version không phải số hợp lệ | Dòng: [" + csvLine + "]", e);
        }
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}