/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package capstone.rt04.retailbackend.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.DecimalMin;
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
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long productId;
    
    @NotNull
    @Column(nullable = false)
    private String productName;
    
    @NotNull
    @Column(nullable = false, columnDefinition = "VARCHAR(1337)")
    @Size(max = 1337)
    private String description;
    
    @NotNull
    @Column(nullable = false, precision = 11, scale = 2)
    @DecimalMin("0.00")
    private BigDecimal price;
    
    @NotNull
    @Column(nullable = false, precision = 11, scale = 2)
    @DecimalMin("0.00")
    private BigDecimal cost;
    
    @ManyToMany(mappedBy = "products")
    private List<Discount> discounts;
    
    @ManyToMany(mappedBy = "products")
    private List<PromoCode> promoCodes;
    
    @ManyToMany(mappedBy = "products")
    private List<Tag> tags;
    
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    @NotNull
    private Category category;
    
    @OneToMany(mappedBy = "product")
    private List<Review> reviews;
    
    @OneToMany(mappedBy = "product")
    private List<ProductVariant> productVariants;
    
    public Product() {
        this.discounts = new ArrayList<>();
        this.tags = new ArrayList<>();
    }

    public Product(String productName, String description, BigDecimal price, BigDecimal cost) {
        this();
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.cost = cost;
    }
    
    public void addTag(Tag tag)
    {
        if(tag != null)
        {
            if(!this.tags.contains(tag))
            {
                this.tags.add(tag);
                
                if(!tag.getProducts().contains(this))
                {                    
                    tag.getProducts().add(this);
                }
            }
        }
    }
    
}
