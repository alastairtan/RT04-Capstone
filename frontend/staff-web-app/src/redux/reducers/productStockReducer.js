import * as types from "../actions/types";

const initialState = {
  currentProductStock: null,
  allProductStock: null,
  simulateOrderProductStocks: null,
  crudAction: null
};

export default function(state = initialState, action) {
  switch (action.type) {
    case types.UPDATE_PRODUCT_STOCK:
      return {
        ...state,
        currentProductStock: action.productStockEntity
      };
    case types.UPDATE_PRODUCT_STOCK_QTY:
      return {
        ...state,
        currentProductStock: action.productStockEntity
      };
    case types.RETRIEVE_PRODUCT_STOCKS_BY_PARAMETER:
      return {
        ...state,
        allProductStock: action.productStockEntities
      };
    case types.SIMULATE_REORDERING_FROM_SUPPLIER:
      return {
        ...state,
        simulateOrderProductStocks: action.simulateOrderProductStocks
      }
    default:
      return state;
  }
}
