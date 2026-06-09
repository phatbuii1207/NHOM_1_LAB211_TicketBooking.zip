package test;

import model.Fan;
import model.Seat;
import model.SeatStatus;
import repository.CsvRepository;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ModelTest – Unit test bằng JUnit 5 cho Fan, Seat, và CsvRepository.
 *
 * ANNOTATION QUAN TRỌNG:
 *   @Test          → Đánh dấu đây là 1 test case
 *   @DisplayName   → Tên hiển thị trong kết quả test
 *   @Nested        → Nhóm các test liên quan vào 1 class con
 *   @BeforeEach    → Chạy trước MỖI test (dùng để khởi tạo dữ liệu)
 *
 * ASSERT QUAN TRỌNG:
 *   assertEquals(expected, actual)      → Kiểm tra bằng nhau
 *   assertTrue(condition)               → Kiểm tra điều kiện đúng
 *   assertFalse(condition)              → Kiểm tra điều kiện sai
 *   assertNotNull(value)                → Kiểm tra khác null
 *   assertThrows(ExType.class, lambda)  → Kiểm tra có ném exception đúng loại
 */
@DisplayName("Model Layer Tests – Fan, Seat, CsvRepository")
public class ModelTest {

    // ================================================================
    // NHÓM 1: Fan Tests
    // ================================================================
    @Nested
    @DisplayName("Fan – Serialize (toCsvLine)")
    class FanSerializeTest {

        private Fan fan;

        @BeforeEach
        void setUp() {
            // Tạo Fan mẫu trước mỗi test
            fan = new Fan("FAN001", "Nguyen Van A", "a@mail.com", "0901234567", "hashXYZ");
        }

        @Test
        @DisplayName("toCsvLine() trả về đúng định dạng CSV")
        void testToCsvLine() {
            String csv = fan.toCsvLine();
            assertEquals("FAN001,Nguyen Van A,a@mail.com,0901234567,hashXYZ", csv);
        }

        @Test
        @DisplayName("toCsvHeader() trả về đúng tên cột")
        void testToCsvHeader() {
            assertEquals("id,name,email,phone,passwordHash", fan.toCsvHeader());
        }

        @Test
        @DisplayName("Fan với dữ liệu rỗng vẫn không crash")
        void testEmptyFanNotNull() {
            Fan emptyFan = new Fan("", "", "", "", "");
            assertNotNull(emptyFan.toCsvLine());
        }
    }

    // ================================================================
    @Nested
    @DisplayName("Fan – Deserialize (fromCsvLine)")
    class FanDeserializeTest {

        @Test
        @DisplayName("Parse CSV đúng format → các field đúng giá trị")
        void testFromCsvLine() {
            Fan fan = new Fan();
            fan.fromCsvLine("FAN0001,Tran Duc Duy,fan1@example.com,0900000001,$2a$10$FAKEHASH");

            assertAll("Kiểm tra tất cả fields cùng lúc",
                () -> assertEquals("FAN0001",              fan.getId()),
                () -> assertEquals("Tran Duc Duy",         fan.getName()),
                () -> assertEquals("fan1@example.com",     fan.getEmail()),
                () -> assertEquals("0900000001",           fan.getPhone()),
                () -> assertEquals("$2a$10$FAKEHASH",      fan.getPasswordHash())
            );
        }

        @Test
        @DisplayName("Round-trip: parse CSV rồi serialize lại phải ra cùng chuỗi gốc")
        void testRoundTrip() {
            String original = "FAN999,Le Thi B,b@test.com,0912345678,myHash";
            Fan fan = new Fan();
            fan.fromCsvLine(original);
            assertEquals(original, fan.toCsvLine());
        }

        @Test
        @DisplayName("Parse xong: name có trim() khoảng trắng thừa")
        void testTrimWhitespace() {
            Fan fan = new Fan();
            fan.fromCsvLine("FAN001, Nguyen Van A , a@mail.com , 0901 , hash");
            assertEquals("Nguyen Van A", fan.getName());
            assertEquals("a@mail.com",   fan.getEmail());
        }
    }

    // ================================================================
    @Nested
    @DisplayName("Fan – Validation trong fromCsvLine()")
    class FanValidationTest {

        @Test
        @DisplayName("null → phải throw IllegalArgumentException")
        void testNullInput() {
            assertThrows(IllegalArgumentException.class,
                () -> new Fan().fromCsvLine(null));
        }

        @Test
        @DisplayName("Chuỗi rỗng → phải throw IllegalArgumentException")
        void testEmptyInput() {
            assertThrows(IllegalArgumentException.class,
                () -> new Fan().fromCsvLine(""));
        }

        @Test
        @DisplayName("Chỉ có 3 trường (thiếu) → phải throw IllegalArgumentException")
        void testMissingFields() {
            assertThrows(IllegalArgumentException.class,
                () -> new Fan().fromCsvLine("FAN001,Nguyen Van A,a@mail.com"));
        }
    }

    // ================================================================
    // NHÓM 2: Seat Tests
    // ================================================================
    @Nested
    @DisplayName("Seat – Serialize (toCsvLine)")
    class SeatSerializeTest {

        @Test
        @DisplayName("toCsvLine() trả về đúng định dạng CSV")
        void testToCsvLine() {
            Seat seat = new Seat("SEAT000001", "ST001_S01", 1, 1, SeatStatus.AVAILABLE, 0);
            assertEquals("SEAT000001,ST001_S01,1,1,AVAILABLE,0", seat.toCsvLine());
        }

        @Test
        @DisplayName("toCsvHeader() trả về đúng tên cột")
        void testToCsvHeader() {
            Seat seat = new Seat();
            assertEquals("id,sectionId,rowNumber,seatNumber,status,version", seat.toCsvHeader());
        }

        @Test
        @DisplayName("Sau khi book(): status=BOOKED, version tăng lên 1")
        void testAfterBook() {
            Seat seat = new Seat("SEAT000001", "ST001_S01", 1, 1, SeatStatus.AVAILABLE, 0);
            seat.book();
            assertEquals("SEAT000001,ST001_S01,1,1,BOOKED,1", seat.toCsvLine());
        }
    }

    // ================================================================
    @Nested
    @DisplayName("Seat – Deserialize (fromCsvLine)")
    class SeatDeserializeTest {

        @Test
        @DisplayName("Parse CSV đúng format → các field đúng giá trị")
        void testFromCsvLine() {
            Seat seat = new Seat();
            seat.fromCsvLine("SEAT000001,ST001_S01,1,1,AVAILABLE,0");

            assertAll("Kiểm tra tất cả fields",
                () -> assertEquals("SEAT000001",        seat.getId()),
                () -> assertEquals("ST001_S01",         seat.getSectionId()),
                () -> assertEquals(1,                   seat.getRowNumber()),
                () -> assertEquals(1,                   seat.getSeatNumber()),
                () -> assertEquals(SeatStatus.AVAILABLE, seat.getStatus()),
                () -> assertEquals(0,                   seat.getVersion())
            );
        }

        @Test
        @DisplayName("Parse ghế LOCKED với version=2")
        void testLockedSeat() {
            Seat seat = new Seat();
            seat.fromCsvLine("SEAT000002,ST001_S02,3,7,LOCKED,2");
            assertEquals(SeatStatus.LOCKED, seat.getStatus());
            assertEquals(2, seat.getVersion());
        }

        @Test
        @DisplayName("Round-trip: parse CSV rồi serialize lại phải ra chuỗi gốc")
        void testRoundTrip() {
            String original = "SEAT999999,ST003_S08,5,100,BOOKED,3";
            Seat seat = new Seat();
            seat.fromCsvLine(original);
            assertEquals(original, seat.toCsvLine());
        }
    }

    // ================================================================
    @Nested
    @DisplayName("Seat – Validation trong fromCsvLine()")
    class SeatValidationTest {

        @Test
        @DisplayName("null → phải throw IllegalArgumentException")
        void testNullInput() {
            assertThrows(IllegalArgumentException.class,
                () -> new Seat().fromCsvLine(null));
        }

        @Test
        @DisplayName("Thiếu trường → phải throw IllegalArgumentException")
        void testMissingFields() {
            assertThrows(IllegalArgumentException.class,
                () -> new Seat().fromCsvLine("SEAT001,ST001_S01,1"));
        }

        @Test
        @DisplayName("rowNumber không phải số → phải throw IllegalArgumentException")
        void testInvalidNumber() {
            assertThrows(IllegalArgumentException.class,
                () -> new Seat().fromCsvLine("SEAT001,ST001_S01,ABC,1,AVAILABLE,0"));
        }
    }

    // ================================================================
    @Nested
    @DisplayName("Seat – State Machine (lock / unlock / book)")
    class SeatStateMachineTest {

        private Seat seat;

        @BeforeEach
        void setUp() {
            seat = new Seat("S1", "SEC01", 1, 1, SeatStatus.AVAILABLE, 0);
        }

        @Test
        @DisplayName("lock(): AVAILABLE → LOCKED, version tăng lên 1")
        void testLock() {
            assertTrue(seat.lock());
            assertEquals(SeatStatus.LOCKED, seat.getStatus());
            assertEquals(1, seat.getVersion());
        }

        @Test
        @DisplayName("lock() ghế đang LOCKED → trả về false")
        void testLockAlreadyLocked() {
            seat.lock();
            assertFalse(seat.lock()); // Lock lần 2 phải thất bại
        }

        @Test
        @DisplayName("unlock(): LOCKED → AVAILABLE")
        void testUnlock() {
            seat.lock();
            assertTrue(seat.unlock());
            assertEquals(SeatStatus.AVAILABLE, seat.getStatus());
        }

        @Test
        @DisplayName("unlock() ghế đang AVAILABLE → trả về false")
        void testUnlockWhenNotLocked() {
            assertFalse(seat.unlock()); // Chưa lock, không thể unlock
        }

        @Test
        @DisplayName("book(): AVAILABLE → BOOKED")
        void testBookFromAvailable() {
            assertTrue(seat.book());
            assertEquals(SeatStatus.BOOKED, seat.getStatus());
        }

        @Test
        @DisplayName("book() ghế đã BOOKED → trả về false")
        void testBookAlreadyBooked() {
            seat.book();
            assertFalse(seat.book()); // Book lần 2 phải thất bại
        }

        @Test
        @DisplayName("Luồng đầy đủ: AVAILABLE → LOCKED → BOOKED, version=2")
        void testFullBookingFlow() {
            assertTrue(seat.lock(),  "lock() phải thành công");
            assertTrue(seat.book(),  "book() từ LOCKED phải thành công");
            assertEquals(SeatStatus.BOOKED, seat.getStatus());
            assertEquals(2, seat.getVersion()); // 2 thao tác → version = 2
        }
    }

    // ================================================================
    // NHÓM 3: CsvRepository Tests (đọc file thực tế)
    // ================================================================
    @Nested
    @DisplayName("CsvRepository<Fan> – Đọc fans.csv")
    class CsvRepositoryFanTest {

        private CsvRepository<Fan> repo;

        @BeforeEach
        void setUp() {
            repo = new CsvRepository<>("data/fans.csv", Fan::new);
        }

        @Test
        @DisplayName("findAll(): đọc được 500 fan từ file")
        void testFindAll() {
            List<Fan> fans = repo.findAll();
            assertEquals(500, fans.size());
        }

        @Test
        @DisplayName("findAll(): fan đầu tiên có đủ thông tin")
        void testFirstFanHasData() {
            Fan first = repo.findAll().get(0);
            assertNotNull(first.getId());
            assertNotNull(first.getName());
            assertNotNull(first.getEmail());
        }

        @Test
        @DisplayName("findById('FAN0001'): tìm thấy đúng fan")
        void testFindById() {
            Optional<Fan> found = repo.findById("FAN0001");
            assertTrue(found.isPresent());
            assertEquals("fan1@example.com", found.get().getEmail());
        }

        @Test
        @DisplayName("findById('FAN99999'): trả về empty nếu không tồn tại")
        void testFindByIdNotFound() {
            Optional<Fan> notFound = repo.findById("FAN99999");
            assertFalse(notFound.isPresent());
        }
    }

    // ================================================================
    @Nested
    @DisplayName("CsvRepository<Seat> – Đọc seats.csv")
    class CsvRepositorySeatTest {

        private CsvRepository<Seat> repo;

        @BeforeEach
        void setUp() {
            repo = new CsvRepository<>("data/seats.csv", Seat::new);
        }

        @Test
        @DisplayName("findAll(): đọc được >= 10.000 ghế từ file")
        void testFindAll() {
            List<Seat> seats = repo.findAll();
            assertTrue(seats.size() >= 10000,
                "Số ghế phải >= 10000, thực tế: " + seats.size());
        }

        @Test
        @DisplayName("findById('SEAT000001'): tìm thấy đúng ghế")
        void testFindById() {
            Optional<Seat> found = repo.findById("SEAT000001");
            assertTrue(found.isPresent());
            assertEquals("ST001_S01", found.get().getSectionId());
            assertEquals(1, found.get().getRowNumber());
        }

        @Test
        @DisplayName("Tất cả ghế đọc được đều có status hợp lệ (không null)")
        void testAllSeatsHaveValidStatus() {
            List<Seat> seats = repo.findAll();
            assertTrue(seats.stream().allMatch(s -> s.getStatus() != null),
                "Có ghế bị null status!");
        }
    }
}
