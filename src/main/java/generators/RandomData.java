package generators;

import org.apache.commons.lang3.RandomStringUtils;
import java.util.concurrent.ThreadLocalRandom;

public class RandomData {
    private RandomData() {}

    public static String getUserName() {
        return RandomStringUtils.insecure().nextAlphabetic(10);
    }

    public static String getPassword() {
        return RandomStringUtils.insecure().nextAlphabetic(3).toUpperCase() +
                RandomStringUtils.insecure().nextAlphabetic(5).toLowerCase() +
                RandomStringUtils.insecure().nextNumeric(3) + "$";
    }

    // генерация суммы от 0.01 до 5000.00
    public static double randomDepositAmount() {
        int amountInCents = ThreadLocalRandom.current().nextInt(1, 500001);
        return amountInCents / 100.0;
    }

    // генерация id для проверки несуществующего акка
    public static int getNonExistingAccountID() {
        return ThreadLocalRandom.current().nextInt(1_000_000, Integer.MAX_VALUE);
    }
}
