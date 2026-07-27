package model;

public class SimulationResult extends BaseEntity {
    private String concurrencyMode;
    private int threadCount;
    private int totalSeats;
    private int successfulBookings;
    private int failedBookings;
    private int doubleBookingCount;
    private long durationMs;
    private double throughput;
    private double doubleBookingRate;
    private String timestamp;

    public SimulationResult() {
        super();
    }

    public SimulationResult(String id, String concurrencyMode, int threadCount, int totalSeats,
            int successfulBookings, int failedBookings, int doubleBookingCount,
            long durationMs, double throughput, double doubleBookingRate, String timestamp) {
        super(id);
        this.concurrencyMode = concurrencyMode;
        this.threadCount = threadCount;
        this.totalSeats = totalSeats;
        this.successfulBookings = successfulBookings;
        this.failedBookings = failedBookings;
        this.doubleBookingCount = doubleBookingCount;
        this.durationMs = durationMs;
        this.throughput = throughput;
        this.doubleBookingRate = doubleBookingRate;
        this.timestamp = timestamp;
    }

    @Override
    public String toCsvHeader() {
        return "id,concurrencyMode,threadCount,totalSeats,successfulBookings,failedBookings,doubleBookingCount,durationMs,throughput,doubleBookingRate,timestamp";
    }

    @Override
    public String toCsvLine() {
        return String.format("%s,%s,%d,%d,%d,%d,%d,%d,%.2f,%.2f,%s",
                id, concurrencyMode, threadCount, totalSeats, successfulBookings, failedBookings, doubleBookingCount,
                durationMs, throughput, doubleBookingRate, timestamp);
    }

    @Override
    public void fromCsvLine(String csvLine) {
        if (csvLine == null || csvLine.isBlank()) {
            throw new IllegalArgumentException("SimulationResult: CSV line rong hoac null");
        }
        String[] p = csvLine.split(",");
        if (p.length < 11) {
            throw new IllegalArgumentException(
                    "SimulationResult CSV phai co 11 truong. Thuc te: " + p.length + " | Dong: [" + csvLine + "]");
        }
        try {
            this.id = p[0].trim();
            this.concurrencyMode = p[1].trim();
            this.threadCount = Integer.parseInt(p[2].trim());
            this.totalSeats = Integer.parseInt(p[3].trim());
            this.successfulBookings = Integer.parseInt(p[4].trim());
            this.failedBookings = Integer.parseInt(p[5].trim());
            this.doubleBookingCount = Integer.parseInt(p[6].trim());
            this.durationMs = Long.parseLong(p[7].trim());
            this.throughput = Double.parseDouble(p[8].trim());
            this.doubleBookingRate = Double.parseDouble(p[9].trim());
            this.timestamp = p[10].trim();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("SimulationResult CSV: loi dinh dang so | Dong: [" + csvLine + "]", e);
        }
    }

    // Getters and Setters
    public String getConcurrencyMode() {
        return concurrencyMode;
    }

    public void setConcurrencyMode(String concurrencyMode) {
        this.concurrencyMode = concurrencyMode;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getSuccessfulBookings() {
        return successfulBookings;
    }

    public void setSuccessfulBookings(int successfulBookings) {
        this.successfulBookings = successfulBookings;
    }

    public int getFailedBookings() {
        return failedBookings;
    }

    public void setFailedBookings(int failedBookings) {
        this.failedBookings = failedBookings;
    }

    public int getDoubleBookingCount() {
        return doubleBookingCount;
    }

    public void setDoubleBookingCount(int doubleBookingCount) {
        this.doubleBookingCount = doubleBookingCount;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public double getThroughput() {
        return throughput;
    }

    public void setThroughput(double throughput) {
        this.throughput = throughput;
    }

    public double getDoubleBookingRate() {
        return doubleBookingRate;
    }

    public void setDoubleBookingRate(double doubleBookingRate) {
        this.doubleBookingRate = doubleBookingRate;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
