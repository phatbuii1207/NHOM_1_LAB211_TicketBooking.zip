package model;

public abstract class BaseEntity {

    protected String id;

    public BaseEntity() {
    }

    public BaseEntity(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Trả về dòng HEADER của file CSV.
     * Ví dụ Fan trả về: "id,name,email,phone,passwordHash"
     * Ví dụ Seat trả về: "id,sectionId,rowNumber,seatNumber,status,version"
     * Dùng bởi CsvRepository khi tạo file mới hoặc ghi đè file.
     */
    public abstract String toCsvHeader();

    /** Chuyển object thành 1 dòng CSV để ghi vào file */
    public abstract String toCsvLine();

    /** Đọc 1 dòng CSV và gán giá trị vào các field của object */
    public abstract void fromCsvLine(String csvLine);
}