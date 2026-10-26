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

    // генерация суммы, исключая граничные значения
    // диапазон: от 0.02 до 4999.99 включительно
    public static double randomNonBoundaryDepositAmount() {
        int amountCents = ThreadLocalRandom.current().nextInt(2, 500000);
        return amountCents / 100.0;
    }

    // генерация id для проверки несуществующего акка
    public static int getNonExistingAccountID() {
        return ThreadLocalRandom.current().nextInt(1_000_000, Integer.MAX_VALUE);
    }

    // генерация имени
    public static String randomValidFullName() {
        String firstName = RandomStringUtils.insecure().nextAlphabetic(5);
        String lastName = RandomStringUtils.insecure().nextAlphabetic(7);

        return capitalize(firstName) + " " + capitalize(lastName);
    }

    private static String capitalize(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }

    // генерация обычной суммы перевода без граничных значений
    // диапазон: от 0.02 до 9999.98 включительно
    public static double randomNonBoundaryTransferAmount() {
        double amountCents = ThreadLocalRandom.current().nextInt(2, 999_999);
        return amountCents / 100.0;
    }
}
