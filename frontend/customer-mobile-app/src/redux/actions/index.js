import * as types from "./types";
import { GET_ERRORS } from "./types";

const _ = require("lodash");

export const clearErrors = () => ({
  type: types.CLEAR_ERRORS
});

export const errorMapError = data => ({
  type: GET_ERRORS,
  errorMap: data
});

export const dispatchErrorMapError = (err, dispatch) => {
  const errorMap = _.get(err, "response.data", null);
  if (errorMap) {
    dispatch(errorMapError(errorMap));
  } else {
    console.log(err);
  }
};
