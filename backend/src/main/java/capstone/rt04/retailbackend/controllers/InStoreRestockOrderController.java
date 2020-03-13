package capstone.rt04.retailbackend.controllers;

import capstone.rt04.retailbackend.entities.*;
import capstone.rt04.retailbackend.request.inStoreRestockOrder.RestockCreateRequest;
import capstone.rt04.retailbackend.request.inStoreRestockOrder.RestockUpdateRequest;
import capstone.rt04.retailbackend.response.InStoreRestockOrderForWarehouse;
import capstone.rt04.retailbackend.response.InStoreRestockOrderItemsForWarehouse;
import capstone.rt04.retailbackend.services.InStoreRestockOrderService;
import capstone.rt04.retailbackend.services.ValidationService;
import capstone.rt04.retailbackend.util.exceptions.inStoreRestockOrder.InStoreRestockOrderNotFoundException;
import capstone.rt04.retailbackend.util.exceptions.inStoreRestockOrder.InStoreRestockOrderUpdateException;
import capstone.rt04.retailbackend.util.exceptions.inStoreRestockOrder.InsufficientStockException;
import capstone.rt04.retailbackend.util.exceptions.product.ProductStockNotFoundException;
import capstone.rt04.retailbackend.util.exceptions.product.ProductVariantNotFoundException;
import capstone.rt04.retailbackend.util.exceptions.store.StoreNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static capstone.rt04.retailbackend.util.routeconstants.InStoreRestockOrderControllerRoutes.*;

@RestController
@RequestMapping(IN_STORE_RESTOCK_ORDER_BASE_ROUTE)
@CrossOrigin(origins = {"http://localhost:3000"})

public class InStoreRestockOrderController {

    private final InStoreRestockOrderService inStoreRestockOrderService;
    private final ValidationService validationService;


    public InStoreRestockOrderController(InStoreRestockOrderService inStoreRestockOrderService, ValidationService validationService) {
        this.inStoreRestockOrderService = inStoreRestockOrderService;
        this.validationService = validationService;
    }

    @GetMapping(RETRIEVE_ALL_IN_STORE_RESTOCK_ORDER)
    public ResponseEntity<?> retrieveAllRestockOrder(@RequestParam(required=false) Long storeId) {
        if (storeId != null) {
            List<InStoreRestockOrder> inStoreRestockOrders = inStoreRestockOrderService.retrieveAllInStoreRestockOrder(storeId);
            clearRestockOrderRelationship(inStoreRestockOrders);
            return new ResponseEntity<>(inStoreRestockOrders, HttpStatus.OK);
        } else {
            List<InStoreRestockOrderForWarehouse> inStoreRestockOrderForWarehouse = inStoreRestockOrderService.retrieveAllInStoreRestockOrderForWarehouse();
            clearRestockOrderForWarehouseRelationship(inStoreRestockOrderForWarehouse);
            return new ResponseEntity<>(inStoreRestockOrderForWarehouse, HttpStatus.OK);
        }
    }

    @PostMapping(CREATE_IN_STORE_RESTOCK_ORDER)
    public ResponseEntity<?> createRestockOrder(@RequestBody RestockCreateRequest restockCreateRequest) throws StoreNotFoundException, ProductStockNotFoundException {
        List<InStoreRestockOrder> inStoreRestockOrders = inStoreRestockOrderService.createInStoreRestockOrder(restockCreateRequest.getStoreId(),
                restockCreateRequest.getStockIdQuantityMaps());
        clearRestockOrderRelationship(inStoreRestockOrders);
        return new ResponseEntity<>(inStoreRestockOrders, HttpStatus.CREATED);
    }

    @PostMapping(UPDATE_IN_STORE_RESTOCK_ORDER)
    public ResponseEntity<?> updateRestockOrder(@RequestBody RestockUpdateRequest restockUpdateRequest) throws ProductStockNotFoundException, InStoreRestockOrderNotFoundException, ProductVariantNotFoundException, InStoreRestockOrderUpdateException {
        List<InStoreRestockOrder> inStoreRestockOrders = inStoreRestockOrderService.updateRestockOrder(restockUpdateRequest.getRestockOrderId(),
                restockUpdateRequest.getStockIdQuantityMaps());
        clearRestockOrderRelationship(inStoreRestockOrders);
        return new ResponseEntity<>(inStoreRestockOrders, HttpStatus.OK);
    }

    @GetMapping(FULFILL_IN_STORE_RESTOCK_ORDER)
    public ResponseEntity<?> fulfillRestockOrder(@PathVariable Long inStoreRestockOrderId) throws InStoreRestockOrderNotFoundException, InsufficientStockException {
        List<InStoreRestockOrder> inStoreRestockOrders = inStoreRestockOrderService.fulfillRestockOrder(inStoreRestockOrderId);
        clearRestockOrderRelationship(inStoreRestockOrders);
        return new ResponseEntity<>(inStoreRestockOrders, HttpStatus.OK);
    }

    @GetMapping(RECEIVE_STOCK)
    public ResponseEntity<?> receiveStock(@PathVariable Long inStoreRestockOrderId) throws InStoreRestockOrderNotFoundException {
        List<InStoreRestockOrder> inStoreRestockOrders = inStoreRestockOrderService.receiveStock(inStoreRestockOrderId);
        clearRestockOrderRelationship(inStoreRestockOrders);
        return new ResponseEntity<>(inStoreRestockOrders, HttpStatus.OK);
    }

    @DeleteMapping(DELETE_IN_STORE_RESTOCK_ORDER)
    public ResponseEntity<?> deleteRestockOrder(@PathVariable Long inStoreRestockOrderId) throws InStoreRestockOrderNotFoundException, InStoreRestockOrderUpdateException {
        List<InStoreRestockOrder> inStoreRestockOrders = inStoreRestockOrderService.deleteInStoreRestockOrder(inStoreRestockOrderId);
        clearRestockOrderRelationship(inStoreRestockOrders);
        return new ResponseEntity<>(inStoreRestockOrders, HttpStatus.OK);
    }

    private void clearRestockOrderRelationship(List<InStoreRestockOrder> inStoreRestockOrders) {
        for (InStoreRestockOrder inStoreRestockOrder : inStoreRestockOrders) {
            // Warehouse
            inStoreRestockOrder.getWarehouse().setProductStocks(null);
            inStoreRestockOrder.getWarehouse().setInStoreRestockOrders(null);

            // Store
            clearStore(inStoreRestockOrder.getStore());

            // Product stock
            for (InStoreRestockOrderItem inStoreRestockOrderItem : inStoreRestockOrder.getInStoreRestockOrderItems()) {
                inStoreRestockOrderItem.getProductStock().setStore(null);
                inStoreRestockOrderItem.getProductStock().setWarehouse(null);
                // Product variant
                ProductVariant productVariant = inStoreRestockOrderItem.getProductStock().getProductVariant();
                clearProductVariant(productVariant);
            }
        }
    }

    private void clearRestockOrderForWarehouseRelationship(List<InStoreRestockOrderForWarehouse> inStoreRestockOrderForWarehouses ) {
        for (InStoreRestockOrderForWarehouse inStoreRestockOrderForWarehouse : inStoreRestockOrderForWarehouses) {
            // Warehouse
            inStoreRestockOrderForWarehouse.getWarehouse().setProductStocks(null);
            inStoreRestockOrderForWarehouse.getWarehouse().setInStoreRestockOrders(null);

            // Store
            clearStore(inStoreRestockOrderForWarehouse.getStore());

            // Product stock
            for (InStoreRestockOrderItemsForWarehouse inStoreRestockOrderItemsForWarehouse : inStoreRestockOrderForWarehouse.getInStoreRestockOrderItemsForWarehouse()) {
                inStoreRestockOrderItemsForWarehouse.getProductStock().setStore(null);
                inStoreRestockOrderItemsForWarehouse.getProductStock().setWarehouse(null);
                // Product variant
                ProductVariant productVariant = inStoreRestockOrderItemsForWarehouse.getProductStock().getProductVariant();
                clearProductVariant(productVariant);
            }
        }
    }

    private void clearProductVariant(ProductVariant productVariant) {
        productVariant.setProductStocks(null);
        // Product
        Product product = productVariant.getProduct();
        product.setStyles(null);
        product.setReviews(null);
        product.setCategory(null);
        product.setTags(null);
        product.setPromoCodes(null);
        product.setDiscounts(null);
        product.setProductVariants(null);
    }

    private void clearStore(Store store) {
        store.setProductStocks(null);
        store.setReservations(null);
        store.setInStoreRestockOrders(null);
        store.setTransactions(null);
        store.setStaff(null);
    }
}
