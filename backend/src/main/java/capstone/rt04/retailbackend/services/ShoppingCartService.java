package capstone.rt04.retailbackend.services;

import capstone.rt04.retailbackend.entities.*;
import capstone.rt04.retailbackend.repositories.ShoppingCartItemRepository;
import capstone.rt04.retailbackend.repositories.ShoppingCartRepository;
import capstone.rt04.retailbackend.util.exceptions.customer.CustomerNotFoundException;
import capstone.rt04.retailbackend.util.exceptions.product.ProductVariantNotFoundException;

import static capstone.rt04.retailbackend.util.Constants.*;

import capstone.rt04.retailbackend.util.exceptions.shoppingcart.InvalidCartTypeException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ShoppingCartService {

    private final ProductService productService;
    private final CustomerService customerService;

    private final ShoppingCartRepository shoppingCartRepository;
    private final ShoppingCartItemRepository shoppingCartItemRepository;


    public ShoppingCartService(ProductService productService, @Lazy CustomerService customerService, ShoppingCartRepository shoppingCartRepository, ShoppingCartItemRepository shoppingCartItemRepository) {
        this.productService = productService;
        this.customerService = customerService;
        this.shoppingCartRepository = shoppingCartRepository;
        this.shoppingCartItemRepository = shoppingCartItemRepository;
    }

    public void initializeShoppingCarts(Long customerId) throws CustomerNotFoundException {
        Customer customer = customerService.retrieveCustomerByCustomerId(customerId);

        ShoppingCart inStoreShoppingCart = new ShoppingCart();
        inStoreShoppingCart.setCustomer(customer);
        shoppingCartRepository.save(inStoreShoppingCart);
        customer.setInStoreShoppingCart(inStoreShoppingCart);

        ShoppingCart onlineShoppingCart = new ShoppingCart();
        onlineShoppingCart.setCustomer(customer);
        shoppingCartRepository.save(onlineShoppingCart);
        customer.setOnlineShoppingCart(onlineShoppingCart);
    }

    private ShoppingCartItem createShoppingCartItem(Integer quantity, Long productVariantId) throws ProductVariantNotFoundException {
        ProductVariant productVariant = productService.retrieveProductVariant(productVariantId);
        ShoppingCartItem shoppingCartItem = new ShoppingCartItem(quantity, productVariant);
        return shoppingCartItemRepository.save(shoppingCartItem);
    }

    public ShoppingCart updateQuantityOfProductVariant(Integer quantity, Long productVariantId, Long customerId, String cartType) throws CustomerNotFoundException, ProductVariantNotFoundException, InvalidCartTypeException {
        ShoppingCart shoppingCart = getShoppingCart(customerId, cartType);
        for (ShoppingCartItem shoppingCartItem : shoppingCart.getShoppingCartItems()) {
            if (shoppingCartItem.getProductVariant().getProductVariantId().equals(productVariantId)) {
                if (quantity > 0) { //set to whatever input quantity
                    shoppingCartItem.setQuantity(quantity);
                    return shoppingCart;
                } else if (quantity == 0){ // delete
                    shoppingCartItem.setProductVariant(null);
                    shoppingCart.getShoppingCartItems().remove(shoppingCartItem);
                    shoppingCartItemRepository.delete(shoppingCartItem);
                    return shoppingCart;
                }
            }
        }
        // does not exist in card, so add 1 (first time adding)
        ShoppingCartItem shoppingCartItem = createShoppingCartItem(1, productVariantId);
        shoppingCart.getShoppingCartItems().add(shoppingCartItem);
        return shoppingCart;
    }

    public ShoppingCart getShoppingCart(Long customerId, String cartType) throws CustomerNotFoundException, InvalidCartTypeException {
        Customer customer = customerService.retrieveCustomerByCustomerId(customerId);
        if (cartType.equals(ONLINE_SHOPPING_CART)) {
            customer.getOnlineShoppingCart().getShoppingCartItems().size();
            return customer.getOnlineShoppingCart();
        } else if (cartType.equals(IN_STORE_SHOPPING_CART)) {
            customer.getOnlineShoppingCart().getShoppingCartItems().size();
            return customer.getInStoreShoppingCart();
        } else {
            throw new InvalidCartTypeException("Cart type is invalid! Must be either instore or online");
        }
    }


}
