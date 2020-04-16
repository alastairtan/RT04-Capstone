import * as types from "../actions/types";

const initialState = {
  displayedProductDetails: null,
  currentProductDetail: null,
  storeForRecommendation: [],
};

export default function(state = initialState, action) {
  switch (action.type) {
    case types.DISPLAY_PRODUCTS:
      return {
        ...state,
        displayedProductDetails: action.products,
      };
    case types.VIEW_SINGLE_PRODUCT:
      return {
        ...state,
        currentProductDetail: action.product,
      };
    case types.VIEW_ELIGIBLE_STORE_RECOMMENDATION:
      return {
        ...state,
        storeForRecommendation: action.storeForRecommendation,
      };
    default:
      return state;
  }
}
