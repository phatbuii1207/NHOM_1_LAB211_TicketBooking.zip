package model;

// CSV format: id,name,email,phone,password
public class Fan extends BaseEntity {

    private String name;
    private String email;
    private String phone;
    private String password;

    public Fan() {
    }

    public Fan(String id, String name, String email, String phone, String password) {
        super(id);
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    @Override
    public String toCsvLine() {
        return String.format("%s,%s,%s,%s,%s", id, name, email, phone, password);
    }

    @Override
    public void fromCsvLine(String csvLine) {
        if (csvLine == null || csvLine.isBlank()) {
            throw new IllegalArgumentException("Fan: CSV line rỗng hoặc null");
        }
        String[] p = csvLine.split(",");
        if (p.length < 5) {
            throw new IllegalArgumentException(
                    "Fan CSV phải có đúng 5 trường (id,name,email,phone,password). " +
                            "Thực tế: " + p.length + " trường | Dòng: [" + csvLine + "]");
        }
        this.id = p[0].trim();
        this.name = p[1].trim();
        this.email = p[2].trim();
        this.phone = p[3].trim();
        this.password = p[4].trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
