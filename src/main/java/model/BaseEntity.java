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

    public abstract String toCsvLine();

    public abstract void fromCsvLine(String csvLine);
}