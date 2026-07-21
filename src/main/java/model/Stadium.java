package model;

/**
 * Stadium – San van dong.
 *
 * CSV header: id,name,location,capacity
 * Vi du: ST001,My Dinh Stadium,Ha Noi,40000
 */
public class Stadium extends BaseEntity {

    private String name;
    private String location;
    private int    capacity;

    public Stadium() {}

    public Stadium(String id, String name, String location, int capacity) {
        super(id);
        this.name     = name;
        this.location = location;
        this.capacity = capacity;
    }

    // ================================================================
    // CSV CONTRACT
    // ================================================================

    @Override
    public String toCsvHeader() { return "id,name,location,capacity"; }

    @Override
    public String toCsvLine() {
        return String.format("%s,%s,%s,%d", id, name, location, capacity);
    }

    @Override
    public void fromCsvLine(String csvLine) {
        if (csvLine == null || csvLine.isBlank())
            throw new IllegalArgumentException("Stadium CSV line is null/blank");
        String[] p = csvLine.split(",");
        if (p.length < 4)
            throw new IllegalArgumentException("Stadium CSV needs 4 fields, got " + p.length);
        this.id       = p[0].trim();
        this.name     = p[1].trim();
        this.location = p[2].trim();
        this.capacity = Integer.parseInt(p[3].trim());
    }

    // Getters & Setters
    public String getName()     { return name; }
    public String getLocation() { return location; }
    public int    getCapacity() { return capacity; }

    public void setName(String name)         { this.name = name; }
    public void setLocation(String location) { this.location = location; }
    public void setCapacity(int capacity)    { this.capacity = capacity; }

    @Override
    public String toString() {
        return String.format("Stadium{id=%s, name=%s, location=%s, capacity=%,d}",
                id, name, location, capacity);
    }
}
