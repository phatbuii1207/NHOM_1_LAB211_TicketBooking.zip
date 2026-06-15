package test;

import model.Fan;
import model.Seat;
import model.SeatStatus;
import repository.FanRepository;
import repository.SeatRepository;

import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RepositoryTest – Test CRUD + Read/Write file + Performance.
 *
 * Deliverable T4:
 *   - Repository CRUD test pass
 *   - Doc file >= 10k dong < 500ms
 *
 * NOTE: FanRepository va SeatRepository dung file TEMP rieng biet
 *       de khong lam hong data that trong data/fans.csv, data/seats.csv.
 */
@DisplayName("T4 Repository Layer Tests - Fan | Seat")
public class RepositoryTest {

    // ================================================================
    // FAN REPOSITORY TESTS
    // ================================================================
    @Nested
    @DisplayName("[FanRepo] CRUD Operations")
    class FanRepositoryCrudTest {

        private FanRepository repo;
        private File tempFile;

        @BeforeEach
        void setUp() throws Exception {
            // Dung file tam de khong lam hong fans.csv that
            tempFile = File.createTempFile("test_fans_", ".csv");
            tempFile.deleteOnExit();
            repo = new FanRepository(tempFile.getAbsolutePath());
        }

        @Test
        @DisplayName("findAll() on empty file -> returns empty list")
        void testFindAllEmpty() {
            List<Fan> fans = repo.findAll();
            assertTrue(fans.isEmpty(), "File moi tao phai rong");
        }

        @Test
        @DisplayName("save() new fan -> findAll() size increases by 1")
        void testSaveNewFan() {
            Fan fan = new Fan("FAN001", "Nguyen Van A", "a@mail.com", "0901", "hash1");
            boolean saved = repo.save(fan);

            assertTrue(saved, "save() phai tra true");
            assertEquals(1, repo.count(), "Sau save, count phai = 1");
        }

        @Test
        @DisplayName("save() 3 fans -> count() = 3")
        void testSaveMultiple() {
            repo.save(new Fan("FAN001", "Fan One", "one@mail.com", "0901", "h1"));
            repo.save(new Fan("FAN002", "Fan Two", "two@mail.com", "0902", "h2"));
            repo.save(new Fan("FAN003", "Fan Three", "three@mail.com", "0903", "h3"));

            assertEquals(3, repo.count());
        }

        @Test
        @DisplayName("findById() -> returns correct fan")
        void testFindById() {
            Fan fan = new Fan("FAN001", "Nguyen Van A", "a@mail.com", "0901", "hash");
            repo.save(fan);

            Optional<Fan> found = repo.findById("FAN001");
            assertTrue(found.isPresent(), "Phai tim thay FAN001");
            assertEquals("Nguyen Van A", found.get().getName());
            assertEquals("a@mail.com",   found.get().getEmail());
        }

        @Test
        @DisplayName("findById() with non-existent id -> returns empty")
        void testFindByIdNotFound() {
            assertFalse(repo.findById("FAN999").isPresent());
        }

        @Test
        @DisplayName("save() existing id -> UPDATE (not duplicate)")
        void testSaveUpdate() {
            repo.save(new Fan("FAN001", "Old Name", "old@mail.com", "0901", "hash"));
            repo.save(new Fan("FAN001", "New Name", "new@mail.com", "0901", "hash")); // Update

            assertEquals(1, repo.count(), "Khong duoc them ban sao, phai update");
            assertEquals("New Name", repo.findById("FAN001").get().getName());
        }

        @Test
        @DisplayName("deleteById() -> fan is removed, count decreases")
        void testDeleteById() {
            repo.save(new Fan("FAN001", "Fan A", "a@mail.com", "0901", "h"));
            repo.save(new Fan("FAN002", "Fan B", "b@mail.com", "0902", "h"));

            boolean deleted = repo.deleteById("FAN001");

            assertTrue(deleted, "deleteById phai tra true");
            assertEquals(1, repo.count(), "Count phai giam xuong 1");
            assertFalse(repo.findById("FAN001").isPresent(), "FAN001 phai bi xoa");
            assertTrue(repo.findById("FAN002").isPresent(), "FAN002 phai con ton tai");
        }

        @Test
        @DisplayName("deleteById() non-existent id -> returns false, nothing changes")
        void testDeleteNotFound() {
            repo.save(new Fan("FAN001", "Fan A", "a@mail.com", "0901", "h"));
            boolean result = repo.deleteById("FAN999");

            assertFalse(result, "Xoa id khong ton tai phai tra false");
            assertEquals(1, repo.count(), "Count khong duoc thay doi");
        }
    }

    // ================================================================
    @Nested
    @DisplayName("[FanRepo] findByCondition & findByEmail")
    class FanRepositoryQueryTest {

        private FanRepository repo;

        @BeforeEach
        void setUp() throws Exception {
            File tempFile = File.createTempFile("test_fans_q_", ".csv");
            tempFile.deleteOnExit();
            repo = new FanRepository(tempFile.getAbsolutePath());

            // Them data mau
            repo.save(new Fan("F001", "Alice",   "alice@gmail.com",   "0901", "h1"));
            repo.save(new Fan("F002", "Bob",     "bob@yahoo.com",     "0902", "h2"));
            repo.save(new Fan("F003", "Charlie", "charlie@gmail.com", "0903", "h3"));
            repo.save(new Fan("F004", "David",   "david@work.com",    "0904", "h4"));
        }

        @Test
        @DisplayName("findByEmail() -> returns matching fan")
        void testFindByEmail() {
            Optional<Fan> found = repo.findByEmail("alice@gmail.com");
            assertTrue(found.isPresent());
            assertEquals("Alice", found.get().getName());
        }

        @Test
        @DisplayName("findByEmail() case-insensitive")
        void testFindByEmailCaseInsensitive() {
            Optional<Fan> found = repo.findByEmail("ALICE@GMAIL.COM");
            assertTrue(found.isPresent(), "Tim kiem email khong phan biet hoa thuong");
        }

        @Test
        @DisplayName("findByCondition() filter by gmail domain -> 2 results")
        void testFindByConditionGmail() {
            List<Fan> gmails = repo.findByCondition(f -> f.getEmail().endsWith("@gmail.com"));
            assertEquals(2, gmails.size(), "Phai co 2 fan gmail");
        }

        @Test
        @DisplayName("findByCondition() filter by name starting with 'A' -> 1 result")
        void testFindByConditionName() {
            List<Fan> result = repo.findByCondition(f -> f.getName().startsWith("A"));
            assertEquals(1, result.size());
            assertEquals("Alice", result.get(0).getName());
        }

        @Test
        @DisplayName("findByCondition() no match -> empty list")
        void testFindByConditionNoMatch() {
            List<Fan> result = repo.findByCondition(f -> f.getEmail().endsWith("@nonexistent.vn"));
            assertTrue(result.isEmpty());
        }
    }

    // ================================================================
    @Nested
    @DisplayName("[FanRepo] CSV Read/Write Integrity")
    class FanRepositoryFileTest {

        @Test
        @DisplayName("Data is persisted: save fan, re-read from same file -> still there")
        void testDataPersistence() throws Exception {
            File tempFile = File.createTempFile("persist_test_", ".csv");
            tempFile.deleteOnExit();

            // Lan 1: Save
            FanRepository repo1 = new FanRepository(tempFile.getAbsolutePath());
            repo1.save(new Fan("FAN001", "Nguyen Van A", "a@mail.com", "0901", "hash"));

            // Lan 2: Doc lai tu cung file (tao repository moi)
            FanRepository repo2 = new FanRepository(tempFile.getAbsolutePath());
            Optional<Fan> found = repo2.findById("FAN001");

            assertTrue(found.isPresent(), "Du lieu phai ton tai sau khi doc lai");
            assertEquals("Nguyen Van A", found.get().getName());
        }

        @Test
        @DisplayName("Reads real fans.csv -> 500 fans")
        void testReadRealFile() {
            FanRepository realRepo = new FanRepository("data/fans.csv");
            assertEquals(500, realRepo.count(),
                "fans.csv phai co dung 500 fan");
        }

        @Test
        @DisplayName("Round-trip: save then read -> data matches exactly")
        void testRoundTrip() throws Exception {
            File tempFile = File.createTempFile("roundtrip_", ".csv");
            tempFile.deleteOnExit();
            FanRepository repo = new FanRepository(tempFile.getAbsolutePath());

            Fan original = new Fan("FAN999", "Le Thi B", "b@test.com", "0912345678", "myHash");
            repo.save(original);

            Fan loaded = repo.findById("FAN999").get();
            assertAll("Round-trip: tat ca fields phai khop",
                () -> assertEquals(original.getId(),           loaded.getId()),
                () -> assertEquals(original.getName(),         loaded.getName()),
                () -> assertEquals(original.getEmail(),        loaded.getEmail()),
                () -> assertEquals(original.getPhone(),        loaded.getPhone()),
                () -> assertEquals(original.getPasswordHash(), loaded.getPasswordHash())
            );
        }
    }

    // ================================================================
    // SEAT REPOSITORY TESTS
    // ================================================================
    @Nested
    @DisplayName("[SeatRepo] CRUD Operations")
    class SeatRepositoryCrudTest {

        private SeatRepository repo;

        @BeforeEach
        void setUp() throws Exception {
            File tempFile = File.createTempFile("test_seats_", ".csv");
            tempFile.deleteOnExit();
            repo = new SeatRepository(tempFile.getAbsolutePath());
        }

        @Test
        @DisplayName("save() new seat -> count() = 1")
        void testSaveNewSeat() {
            Seat seat = new Seat("S001", "SEC01", 1, 1, SeatStatus.AVAILABLE, 0);
            assertTrue(repo.save(seat));
            assertEquals(1, repo.count());
        }

        @Test
        @DisplayName("findById() -> returns correct seat with all fields")
        void testFindById() {
            Seat seat = new Seat("SEAT001", "ST001_S01", 2, 5, SeatStatus.AVAILABLE, 0);
            repo.save(seat);

            Optional<Seat> found = repo.findById("SEAT001");
            assertTrue(found.isPresent());
            assertAll("Tat ca fields phai dung",
                () -> assertEquals("ST001_S01",          found.get().getSectionId()),
                () -> assertEquals(2,                    found.get().getRowNumber()),
                () -> assertEquals(5,                    found.get().getSeatNumber()),
                () -> assertEquals(SeatStatus.AVAILABLE, found.get().getStatus()),
                () -> assertEquals(0,                    found.get().getVersion())
            );
        }

        @Test
        @DisplayName("save() update seat status -> persisted correctly")
        void testUpdateStatus() {
            Seat seat = new Seat("S001", "SEC01", 1, 1, SeatStatus.AVAILABLE, 0);
            repo.save(seat);

            // Cap nhat trang thai
            seat.lock();
            repo.save(seat);

            Seat loaded = repo.findById("S001").get();
            assertEquals(SeatStatus.LOCKED, loaded.getStatus());
            assertEquals(1, loaded.getVersion());
        }

        @Test
        @DisplayName("deleteById() -> removes seat correctly")
        void testDelete() {
            repo.save(new Seat("S001", "SEC01", 1, 1, SeatStatus.AVAILABLE, 0));
            repo.save(new Seat("S002", "SEC01", 1, 2, SeatStatus.AVAILABLE, 0));

            assertTrue(repo.deleteById("S001"));
            assertEquals(1, repo.count());
            assertFalse(repo.findById("S001").isPresent());
        }
    }

    // ================================================================
    @Nested
    @DisplayName("[SeatRepo] Special Queries")
    class SeatRepositoryQueryTest {

        private SeatRepository repo;

        @BeforeEach
        void setUp() throws Exception {
            File tempFile = File.createTempFile("test_seats_q_", ".csv");
            tempFile.deleteOnExit();
            repo = new SeatRepository(tempFile.getAbsolutePath());

            // Them 5 ghe mau
            repo.save(new Seat("S001", "SEC01", 1, 1, SeatStatus.AVAILABLE, 0));
            repo.save(new Seat("S002", "SEC01", 1, 2, SeatStatus.AVAILABLE, 0));
            repo.save(new Seat("S003", "SEC01", 1, 3, SeatStatus.LOCKED,    1));
            repo.save(new Seat("S004", "SEC02", 2, 1, SeatStatus.AVAILABLE, 0));
            repo.save(new Seat("S005", "SEC02", 2, 2, SeatStatus.BOOKED,    1));
        }

        @Test
        @DisplayName("findBySectionId('SEC01') -> 3 seats")
        void testFindBySectionId() {
            List<Seat> result = repo.findBySectionId("SEC01");
            assertEquals(3, result.size());
        }

        @Test
        @DisplayName("findAvailableSeats() -> 3 available seats")
        void testFindAvailable() {
            List<Seat> available = repo.findAvailableSeats();
            assertEquals(3, available.size());
            assertTrue(available.stream().allMatch(s -> s.getStatus() == SeatStatus.AVAILABLE));
        }

        @Test
        @DisplayName("findLockedSeats() -> 1 locked seat")
        void testFindLocked() {
            assertEquals(1, repo.findLockedSeats().size());
        }

        @Test
        @DisplayName("findBookedSeats() -> 1 booked seat")
        void testFindBooked() {
            assertEquals(1, repo.findBookedSeats().size());
        }

        @Test
        @DisplayName("findAvailableInSection('SEC01') -> 2 seats")
        void testFindAvailableInSection() {
            List<Seat> result = repo.findAvailableInSection("SEC01");
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("findByCondition: row 1 -> 3 seats")
        void testFindByConditionRow() {
            List<Seat> row1 = repo.findByCondition(s -> s.getRowNumber() == 1);
            assertEquals(3, row1.size());
        }

        @Test
        @DisplayName("countByStatus(AVAILABLE) -> 3")
        void testCountByStatus() {
            assertEquals(3, repo.countByStatus(SeatStatus.AVAILABLE));
            assertEquals(1, repo.countByStatus(SeatStatus.LOCKED));
            assertEquals(1, repo.countByStatus(SeatStatus.BOOKED));
        }
    }

    // ================================================================
    @Nested
    @DisplayName("[SeatRepo] lockSeat / unlockSeat / bookSeat")
    class SeatRepositoryStateTest {

        private SeatRepository repo;

        @BeforeEach
        void setUp() throws Exception {
            File tempFile = File.createTempFile("state_seats_", ".csv");
            tempFile.deleteOnExit();
            repo = new SeatRepository(tempFile.getAbsolutePath());
            repo.save(new Seat("S001", "SEC01", 1, 1, SeatStatus.AVAILABLE, 0));
        }

        @Test
        @DisplayName("lockSeat() -> AVAILABLE to LOCKED, persisted in file")
        void testLockSeat() {
            assertTrue(repo.lockSeat("S001"), "lockSeat phai thanh cong");

            Seat loaded = repo.findById("S001").get();
            assertEquals(SeatStatus.LOCKED, loaded.getStatus());
            assertEquals(1, loaded.getVersion());
        }

        @Test
        @DisplayName("lockSeat() on non-existent seat -> false")
        void testLockNotFound() {
            assertFalse(repo.lockSeat("S999"));
        }

        @Test
        @DisplayName("unlockSeat() after lock -> back to AVAILABLE")
        void testUnlockSeat() {
            repo.lockSeat("S001");
            assertTrue(repo.unlockSeat("S001"));
            assertEquals(SeatStatus.AVAILABLE, repo.findById("S001").get().getStatus());
        }

        @Test
        @DisplayName("bookSeat() -> AVAILABLE to BOOKED, persisted")
        void testBookSeat() {
            assertTrue(repo.bookSeat("S001"));
            assertEquals(SeatStatus.BOOKED, repo.findById("S001").get().getStatus());
        }

        @Test
        @DisplayName("Full flow: AVAILABLE -> LOCKED -> BOOKED, persisted each step")
        void testFullFlow() {
            repo.lockSeat("S001");
            assertEquals(SeatStatus.LOCKED, repo.findById("S001").get().getStatus());

            repo.bookSeat("S001");
            Seat final_ = repo.findById("S001").get();
            assertEquals(SeatStatus.BOOKED, final_.getStatus());
            assertEquals(2, final_.getVersion()); // 2 thao tac = version 2
        }
    }

    // ================================================================
    @Nested
    @DisplayName("[Performance] Read >= 10k rows < 500ms")
    class PerformanceTest {

        @Test
        @DisplayName("SeatRepository: read 10k+ rows from seats.csv < 500ms")
        void testReadSeatsPerformance() {
            SeatRepository repo = new SeatRepository("data/seats.csv");

            long start = System.currentTimeMillis();
            List<Seat> seats = repo.findAll();
            long elapsed = System.currentTimeMillis() - start;

            System.out.println("[Performance] Read " + seats.size() + " seats in " + elapsed + "ms");

            assertTrue(seats.size() >= 10000,
                "seats.csv phai co >= 10000 ghe, thuc te: " + seats.size());
            assertTrue(elapsed < 500,
                "Doc " + seats.size() + " ghe phai < 500ms, thuc te: " + elapsed + "ms");
        }

        @Test
        @DisplayName("FanRepository: read 500 fans < 100ms")
        void testReadFansPerformance() {
            FanRepository repo = new FanRepository("data/fans.csv");

            long start = System.currentTimeMillis();
            List<Fan> fans = repo.findAll();
            long elapsed = System.currentTimeMillis() - start;

            System.out.println("[Performance] Read " + fans.size() + " fans in " + elapsed + "ms");

            assertEquals(500, fans.size());
            assertTrue(elapsed < 100,
                "Doc 500 fan phai < 100ms, thuc te: " + elapsed + "ms");
        }

        @Test
        @DisplayName("findByCondition on 10k+ seats < 600ms total")
        void testFilterPerformance() {
            SeatRepository repo = new SeatRepository("data/seats.csv");

            long start = System.currentTimeMillis();
            List<Seat> available = repo.findAvailableSeats();
            long elapsed = System.currentTimeMillis() - start;

            System.out.println("[Performance] findAvailableSeats: " + available.size()
                    + " results in " + elapsed + "ms");

            assertTrue(elapsed < 600,
                "findAvailableSeats tren 10k ghe phai < 600ms, thuc te: " + elapsed + "ms");
        }
    }
}
