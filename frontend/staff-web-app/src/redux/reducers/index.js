import { combineReducers } from "redux";
import sidebarReducer from "./sidebarReducer";
import storeReducer from "./storeReducer";
import errorReducer from "./errorReducer";
import tagReducer from "./tagReducer";
import productReducer from "./productReducer";
import productStockReducer from "./productStockReducer";
import categoryReducer from "./categoryReducer";
import staffReducer from "./staffReducer";
import feedbackReducer from "./feedbackReducer";
import utilReducer from "./utilReducer";
import reviewReducer from "./reviewReducer";
import restockOrderReducer from "./restockOrderReducer";

const rootReducer = combineReducers({
  errors: errorReducer,
  sidebar: sidebarReducer,
  storeEntity: storeReducer,
  tag: tagReducer,
  product: productReducer,
  category: categoryReducer,
  staffEntity: staffReducer,
  productStock: productStockReducer,
  feedback: feedbackReducer,
  reviewEntity: reviewReducer,
  restockOrder: restockOrderReducer,
  util: utilReducer
});

export default rootReducer;
