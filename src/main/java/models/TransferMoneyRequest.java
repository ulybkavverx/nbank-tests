package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferMoneyRequest extends BaseModel {

    // id акка, с которого будут списываться деньги
    private int senderAccountId;

    // id акка, на который будут зачисляться деньги
    private int receiverAccountId;

    // сумма перевода
    private double amount;
}
