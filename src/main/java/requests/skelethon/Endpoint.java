package requests.skelethon;

import models.ChangeUserNameRequest;
import models.ChangeUserNameResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import models.*;

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
    );


    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}
