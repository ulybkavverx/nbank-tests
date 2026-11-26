package common.utils;

public final class CommonUtils {
    private CommonUtils(){}

    public static void repeat(int times, Runnable action) {
        for (int i = 0; i < times; i++) {
            action.run();
        }
    }
}
