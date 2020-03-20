/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package capstone.rt04.retailbackend.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.voodoodyne.jackson.jsog.JSOGGenerator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author shawn
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode
// @ToString
@JsonIdentityInfo(generator = JSOGGenerator.class)
public class ProductImage implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long productImageId;

    @NotNull(message = "Product image URL must be provided")
    private String productImageUrl;

    @NotNull(message = "The position of the image must be specified")
    private Integer position;

    public ProductImage() {
    }

    public ProductImage(String productImageUrl, Integer position) {
        this.productImageUrl = productImageUrl;
        this.position = position;
    }

}
