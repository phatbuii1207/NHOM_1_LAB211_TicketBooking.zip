package model;

/**
 * SimulationResult – Ket qua mo phong dat ve dong thoi (dung cho T8 Simulator).
 *
 * Luu:
 *   - lockType         : co che lock (NO_LOCK / SYNCHRONIZED / FILE_LOCK / OPTIMISTIC)
 *   - threadCount      : so luong thread
 *   - successCount     : so luong dat ve thanh cong
 *   - failCount        : so luong that bai (het ghe hoac loi)
 *   - doubleBookCount  : so truong hop double-booking (2 thread cung dat 1 ghe)
 *   - durationMs       : thoi gian chay (milliseconds)
 *   - throughputPerSec : so ve dat thanh cong / giay
 */
public class SimulationResult {

    private String lockType;
    private int    threadCount;
    private int    successCount;
    private int    failCount;
    private int    doubleBookCount;
    private long   durationMs;
    private double throughputPerSec;

    public SimulationResult() {}

    public SimulationResult(String lockType, int threadCount, int successCount,
                            int failCount, int doubleBookCount, long durationMs) {
        this.lockType        = lockType;
        this.threadCount     = threadCount;
        this.successCount    = successCount;
        this.failCount       = failCount;
        this.doubleBookCount = doubleBookCount;
        this.durationMs      = durationMs;
        this.throughputPerSec = durationMs > 0
                ? (successCount * 1000.0 / durationMs)
                : 0;
    }

    // Getters
    public String getLockType()        { return lockType; }
    public int    getThreadCount()     { return threadCount; }
    public int    getSuccessCount()    { return successCount; }
    public int    getFailCount()       { return failCount; }
    public int    getDoubleBookCount() { return doubleBookCount; }
    public long   getDurationMs()      { return durationMs; }
    public double getThroughputPerSec(){ return throughputPerSec; }

    /** CSV header de ghi ra file ket qua */
    public static String csvHeader() {
        return "lockType,threads,success,fail,doubleBook,durationMs,throughput";
    }

    /** Chuyen thanh dong CSV */
    public String toCsvLine() {
        return String.format("%s,%d,%d,%d,%d,%d,%.2f",
                lockType, threadCount, successCount,
                failCount, doubleBookCount, durationMs, throughputPerSec);
    }

    @Override
    public String toString() {
        return String.format(
            "[%s] threads=%d success=%d fail=%d doubleBook=%d time=%dms throughput=%.1f/s",
            lockType, threadCount, successCount,
            failCount, doubleBookCount, durationMs, throughputPerSec);
    }
}
