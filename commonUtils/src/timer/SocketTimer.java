package timer;

public class SocketTimer {
    private static long start, end;
    private static long newTimeout;

    public static void start() {
        start = System.currentTimeMillis();
    }

    public static void stop() {
        end = System.currentTimeMillis();
    }

    public static int getTimeout() {
        long duration = end - start;
        newTimeout = duration * 3;

        return (int) newTimeout;
    }
}
