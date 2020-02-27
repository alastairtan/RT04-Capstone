import axios from "axios";
import {
  CREATE_NEW_CUSTOMER,
  CUSTOMER_LOGIN,
  CUSTOMER_LOGOUT,
  EMAIL_SENDING,
  EMAIL_SENT,
  GET_ERRORS,
  RESET_VERIFICATION_STATUS,
  VERIFY_FAILURE,
  VERIFY_SUCCESS
} from "./types";
import { UPDATE_CUSTOMER } from "redux/actions/types";

const CUSTOMER_BASE_URL = "/api/customer";

const _ = require("lodash");
const jsog = require("jsog");

export const emailSending = () => ({
  type: EMAIL_SENDING
});

export const emailSent = () => ({
  type: EMAIL_SENT
});

const errorMapError = data => ({
  type: GET_ERRORS,
  errorMap: data
});

const dispatchErrorMapError = (err, dispatch) => {
  const errorMap = _.get(err, "response.data", null);
  if (errorMap) {
    dispatch(errorMapError(errorMap));
  } else {
    console.log(err);
  }
};

const dispatchUpdatedCustomer = (customerDataRaw, dispatch) => {
  const customer = jsog.decode(customerDataRaw);
  dispatch(updateCustomer(customer));
};

export const createNewCustomer = (createCustomerRequest, history) => {
  return dispatch => {
    //redux thunk passes dispatch
    axios
      .post(CUSTOMER_BASE_URL + "/createNewCustomer", createCustomerRequest)
      .then(response => {
        dispatch(emailSent());
        history.push("/account/verifyEmail");
      })
      .catch(err => {
        dispatch(emailSent());
        dispatchErrorMapError(err, dispatch);
        //console.log(err.response.data);
      });
  };
};

// const createCustomerSuccess = data => ({
//   type: CREATE_NEW_CUSTOMER,
//   customer: data
// });

export const customerLogin = (customerLoginRequest, history) => {
  return dispatch => {
    axios
      .post(CUSTOMER_BASE_URL + "/login", customerLoginRequest)
      .then(response => {
        dispatchUpdatedCustomer(response.data, dispatch);
        history.push("/");
      })
      .catch(err => {
        dispatchErrorMapError(err, dispatch);
      });
  };
};

// const customerLoginSuccess = data => ({
//   type: CUSTOMER_LOGIN,
//   customer: data
// });

export const customerLogout = () => ({
  type: CUSTOMER_LOGOUT
});

// bad request(400) if expired, not found(404) if invalid, or already verified
export const verify = (verificationCode, history) => {
  return dispatch => {
    axios
      .get(CUSTOMER_BASE_URL + `/verify/${verificationCode}`)
      .then(response => {
        console.log("VERIFY SUCCESS");
        const { data } = jsog.decode(response);
        dispatch(verificationSuccess());
        dispatchUpdatedCustomer(response.data, dispatch);
      })
      .catch(err => {
        if (err.response.status === 404) {
          console.log(err.response);
          history.push("/404");
        } else {
          const errorMap = _.get(err, "response.data", null);
          if (errorMap) {
            dispatch(verificationError(errorMap));
          } else {
            console.log(err);
          }
        }
      });
  };
};

const verificationSuccess = () => ({
  type: VERIFY_SUCCESS
});

const verificationError = () => ({
  type: VERIFY_FAILURE
});

export const resetVerificationStatus = () => ({
  type: RESET_VERIFICATION_STATUS
});

export const resendVerifyEmail = (customerEmailReq, history) => {
  return dispatch => {
    axios
      .post(CUSTOMER_BASE_URL + `/resendVerifyEmail`, customerEmailReq)
      .then(response => {
        dispatch(emailSent());
        history.push("/account/verifyEmail");
      })
      .catch(err => {
        dispatch(emailSent());
        dispatchErrorMapError(err, dispatch);
      });
  };
};

export const updateCustomerName = (updateCustomerReq, enqueueSnackbar) => {
  return dispatch => {
    axios
      .post(CUSTOMER_BASE_URL + "/updateCustomer", updateCustomerReq)
      .then(response => {
        dispatchUpdatedCustomer(response.data, dispatch);
        enqueueSnackbar("Changes saved", {
          variant: "success",
          autoHideDuration: 1200
        });
      })
      .catch(err => {
        dispatchErrorMapError(err, dispatch);
      });
  };
};

const updateCustomer = data => ({
  type: UPDATE_CUSTOMER,
  customer: data
});

export const sendUpdateEmailLink = (
  req,
  setDialogOpen,
  resetInputState,
  setChangingEmail
) => {
  return dispatch => {
    axios
      .post(CUSTOMER_BASE_URL + "/sendUpdateEmailLink", req)
      .then(response => {
        setTimeout(() => dispatch(emailSent()), 500);
        resetInputState();
        setChangingEmail(false);
        setDialogOpen(true);
      })
      .catch(err => {
        setTimeout(() => dispatch(emailSent()), 500);
        dispatchErrorMapError(err, dispatch);
      });
  };
};

// verify against database
export const updateEmail = (verificationCode, history) => {
  return dispatch => {
    axios
      .get(CUSTOMER_BASE_URL + `/updateEmail/${verificationCode}`)
      .then(response => {
        dispatch(customerLogout());
        dispatch(verificationSuccess());
      })
      .catch(err => {
        if (err.response.status === 404) {
          history.push("/404");
        } else {
          const errorMap = _.get(err, "response.data", null);
          if (errorMap) {
            dispatch(verificationError(errorMap));
          } else {
            console.log(err);
          }
        }
      });
  };
};

export const changePassword = (req, enqueueSnackbar, setChangingPassword) => {
  return dispatch => {
    axios
      .post(CUSTOMER_BASE_URL + `/changePassword`, req)
      .then(response => {
        dispatchUpdatedCustomer(response.data, dispatch);
        enqueueSnackbar("Password updated", {
          variant: "success",
          autoHideDuration: 1200
        });
        setChangingPassword(false);
      })
      .catch(err => {
        dispatchErrorMapError(err, dispatch);
      });
  };
};

export const sendResetPasswordLink = (req, setDialogOpen) => {
  return dispatch => {
    axios
      .post(CUSTOMER_BASE_URL + "/sendResetPasswordLink", req)
      .then(response => {
        dispatch(emailSent());
        setDialogOpen(true);
      })
      .catch(err => {
        dispatch(emailSent());
        dispatchErrorMapError(err, dispatch);
      });
  };
};

export const resetPassword = (req, setDialogOpen, setDialogText) => {
  return dispatch => {
    axios
      .post(CUSTOMER_BASE_URL + "/resetPassword", req)
      .then(response => {
        setDialogText({
          dialogTitle: "Success",
          dialogContent:
            "Your password has been updated. Please login with your new password."
        });
        setDialogOpen(true);
        dispatch(verificationSuccess());
      })
      .catch(err => {
        const errorMap = _.get(err, "response.data", null);
        if (
          errorMap.hasOwnProperty("newPassword") ||
          errorMap.hasOwnProperty("confirmNewPassword")
        ) {
          //input field errors
          dispatchErrorMapError(err, dispatch);
        } else {
          //not input field errors
          setDialogText({
            dialogTitle: "Error",
            dialogContent: "Your link has expired. Please request a new link."
          });
          setDialogOpen(true);
        }
        dispatch(verificationError());
      });
  };
};
