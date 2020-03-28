import { combineReducers } from "redux";
import sidebarReducer from "./sidebarReducer";
import storeReducer from "./storeReducer";
import errorReducer from "./errorReducer";
import tagReducer from "./tagReducer";
import promoCodeReducer from "./promoCodeReducer";
import productReducer from "./productReducer";
import productStockReducer from "./productStockReducer";
import categoryReducer from "./categoryReducer";
import staffReducer from "./staffReducer";
import feedbackReducer from "./feedbackReducer";
import utilReducer from "./utilReducer";
import reviewReducer from "./reviewReducer";
import restockOrderReducer from "./restockOrderReducer";
import deliveryReducer from "./deliveryReducer";
import advertisementReducer from "./advertisementReducer";
import instagramReducer from "./instagramReducer";
import discountReducer from "./discountReducer";
import transactionReducer from "./transactionReducer";

const rootReducer = combineReducers({
  errors: errorReducer,
  sidebar: sidebarReducer,
  storeEntity: storeReducer,
  tag: tagReducer,
  promoCode: promoCodeReducer,
  product: productReducer,
  category: categoryReducer,
  staffEntity: staffReducer,
  productStock: productStockReducer,
  feedback: feedbackReducer,
  reviewEntity: reviewReducer,
  restockOrder: restockOrderReducer,
  delivery: deliveryReducer,
  advertisement: advertisementReducer,
  instagram: instagramReducer,
  discount: discountReducer,
  transaction: transactionReducer,
  util: utilReducer
});

export default rootReducer;
