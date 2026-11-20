package ui.pages;

import lombok.Getter;

@Getter
public enum BankAlert {
    USER_CREATED_SUCCESSFULLY("✅ User created successfully!"),
    USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS("Username must be between 3 and 15 characters"),
    NEW_ACCOUNT_CREATED("✅ New Account Created! Account Number: "),
    DEPOSIT_SUCCESSFULLY("✅ Successfully deposited"),
    DEPOSIT_LESS_OR_EQUALS_TO_5000$("❌ Please deposit less or equal to 5000$."),
    TRANSFER_SUCCESSFULLY("✅ Successfully transferred"),
    TRANSFER_AMOUNT_CANNOT_EXCEED_10000("❌ Error: Transfer amount cannot exceed 10000"),
    NAME_UPDATE_SUCCESSFULLY("✅ Name updated successfully!"),
    NAME_MUST_CONTAINS_2_WORDS_WITH_LETTERS_ONLY("Name must contain two words with letters only"),
    PLEASE_ENTER_A_VALID_NAME("❌ Please enter a valid name.");

    private final String message;

    BankAlert(String message) {
        this.message = message;
    }
}
