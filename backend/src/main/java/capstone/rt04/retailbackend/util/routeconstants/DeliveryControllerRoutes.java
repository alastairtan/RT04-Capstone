package capstone.rt04.retailbackend.util.routeconstants;

public class DeliveryControllerRoutes {
    public static final String DELIVERY_BASE_ROUTE = "/api/delivery";

    // Delivery
    public static final String CREATE_DELIVERY_FOR_CUSTOMER_ORDER = "/createDeliveryForCustomerOrder";
    public static final String CREATE_DELIVERY_FOR_RESTOCK_ORDER = "/createDeliveryForRestockOrder";
    public static final String RETRIEVE_ALL_DELIVERY = "/retrieveAllDelivery";
    public static final String RECEIVE_RESTOCK_ORDER_ITEM_THROUGH_DELIVERY = "/receiveRestockOrderItemThroughDelivery";
    public static final String RETRIEVE_DELIVERY_BY_ID = "/retrieveDeliveryById/{deliveryId}";
    public static final String DELETE_DELIVERY = "/deleteDelivery/{deliveryId}";

    // Restock Order Item
    public static final String RETRIEVE_ALL_RESTOCK_ORDER_ITEM_TO_DELIVER = "/retrieveAllRestockOrderItemToDeliver";
}
