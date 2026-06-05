package repository;

import model.Seat;
import model.SeatStatus;
import java.util.List;

public class SeatRepository extends CsvRepository<Seat> {

    public SeatRepository(String filePath) {
        super(filePath);
    }

    @Override
    protected Seat parseFromCsvLine(String line) {
        Seat seat = new Seat();
        seat.fromCsvLine(line);
        return seat;
    }

    @Override
    protected String parseToCsvLine(Seat seat) {
        return seat.toCsvLine();
    }

    @Override
    protected String getHeader() {
        return "id,sectionId,rowNumber,seatNumber,status,version";
    }

    public List<Seat> findBySectionAndRow(String sectionId, int rowNumber) {
        return findByCondition(seat -> seat.getSectionId().equals(sectionId) && seat.getRowNumber() == rowNumber);
    }

    public List<Seat> findAvailableSeatsBySection(String sectionId) {
        return findByCondition(
                seat -> seat.getSectionId().equals(sectionId) && seat.getStatus() == SeatStatus.AVAILABLE);
    }

    public boolean updateSeatStatus(String seatId, SeatStatus newStatus) {
        Seat seat = findById(seatId);
        if (seat == null)
            return false;

        seat.setStatus(newStatus);
        if (newStatus == SeatStatus.BOOKED) {
            seat.setVersion(seat.getVersion() + 1);
        }
        return update(seat);
    }

    // Optimistic Locking
    public boolean optimisticUpdate(Seat seat, int expectedVersion) {
        Seat current = findById(seat.getId());
        if (current == null)
            return false;

        if (current.getVersion() != expectedVersion) {
            return false;
        }

        seat.setVersion(expectedVersion + 1);
        return update(seat);
    }
}
