package model;

public class Seat extends BaseEntity {
    private String sectionId;
    private int rowNumber;
    private int seatNumber;
    private SeatStatus status;
    private int version;

    public Seat() {
        super();
        this.status = SeatStatus.AVAILABLE;
        this.version = 0;
    }

    // Getters
    public String getSectionId() {
        return sectionId;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public int getVersion() {
        return version;
    }

    // Setters
    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    // Business methods
    public boolean lock() {
        if (status == SeatStatus.AVAILABLE) {
            this.status = SeatStatus.LOCKED;
            return true;
        }
        return false;
    }

    public boolean book() {
        if (status == SeatStatus.AVAILABLE) {
            this.status = SeatStatus.BOOKED;
            this.version++;
            return true;
        }
        return false;
    }

    public void unlock() {
        if (status == SeatStatus.LOCKED) {
            this.status = SeatStatus.AVAILABLE;
        }
    }

    @Override
    public String toCsvLine() {
        return String.format("%s,%s,%d,%d,%s,%d",
                id, sectionId, rowNumber, seatNumber, status.name(), version);
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = line.split(",");
        this.id = parts[0];
        this.sectionId = parts[1];
        this.rowNumber = Integer.parseInt(parts[2]);
        this.seatNumber = Integer.parseInt(parts[3]);
        this.status = SeatStatus.valueOf(parts[4]);
        this.version = Integer.parseInt(parts[5]);
    }
}