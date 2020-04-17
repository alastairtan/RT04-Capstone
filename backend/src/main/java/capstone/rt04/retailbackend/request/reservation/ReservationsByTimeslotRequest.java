package capstone.rt04.retailbackend.request.reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReservationsByTimeslotRequest {

    private String fromDateString;

    private String toDateString;

    private List<Long> storeIds;

}
