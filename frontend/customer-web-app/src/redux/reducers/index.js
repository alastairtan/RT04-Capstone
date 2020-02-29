import { combineReducers } from "redux";
import customerReducer from "redux/reducers/customerReducer";
import errorReducer from "redux/reducers/errorReducer";
import contactUsReducer from "./contactUsReducer";
import categoryReducer from "redux/reducers/categoryReducer";
import productReducer from "redux/reducers/productReducer";
import tagReducer from "redux/reducers/tagReducer";

const rootReducer = combineReducers({
  customer: customerReducer,
  contactUs: contactUsReducer,
  errors: errorReducer,
  category: categoryReducer,
  product: productReducer,
  tag: tagReducer
});

export default rootReducer;
