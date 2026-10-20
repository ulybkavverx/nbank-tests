package generators;

import org.apache.commons.lang3.RandomStringUtils;

public class RandomData {
    private RandomData() {}

    public static String getUserName() {
        return RandomStringUtils.insecure().nextAlphabetic(10);
    }

    public static String getPassword() {
        return RandomStringUtils.insecure().nextAlphabetic(3).toUpperCase() +
                RandomStringUtils.insecure().nextAlphabetic(5).toLowerCase() +
                RandomStringUtils.insecure().nextNumeric(3) + "$" ;
    }
}
