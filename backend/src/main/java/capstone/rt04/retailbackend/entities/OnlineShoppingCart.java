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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
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
public class OnlineShoppingCart implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long onlineShoppingCartId;
    
    @NotNull
    @Column(nullable = false)
    private BigDecimal initialTotalAmount;
    
    private BigDecimal discountedTotalAmount;
    
    @OneToMany
    private List<OnlineShoppingCartItem> onlineShoppingCartItems;
    
    @OneToOne(mappedBy = "onlineShoppingCart")
    @NotNull
    private Customer customer;

    public OnlineShoppingCart() {
        this.onlineShoppingCartItems = new ArrayList<>();
    }
    
    

    
}
