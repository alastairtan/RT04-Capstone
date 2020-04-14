// General
export const GET_ERRORS = "GET_ERRORS";
export const CLEAR_ERRORS = "CLEAR_ERRORS";
export const UPDATE_ERRORS = "UPDATE ERRORS";

// Login
export const RETRIEVE_STORE_LOGIN = "RETRIEVE_STORE_LOGIN";

// Store
export const CREATE_STORE = "CREATE_STORE";
export const RETRIEVE_STORE = "RETRIEVE_STORE";
export const UPDATE_STORE = "UPDATE_STORE";
export const RETRIEVE_ALL_STORES = "RETRIEVE_ALL_STORES";
export const DELETE_STORE = "DELETE_STORE";
export const CLEAR_CURRENT_STORE = "CLEAR_CURRENT_STORE";

// Product
export const CREATE_PRODUCT = "CREATE_PRODUCT";
export const RETRIEVE_PRODUCT_BY_ID = "RETRIEVE_PRODUCT_BY_ID";
export const RETRIEVE_ALL_PRODUCTS = "RETRIEVE_ALL_PRODUCTS";
export const RETRIEVE_PRODUCTS_DETAILS = "RETRIEVE_PRODUCTS_DETAILS";

// Tag
export const CREATE_TAG = "CREATE_TAG";
export const RETRIEVE_ALL_TAGS = "RETRIEVE_ALL_TAGS";
export const RETRIEVE_TAG = "RETRIEVE_TAG";
export const UPDATE_TAG = "UPDATE_TAG";
export const DELETE_TAG = "DELETE_TAG";
export const ADD_TAG_TO_PRODUCTS = "ADD_TAG_TO_PRODUCTS";
export const DELETE_TAG_FROM_PRODUCTS = "DELETE_TAG_FROM_PRODUCTS";

// Style
export const CREATE_STYLE = "CREATE_STYLE";
export const RETRIEVE_ALL_STYLES = "RETRIEVE_ALL_STYLES";
export const RETRIEVE_STYLE = "RETRIEVE_STYLE";
export const UPDATE_STYLE = "UPDATE_STYLE";
export const DELETE_STYLE = "DELETE_STYLE";
export const ADD_STYLE_TO_PRODUCTS = "ADD_STYLE_TO_PRODUCTS";
export const DELETE_STYLE_FROM_PRODUCTS = "DELETE_STYLE_FROM_PRODUCTS";

// Category
export const RETRIEVE_ALL_ROOT_CATEGORIES = "RETRIEVE_ALL_ROOT_CATEGORIES";
export const RETRIEVE_ALL_CATEGORIES = "RETRIEVE_ALL_CATEGORIES";
export const RETRIEVE_ALL_PRODUCTS_FOR_CATEGORY =
  "RETRIEVE_ALL_PRODUCTS_FOR_CATEGORY";
export const DELETE_CATEGORY = "DELETE_CATEGORY";

// Staff
export const CHANGE_STAFF_PASSWORD = "CHANGE_STAFF_PASSWORD";
export const STAFF_LOGIN = "STAFF_LOGIN";
export const STAFF_LOGOUT = "STAFF_LOGOUT";
export const UPDATE_STAFF = "UPDATE_STAFF";
export const RETRIEVE_ROLE = "RETRIEVE_ROLE";
export const RETRIEVE_DEPARTMENT = "RETRIEVE_DEPARTMENT";
export const RETRIEVE_ADDRESS = "RETRIEVE_ADDRESS";
//For HR only
export const RETRIEVE_ALL_STAFF = "RETRIEVE_ALL_STAFF";
export const CREATE_STAFF = "CREATE_STAFF";
export const DELETE_STAFF = "DELETE_STAFF";
export const RETRIEVE_STAFF = "RETRIEVE_STAFF";
export const RETRIEVE_ALL_ROLES = "RETRIEVE_ALL_ROLES";
export const RETRIEVE_ALL_DEPARTMENTS = "RETRIEVE_ALL_DEPARTMENTS";
export const CLEAR_CURRENT_STAFF = "CLEAR_CURRENT_STAFF";
export const REASSIGN_STAFF_STORE = "REASSIGN_STAFF_STORE";
export const RETRIEVE_ALL_STORE_STAFF = "RETRIEVE_ALL_STORE_STAFF";
export const RETRIEVE_STAFF_OF_STORE = "RETRIEVE_STAFF_OF_STORE";
//For admin/IT only
export const CREATE_STAFF_ACCOUNT = "CREATE_STAFF_ACCOUNT";
export const RETRIEVE_STAFF_WITH_NO_ACCOUNT = "RETRIEVE_STAFF_WITH_NO_ACCOUNT";
export const RESET_STAFF_PASSWORD = "RESET_STAFF_PASSWORD";
// ProductStock
export const UPDATE_PRODUCT_STOCK = "UPDATE_PRODUCT_STOCK";
export const UPDATE_PRODUCT_STOCK_QTY = "UPDATE_PRODUCT_STOCK_QTY";
export const RETRIEVE_PRODUCT_STOCKS_BY_PARAMETER =
  "RETRIEVE_PRODUCT_STOCKS_BY_PARAMETER";
export const SIMULATE_REORDERING_FROM_SUPPLIER =
  "SIMULATE_REORDERING_FROM_SUPPLIER";

// FEEDBACK
export const RETRIEVE_ALL_FEEDBACK = "RETRIEVE_ALL_FEEDBACK";

// REVIEW
export const RETRIEVE_ALL_REVIEWS = "RETRIEVE_ALL_REVIEWS";
export const DELETE_REVIEW = "DELETE_REVIEW";

// REFUND
export const RETRIEVE_ALL_REFUND_MODE_ENUM_SUCCESS =
  "RETRIEVE_ALL_REFUND_MODE_ENUM_SUCCESS";
export const RETRIEVE_ALL_REFUND_STATUS_ENUM_SUCCESS =
  "RETRIEVE_ALL_REFUND_STATUS_ENUM_SUCCESS";
export const CREATE_IN_STORE_REFUND_RECORD = "CREATE_IN_STORE_REFUND_RECORD";
export const CREATE_ONLINE_REFUND_RECORD = "CREATE_ONLINE_REFUND_RECORD";
export const UPDATE_REFUND_RECORD = "UPDATE_REFUND_RECORD";
export const RETRIEVE_REFUND_BY_ID = "RETRIEVE_REFUND_BY_ID";
export const RETRIEVE_ALL_REFUNDS = "RETRIEVE_ALL_REFUNDS";
export const RETRIEVE_ALL_REFUND_PROGRESS_ENUM_SUCCESS =
  "RETRIEVE_ALL_REFUND_PROGRESS_ENUM_SUCCESS";

// TRANSACTION
export const RETRIEVE_TRANSACTION_BY_ORDER_NUMBER_SUCCESS =
  "RETRIEVE_TRANSACTION_BY_ORDER_NUMBER_SUCCESS";

// UTIL
export const OPEN_CIRCULAR_PROGRESS = "OPEN_CIRCULAR_PROGRESS";
export const CLOSE_CIRCULAR_PROGRESS = "CLOSE_CIRCULAR_PROGRESS";

// RESTOCK ORDER
export const RETRIEVE_ALL_RESTOCK_ORDER = "RETRIEVE_ALL_RESTOCK_ORDER";

// DELIVERY
export const RETRIEVE_ALL_RESTOCK_ORDER_ITEM_TO_DELIVER =
  "RETRIEVE_ALL_RESTOCK_ORDER_ITEM_TO_DELIVER";
export const RETRIEVE_ALL_DELIVERY = "RETRIEVE_ALL_DELIVERY";

// ADVERTISEMENT
export const RETRIEVE_ALL_ADVERTISEMENT = "RETRIEVE_ALL_ADVERTISEMENT";
export const RETRIEVE_ALL_INSTAGRAM_POST = "RETRIEVE_ALL_INSTAGRAM_POST";

//PROMO CODE
export const RETRIEVE_ALL_PROMOCODES = "RETRIEVE_ALL_PROMOCODES";
export const CREATE_PROMOCODE = "CREATE_PROMOCODE";
export const DELETE_PROMOCODE = "DELETE_PROMOCODE";
export const UPDATE_PROMOCODE = "UPDATE_PROMOCODE";
export const RETRIEVE_PROMOCODE = "RETRIEVE_PROMOCODE";

//Leave
export const APPLY_FOR_LEAVE = "APPLY_FOR_LEAVE";
export const DELETE_LEAVE = "DELETE_LEAVE";
export const UPDATE_LEAVE = "UPDATE_LEAVE";
export const RETRIEVE_ALL_LEAVES = "RETRIEVE_ALL_LEAVES";
export const RETRIEVE_ALL_LEAVES_MANAGER = "RETRIEVE_ALL_LEAVES_MANAGER";
export const RETRIEVE_ALL_PENDING_LEAVES = "RETRIEVE_ALL_PENDING_LEAVES";
export const RETRIEVE_ALL_ENDORSED_LEAVES = "RETRIEVE_ALL_ENDORSED_LEAVES";
export const RETRIEVE_ALL_LEAVES_HR = "RETRIEVE_ALL_LEAVES_HR";
export const ENDORSE_REJECT_LEAVE = "ENDORSE_REJECT_LEAVE";
export const APPROVE_REJECT_LEAVE = "APPROVE_REJECT_LEAVE";
export const RETRIEVE_LEAVE_COUNT_IN_A_MONTH =
  "RETRIEVE_LEAVE_COUNT_IN_A_MONTH";
export const RETRIEVE_PAYROLLS_FOR_A_MONTH = "RETRIEVE_PAYROLLS_FOR_A_MONTH";
export const RETRIEVE_ALL_PAYROLLS = "RETRIEVE_ALL_PAYROLLS";
export const UPDATE_PAYROLL_STATUS = "UPDATE_PAYROLL_STATUS";

//Salary
export const CALCULATE_MONTHLY_SALARY = "CALCULATE_MONTHLY_SALARY";
export const CREATE_PAYROLLS = "CREATE_PAYROLLS";

//PROMO CODE
export const RETRIEVE_ALL_DISCOUNT = "RETRIEVE_ALL_DISCOUNT";
export const RETRIEVE_DISCOUNT_BY_ID = "RETRIEVE_DISCOUNT_BY_ID";

// TRANSACTION
export const RETRIEVE_TRANSACTIONS = "RETRIEVE_TRANSACTIONS";

// DASHBOARD
export const RETRIEVE_MARKET_BASKET_ANALYSIS =
  "RETRIEVE_MARKET_BASKET_ANALYSIS";

// ANALYTICS
export const RETRIEVE_SALES_BY_DAY = "RETRIEVE_SALES_BY_DAY";
