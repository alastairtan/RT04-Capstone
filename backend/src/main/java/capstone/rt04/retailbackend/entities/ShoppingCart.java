/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package capstone.rt04.retailbackend.entities;

import capstone.rt04.retailbackend.services.ProductService;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.voodoodyne.jackson.jsog.JSOGGenerator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shawn
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode
// @ToString()
@JsonIdentityInfo(generator = JSOGGenerator.class)
public class ShoppingCart implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long shoppingCartId;

    @NotNull
    @Column(nullable = false, precision = 11, scale = 2)
    private BigDecimal initialTotalAmount;

    @Column(precision = 11, scale = 2)
    private BigDecimal finalTotalAmount;

    private Timestamp lastUpdated;

    @OneToMany
    private List<ShoppingCartItem> shoppingCartItems;

    @OneToOne
    private Store store; //to track which store the in-store shopping cart is for

    public ShoppingCart() {
        this.initialTotalAmount = BigDecimal.ZERO;
        this.finalTotalAmount = BigDecimal.ZERO;
        this.shoppingCartItems = new ArrayList<>();
    }

    public void calculateAndSetInitialTotal(ProductService productService) {
        BigDecimal initialTotal = BigDecimal.ZERO;
        BigDecimal finalTotal = BigDecimal.ZERO;
        BigDecimal discountedPrice;
        BigDecimal subTotal;
        BigDecimal quantity;

        for (ShoppingCartItem item : this.getShoppingCartItems()) {
            quantity = new BigDecimal(item.getQuantity());
            discountedPrice = null;

            for(Discount discount : item.getProductVariant().getProduct().getDiscounts()) {
                discountedPrice = productService.applyDiscount(discount, item.getProductVariant().getProduct(), null);
                if (discountedPrice != null) break;
            }
            BigDecimal unitPrice = item.getProductVariant().getProduct().getPrice();
            subTotal = unitPrice.multiply(quantity).setScale(2, BigDecimal.ROUND_HALF_UP);
            // original total with non-discounted price
            initialTotal = initialTotal.add(subTotal).setScale(2, BigDecimal.ROUND_HALF_UP);

            if (discountedPrice != null) unitPrice = discountedPrice;

            subTotal = unitPrice.multiply(quantity).setScale(2, BigDecimal.ROUND_HALF_UP);
            // total with discounted price
            finalTotal = finalTotal.add(subTotal).setScale(2, BigDecimal.ROUND_HALF_UP);
        }

        this.setInitialTotalAmount(initialTotal);
        this.setFinalTotalAmount(finalTotal);
    }


}
