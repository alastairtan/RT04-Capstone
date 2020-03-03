import * as types from "../actions/types";

const initialState = {
  storesWithStockStatus: null,
  prodVariantToStoreStock: {},
  availSlotsForStore: null,
  upcomingReservations: null
};

export default function(state = initialState, action) {
  switch (action.type) {
    case types.GET_STORES_WITH_STOCK_STATUS:
      return {
        ...state,
        storesWithStockStatus: action.storesWithStockStatus
      };
    case types.GET_PROD_VAR_STORE_STOCK_STATUS:
      return {
        ...state,
        prodVariantToStoreStock: action.prodVariantToStock
      };
    case types.CLEAR_PROD_VAR_STORE_STOCK_STATUS:
      return {
        ...state,
        prodVariantToStoreStock: {}
      };
    case types.GET_AVAIL_SLOTS_FOR_STORE:
      return {
        ...state,
        availSlotsForStore: action.availSlotsForStore
      };
    case types.UPDATE_UPCOMING_RESERVATIONS:
      return {
        ...state,
        upcomingReservations: action.reservations
      };
    default:
      return state;
  }
}
