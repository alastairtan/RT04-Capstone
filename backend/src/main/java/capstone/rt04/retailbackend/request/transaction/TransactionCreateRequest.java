package capstone.rt04.retailbackend.request.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionCreateRequest {

    private Long customerId;

    private Long shoppingCartId;

    private String cartType;
}
