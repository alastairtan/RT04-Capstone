package capstone.rt04.retailbackend.request.style;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateStyleRequest {
    private String styleName;

    private List<StyleIdAnswerMap> styleIdAnswerMaps;
}
