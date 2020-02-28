package capstone.rt04.retailbackend.request.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ColourToImageUrlsMap {

    private String colour;

    private List<String> imageUrls;

}
