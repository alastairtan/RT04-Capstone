/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package capstone.rt04.retailbackend.entities;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author shawn
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class ProductVariant implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long productVariantId;
    
    @NotNull
    @Column(nullable = false)
    private String SKU;
    
    @NotNull
    @Column(nullable = false)
    private String colour;
        
    @OneToMany
    @Size(min = 1)
    private List<ProductImage> productImages;
    
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Product product;
    
    @OneToOne
    private SizeDetails sizeDetails;

    public ProductVariant() {
    }

    public ProductVariant(String SKU, String colour, List<ProductImage> productImages, Product product, SizeDetails sizeDetails) {
        this.SKU = SKU;
        this.colour = colour;
        this.productImages = productImages;
        this.product = product;
        this.sizeDetails = sizeDetails;
    }

}
