package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepositMoneyRequest extends BaseModel {

    // id аккаунта, на который зачисляем деньги
    private int id;

    // сумма пополнения
    private double balance;
}
