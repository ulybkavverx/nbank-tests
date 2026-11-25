package api.requests.skelethon;

import api.models.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Endpoint {
    ADMIN_USER(
            "/admin/users",
            CreateUserRequest.class,
            CreateUserResponse.class
    ),

    LOGIN(
            "/auth/login",
            LoginUserRequest.class,
            LoginUserResponse.class
    ),

    ACCOUNTS(
            "/accounts",
            BaseModel.class,
            CreateAccountResponse.class
    ),

    CHANGE_USER_NAME(
            "/customer/profile",
            ChangeUserNameRequest.class,
            ChangeUserNameResponse.class
    ),

    DEPOSIT_MONEY(
            "/accounts/deposit",
            DepositMoneyRequest.class,
            BaseModel.class
    ),

    TRANSFER_MONEY(
            "/accounts/transfer",
            TransferMoneyRequest.class,
            BaseModel.class
    ),

    PROFILE(
            "/customer/profile",
            BaseModel.class,
            CreateUserResponse.class
    ),

    CUSTOMER_ACCOUNTS(
            "/customer/accounts",
            BaseModel.class,
            CreateUserResponse.class
    );


    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}
