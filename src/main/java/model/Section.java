package model;

/**
 * Section – Khu vực trong sân vận động.
 *
 * CSV header: id,stadiumId,name,type,price,totalRows,seatsPerRow
 * Ví dụ: ST001_S01,ST001,VIP A,VIP,1200000,10,50
 */
public class Section extends BaseEntity {

    private String stadiumId;   // Sân vận động (VD: ST001)
    private String name;        // Tên khu (VD: VIP A)
    private String type;        // Loại: VIP, NORMAL, STANDING
    private double price;       // Giá vé khu này (VD: 1200000)
    private int    totalRows;   // Số hàng (VD: 10)
    private int    seatsPerRow; // Số ghế mỗi hàng (VD: 50)

    public Section() {}

    public Section(String id, String stadiumId, String name, String type,
                   double price, int totalRows, int seatsPerRow) {
        super(id);
        this.stadiumId   = stadiumId;
        this.name        = name;
        this.type        = type;
        this.price       = price;
        this.totalRows   = totalRows;
        this.seatsPerRow = seatsPerRow;
    }

    @Override
    public String toCsvHeader() {
        return "id,stadiumId,name,type,price,totalRows,seatsPerRow";
    }

    @Override
    public String toCsvLine() {
        return String.format("%s,%s,%s,%s,%.0f,%d,%d",
                id, stadiumId, name, type, price, totalRows, seatsPerRow);
    }

    @Override
    public void fromCsvLine(String csvLine) {
        if (csvLine == null || csvLine.isBlank())
            throw new IllegalArgumentException("Section CSV line is null/blank");
        String[] p = csvLine.split(",");
        if (p.length < 7)
            throw new IllegalArgumentException("Section CSV needs 7 fields, got " + p.length);
        this.id          = p[0].trim();
        this.stadiumId   = p[1].trim();
        this.name        = p[2].trim();
        this.type        = p[3].trim();
        this.price       = Double.parseDouble(p[4].trim());
        this.totalRows   = Integer.parseInt(p[5].trim());
        this.seatsPerRow = Integer.parseInt(p[6].trim());
    }

    // Getters
    public String getStadiumId()   { return stadiumId; }
    public String getName()        { return name; }
    public String getType()        { return type; }
    public double getPrice()       { return price; }
    public int    getTotalRows()   { return totalRows; }
    public int    getSeatsPerRow() { return seatsPerRow; }
    public int    getTotalSeats()  { return totalRows * seatsPerRow; }

    @Override
    public String toString() {
        return String.format("Section{id=%s, name=%s, type=%s, price=%.0f, seats=%d}",
                id, name, type, price, getTotalSeats());
    }
}
