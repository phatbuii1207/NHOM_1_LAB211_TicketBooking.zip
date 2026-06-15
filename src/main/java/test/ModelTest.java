package test;

import model.Fan;
import model.Seat;
import model.SeatStatus;
import repository.CsvRepository;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Model Layer Tests - Fan | Seat | CsvRepository")
public class ModelTest {

    // ================================================================
    // GROUP 1: Fan
    // ================================================================
    @Nested
    @DisplayName("[Fan] Serialize - toCsvLine()")
    class FanSerializeTest {

        private Fan fan;

        @BeforeEach
        void setUp() {
            fan = new Fan("FAN001", "Nguyen Van A", "a@mail.com", "0901234567", "hashXYZ");
        }

        @Test
        @DisplayName("toCsvLine() returns correct CSV format")
        void testToCsvLine() {
            assertEquals("FAN001,Nguyen Van A,a@mail.com,0901234567,hashXYZ", fan.toCsvLine());
        }

        @Test
        @DisplayName("toCsvHeader() returns correct column names")
        void testToCsvHeader() {
            assertEquals("id,name,email,phone,passwordHash", fan.toCsvHeader());
        }

        @Test
        @DisplayName("Fan with empty data does not crash")
        void testEmptyFanNotNull() {
            Fan emptyFan = new Fan("", "", "", "", "");
            assertNotNull(emptyFan.toCsvLine());
        }
    }

    // ================================================================
    @Nested
    @DisplayName("[Fan] Deserialize - fromCsvLine()")
    class FanDeserializeTest {

        @Test
        @DisplayName("Parse valid CSV -> all fields correct")
        void testFromCsvLine() {
            Fan fan = new Fan();
            fan.fromCsvLine("FAN0001,Tran Duc Duy,fan1@example.com,0900000001,$2a$10$FAKEHASH");

            assertAll("Check all fields at once",
                () -> assertEquals("FAN0001",          fan.getId()),
                () -> assertEquals("Tran Duc Duy",     fan.getName()),
                () -> assertEquals("fan1@example.com", fan.getEmail()),
                () -> assertEquals("0900000001",       fan.getPhone()),
                () -> assertEquals("$2a$10$FAKEHASH",  fan.getPasswordHash())
            );
        }

        @Test
        @DisplayName("Round-trip: parse CSV then re-serialize must match original")
        void testRoundTrip() {
            String original = "FAN999,Le Thi B,b@test.com,0912345678,myHash";
            Fan fan = new Fan();
            fan.fromCsvLine(original);
            assertEquals(original, fan.toCsvLine());
        }

        @Test
        @DisplayName("Fields are trimmed of extra whitespace")
        void testTrimWhitespace() {
            Fan fan = new Fan();
            fan.fromCsvLine("FAN001, Nguyen Van A , a@mail.com , 0901 , hash");
            assertEquals("Nguyen Van A", fan.getName());
            assertEquals("a@mail.com",   fan.getEmail());
        }
    }

    // ================================================================
    @Nested
    @DisplayName("[Fan] Validation - fromCsvLine() error handling")
    class FanValidationTest {

        @Test
        @DisplayName("null input -> throws IllegalArgumentException")
        void testNullInput() {
            assertThrows(IllegalArgumentException.class,
                () -> new Fan().fromCsvLine(null));
        }

        @Test
        @DisplayName("empty string -> throws IllegalArgumentException")
        void testEmptyInput() {
            assertThrows(IllegalArgumentException.class,
                () -> new Fan().fromCsvLine(""));
        }

        @Test
        @DisplayName("only 3 fields (missing) -> throws IllegalArgumentException")
        void testMissingFields() {
            assertThrows(IllegalArgumentException.class,
                () -> new Fan().fromCsvLine("FAN001,Nguyen Van A,a@mail.com"));
        }
    }

    // ================================================================
    // GROUP 2: Seat
    // ================================================================
    @Nested
    @DisplayName("[Seat] Serialize - toCsvLine()")
    class SeatSerializeTest {

        @Test
        @DisplayName("toCsvLine() returns correct CSV format")
        void testToCsvLine() {
            Seat seat = new Seat("SEAT000001", "ST001_S01", 1, 1, SeatStatus.AVAILABLE, 0);
            assertEquals("SEAT000001,ST001_S01,1,1,AVAILABLE,0", seat.toCsvLine());
        }

        @Test
        @DisplayName("toCsvHeader() returns correct column names")
        void testToCsvHeader() {
            assertEquals("id,sectionId,rowNumber,seatNumber,status,version", new Seat().toCsvHeader());
        }

        @Test
        @DisplayName("After book(): status=BOOKED, version incremented to 1")
        void testAfterBook() {
            Seat seat = new Seat("SEAT000001", "ST001_S01", 1, 1, SeatStatus.AVAILABLE, 0);
            seat.book();
            assertEquals("SEAT000001,ST001_S01,1,1,BOOKED,1", seat.toCsvLine());
        }
    }

    // ================================================================
    @Nested
    @DisplayName("[Seat] Deserialize - fromCsvLine()")
    class SeatDeserializeTest {

        @Test
        @DisplayName("Parse valid CSV -> all fields correct")
        void testFromCsvLine() {
            Seat seat = new Seat();
            seat.fromCsvLine("SEAT000001,ST001_S01,1,1,AVAILABLE,0");

            assertAll("Check all fields",
                () -> assertEquals("SEAT000001",         seat.getId()),
                () -> assertEquals("ST001_S01",          seat.getSectionId()),
                () -> assertEquals(1,                    seat.getRowNumber()),
                () -> assertEquals(1,                    seat.getSeatNumber()),
                () -> assertEquals(SeatStatus.AVAILABLE, seat.getStatus()),
                () -> assertEquals(0,                    seat.getVersion())
            );
        }

        @Test
        @DisplayName("Parse LOCKED seat with version=2")
        void testLockedSeat() {
            Seat seat = new Seat();
            seat.fromCsvLine("SEAT000002,ST001_S02,3,7,LOCKED,2");
            assertEquals(SeatStatus.LOCKED, seat.getStatus());
            assertEquals(2, seat.getVersion());
        }

        @Test
        @DisplayName("Round-trip: parse CSV then re-serialize must match original")
        void testRoundTrip() {
            String original = "SEAT999999,ST003_S08,5,100,BOOKED,3";
            Seat seat = new Seat();
            seat.fromCsvLine(original);
            assertEquals(original, seat.toCsvLine());
        }
    }

    // ================================================================
    @Nested
    @DisplayName("[Seat] Validation - fromCsvLine() error handling")
    class SeatValidationTest {

        @Test
        @DisplayName("null input -> throws IllegalArgumentException")
        void testNullInput() {
            assertThrows(IllegalArgumentException.class,
                () -> new Seat().fromCsvLine(null));
        }

        @Test
        @DisplayName("missing fields -> throws IllegalArgumentException")
        void testMissingFields() {
            assertThrows(IllegalArgumentException.class,
                () -> new Seat().fromCsvLine("SEAT001,ST001_S01,1"));
        }

        @Test
        @DisplayName("non-numeric rowNumber -> throws IllegalArgumentException")
        void testInvalidNumber() {
            assertThrows(IllegalArgumentException.class,
                () -> new Seat().fromCsvLine("SEAT001,ST001_S01,ABC,1,AVAILABLE,0"));
        }
    }

    // ================================================================
    @Nested
    @DisplayName("[Seat] State Machine - lock / unlock / book")
    class SeatStateMachineTest {

        private Seat seat;

        @BeforeEach
        void setUp() {
            seat = new Seat("S1", "SEC01", 1, 1, SeatStatus.AVAILABLE, 0);
        }

        @Test
        @DisplayName("lock(): AVAILABLE -> LOCKED, version = 1")
        void testLock() {
            assertTrue(seat.lock());
            assertEquals(SeatStatus.LOCKED, seat.getStatus());
            assertEquals(1, seat.getVersion());
        }

        @Test
        @DisplayName("lock() on LOCKED seat -> returns false")
        void testLockAlreadyLocked() {
            seat.lock();
            assertFalse(seat.lock());
        }

        @Test
        @DisplayName("unlock(): LOCKED -> AVAILABLE")
        void testUnlock() {
            seat.lock();
            assertTrue(seat.unlock());
            assertEquals(SeatStatus.AVAILABLE, seat.getStatus());
        }

        @Test
        @DisplayName("unlock() on AVAILABLE seat -> returns false")
        void testUnlockWhenNotLocked() {
            assertFalse(seat.unlock());
        }

        @Test
        @DisplayName("book(): AVAILABLE -> BOOKED")
        void testBookFromAvailable() {
            assertTrue(seat.book());
            assertEquals(SeatStatus.BOOKED, seat.getStatus());
        }

        @Test
        @DisplayName("book() on already BOOKED seat -> returns false")
        void testBookAlreadyBooked() {
            seat.book();
            assertFalse(seat.book());
        }

        @Test
        @DisplayName("Full flow: AVAILABLE -> LOCKED -> BOOKED, version = 2")
        void testFullBookingFlow() {
            assertTrue(seat.lock(), "lock() must succeed");
            assertTrue(seat.book(), "book() from LOCKED must succeed");
            assertEquals(SeatStatus.BOOKED, seat.getStatus());
            assertEquals(2, seat.getVersion());
        }
    }

    // ================================================================
    // GROUP 3: CsvRepository (reads real CSV files)
    // ================================================================
    @Nested
    @DisplayName("[CsvRepo] Fan - reads fans.csv")
    class CsvRepositoryFanTest {

        private CsvRepository<Fan> repo;

        @BeforeEach
        void setUp() {
            repo = new CsvRepository<>("data/fans.csv", Fan::new);
        }

        @Test
        @DisplayName("findAll() reads 500 fans from file")
        void testFindAll() {
            assertEquals(500, repo.findAll().size());
        }

        @Test
        @DisplayName("First fan has non-null id, name, email")
        void testFirstFanHasData() {
            Fan first = repo.findAll().get(0);
            assertNotNull(first.getId());
            assertNotNull(first.getName());
            assertNotNull(first.getEmail());
        }

        @Test
        @DisplayName("findById('FAN0001') -> found with correct email")
        void testFindById() {
            Optional<Fan> found = repo.findById("FAN0001");
            assertTrue(found.isPresent());
            assertEquals("fan1@example.com", found.get().getEmail());
        }

        @Test
        @DisplayName("findById('FAN99999') -> empty (not found)")
        void testFindByIdNotFound() {
            assertFalse(repo.findById("FAN99999").isPresent());
        }
    }

    // ================================================================
    @Nested
    @DisplayName("[CsvRepo] Seat - reads seats.csv")
    class CsvRepositorySeatTest {

        private CsvRepository<Seat> repo;

        @BeforeEach
        void setUp() {
            repo = new CsvRepository<>("data/seats.csv", Seat::new);
        }

        @Test
        @DisplayName("findAll() reads >= 10,000 seats from file")
        void testFindAll() {
            assertTrue(repo.findAll().size() >= 10000);
        }

        @Test
        @DisplayName("findById('SEAT000001') -> found with correct data")
        void testFindById() {
            Optional<Seat> found = repo.findById("SEAT000001");
            assertTrue(found.isPresent());
            assertEquals("ST001_S01", found.get().getSectionId());
            assertEquals(1, found.get().getRowNumber());
        }

        @Test
        @DisplayName("All seats have non-null status")
        void testAllSeatsHaveValidStatus() {
            assertTrue(repo.findAll().stream().allMatch(s -> s.getStatus() != null));
        }
    }
}
