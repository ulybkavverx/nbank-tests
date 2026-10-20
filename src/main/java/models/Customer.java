package models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// чтобы Jackson не выбрасывал ошибку, а пропускал поля, которых нет в моем классе(напр. accounts)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Customer extends BaseModel {
    private long id;
    private String username;
    private String password;
    private String name;
    private String role;
}
