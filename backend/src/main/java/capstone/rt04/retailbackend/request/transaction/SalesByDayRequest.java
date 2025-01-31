package capstone.rt04.retailbackend.request.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SalesByDayRequest {

    private String fromDateString;

    private String toDateString;

    private List<Long> fromStoreIds;

    private Boolean onlineSelected;
}
