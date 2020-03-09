import * as types from "../actions/types";

const initialState = {
  loggedInCustomer: null,
  isSendingEmail: false,
  verificationStatus: null,
  clientSecret: null,
  shoppingCartTooltipOpen: false,
  wishlistTooltipOpen: false,
  reservationTooltipOpen: false
};

export default function(state = initialState, action) {
  switch (action.type) {
    case types.CREATE_NEW_CUSTOMER:
      return state;
    case types.CUSTOMER_LOGIN:
      return {
        ...state,
        loggedInCustomer: action.customer
      };
    case types.VERIFY_SUCCESS:
      return {
        ...state,
        verificationStatus: "SUCCESS"
      };
    case types.VERIFY_FAILURE:
      return {
        ...state,
        verificationStatus: "FAILURE"
      };
    case types.RESET_VERIFICATION_STATUS:
      return {
        ...state,
        verificationStatus: null
      };
    case types.CUSTOMER_LOGOUT:
      return initialState;
    case types.EMAIL_SENDING:
      return {
        ...state,
        isSendingEmail: true
      };
    case types.EMAIL_SENT:
      return {
        ...state,
        isSendingEmail: false
      };
    case types.UPDATE_CUSTOMER:
      return {
        ...state,
        loggedInCustomer: action.customer
      };
    case types.UPDATE_SHIPPING_ADDRESS_SUCCESS:
      return {
        ...state,
        loggedInCustomer: action.loggedInCustomer
      };
    case types.ADD_SHIPPING_ADDRESS_SUCCESS:
      return {
        ...state,
        loggedInCustomer: action.loggedInCustomer
      };
    case types.REMOVE_SHIPPING_ADDRESS_SUCCESS:
      return {
        ...state,
        loggedInCustomer: action.loggedInCustomer
      };
    case types.UPDATE_SHOPPING_CART_SUCCESS:
      return {
        ...state,
        loggedInCustomer: action.customer
      };
    case types.PAYMENT_SUCCESS:
      return {
        ...state,
        clientSecret: action.clientSecret
      };
    case types.CART_TOOLTIP_OPEN:
      return {
        ...state,
        shoppingCartTooltipOpen: true
      };
    case types.CART_TOOLTIP_CLOSE:
      return {
        ...state,
        shoppingCartTooltipOpen: false
      };
    case types.WISHLIST_TOOLTIP_OPEN:
      return {
        ...state,
        wishlistTooltipOpen: true
      };
    case types.WISHLIST_TOOLTIP_CLOSE:
      return {
        ...state,
        wishlistTooltipOpen: false
      };
    case types.RESERVATION_TOOLTIP_OPEN:
      return {
        ...state,
        reservationTooltipOpen: true
      };
    case types.RESERVATION_TOOLTIP_CLOSE:
      return {
        ...state,
        reservationTooltipOpen: false
      };
    default:
      return state;
  }
}
