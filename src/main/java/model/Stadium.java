package model;

public class Stadium extends BaseEntity {
    private String stadiumName;
    private String location;
    private int capacity;

    public Stadium(String id, String stadiumName, String location, int capacity) {
        super(id);
        this.stadiumName = stadiumName;
        this.location = location;
        this.capacity = capacity;
    }

    public Stadium() {
    }

    public String getStadiumName() {
        return stadiumName;
    }

    public void setStadiumName(String stadiumName) {
        this.stadiumName = stadiumName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public String toString() {
        return String.format("Stadium{id=%s, stadiumName=%s, location=%s, capacity=%d}",
                id, stadiumName, location, capacity);
    }

    @Override
    public String toCsvHeader() {
        return "id, stadiumName, location, capacity";
    }

    @Override
    public String toCsvLine() {
        return String.format("%s, %s, %s, %d", id, stadiumName, location, capacity);
    }

    @Override
    public void fromCsvLine(String csvLine) {
        if (csvLine == null || csvLine.isBlank())
            throw new IllegalArgumentException("Stadium CSV line is null/blank");
        String[] p = csvLine.split(",");
        if (p.length < 4)
            throw new IllegalArgumentException("Stadium CSV needs 4 fields, got " + p.length);
        this.id = p[0].trim();
        this.stadiumName = p[1].trim();
        this.location = p[2].trim();
        this.capacity = Integer.parseInt(p[3].trim());
    }
}
