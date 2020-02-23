import { combineReducers } from "redux";
import sidebarReducer from "./sidebarReducer";
import storeReducer from "./storeReducer";
import errorReducer from "./errorReducer";
import tagReducer from "./tagReducer";
import productReducer from "./productReducer";
import categoryReducer from "./categoryReducer";
import staffReducer from "./staffReducer";

const rootReducer = combineReducers({
  errors: errorReducer,
  sidebar: sidebarReducer,
  storeEntity: storeReducer,
  tag: tagReducer,
  product: productReducer,
  category: categoryReducer,
  staffEntity: staffReducer
});

export default rootReducer;
