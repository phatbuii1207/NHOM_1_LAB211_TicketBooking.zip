package model;

/**
 * Match – Trận đấu bóng đá.
 *
 * CSV header: id,homeTeam,awayTeam,matchDate,stadiumId,kickoffTime
 * Ví dụ dòng: MATCH001,Viet Nam,Thai Lan,2026-06-05 19:30:00,ST001,19:30:00
 */
public class Match extends BaseEntity {

    private String homeTeam;    // Đội chủ nhà (VD: Viet Nam)
    private String awayTeam;    // Đội khách (VD: Thai Lan)
    private String matchDate;   // Ngày giờ thi đấu (VD: 2026-06-05 19:30:00)
    private String stadiumId;   // Sân vận động (VD: ST001)
    private String kickoffTime; // Giờ kick-off (VD: 19:30:00)

    // Constructor rỗng – dùng bởi CsvRepository factory: Match::new
    public Match() {}

    // Constructor đầy đủ
    public Match(String id, String homeTeam, String awayTeam,
                 String matchDate, String stadiumId, String kickoffTime) {
        super(id);
        this.homeTeam    = homeTeam;
        this.awayTeam    = awayTeam;
        this.matchDate   = matchDate;
        this.stadiumId   = stadiumId;
        this.kickoffTime = kickoffTime;
    }

    // ================================================================
    // CSV CONTRACT
    // ================================================================

    @Override
    public String toCsvHeader() {
        return "id,homeTeam,awayTeam,matchDate,stadiumId,kickoffTime";
    }

    @Override
    public String toCsvLine() {
        return String.format("%s,%s,%s,%s,%s,%s",
                id, homeTeam, awayTeam, matchDate, stadiumId, kickoffTime);
    }

    @Override
    public void fromCsvLine(String csvLine) {
        if (csvLine == null || csvLine.isBlank()) {
            throw new IllegalArgumentException("CSV line must not be null or empty");
        }
        String[] parts = csvLine.split(",");
        if (parts.length < 6) {
            throw new IllegalArgumentException(
                "Match CSV needs 6 fields, got " + parts.length + ": [" + csvLine + "]");
        }
        this.id          = parts[0].trim();
        this.homeTeam    = parts[1].trim();
        this.awayTeam    = parts[2].trim();
        this.matchDate   = parts[3].trim();
        this.stadiumId   = parts[4].trim();
        this.kickoffTime = parts[5].trim();
    }

    // ================================================================
    // GETTERS & SETTERS
    // ================================================================

    public String getHomeTeam()    { return homeTeam; }
    public String getAwayTeam()    { return awayTeam; }
    public String getMatchDate()   { return matchDate; }
    public String getStadiumId()   { return stadiumId; }
    public String getKickoffTime() { return kickoffTime; }

    public void setHomeTeam(String homeTeam)       { this.homeTeam = homeTeam; }
    public void setAwayTeam(String awayTeam)       { this.awayTeam = awayTeam; }
    public void setMatchDate(String matchDate)     { this.matchDate = matchDate; }
    public void setStadiumId(String stadiumId)     { this.stadiumId = stadiumId; }
    public void setKickoffTime(String kickoffTime) { this.kickoffTime = kickoffTime; }

    @Override
    public String toString() {
        return String.format("Match{id=%s, %s vs %s, date=%s, stadium=%s}",
                id, homeTeam, awayTeam, matchDate, stadiumId);
    }
}
