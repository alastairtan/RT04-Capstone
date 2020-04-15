import axios from "axios";
import { dispatchErrorMapError } from "./index";
import * as types from "./types";
import { CREATE_ONLINE_REFUND_RECORD } from "./types";
import GenerateRefundLabel from "../../models/refund/GenerateRefundLabel";

axios.defaults.baseURL = process.env.REACT_APP_SPRING_API_URL;
const NODE_API_URL = process.env.REACT_APP_NODE_API_URL;

console.log(NODE_API_URL);

const REFUND_BASE_URL = "/api/refund";
const EMAIL_BASE_URL = "/api/email";
const _ = require("lodash");
const jsog = require("jsog");

export const createOnlineRefundRequest = (
  refundRequest,
  history,
  enqueueSnackbar,
  deliveryAddress,
  name,
  orderNumber
) => {
  return dispatch => {
    //redux thunk passes dispatch
    axios
      .post(REFUND_BASE_URL + "/createRefundRecord", refundRequest)
      .then(response => {
        const { data } = jsog.decode(response);
        dispatch(createOnlineRefundSuccess(data));
        const generateRefundLabel = new GenerateRefundLabel(
          deliveryAddress,
          name,
          data.refundNumber,
          data.refundId,
          data.customer.email,
          orderNumber
        );
        dispatch(createRefundLabel(generateRefundLabel));
        setTimeout(() => dispatch(sendRefundLabel(generateRefundLabel)), 10000);
        history.push(`/account/profile/viewRefund/${data.refundId}`);
        enqueueSnackbar("Refund request created", {
          variant: "success",
          autoHideDuration: 1200
        });
      })
      .catch(err => {
        dispatchErrorMapError(err, dispatch);
        console.log(err.response.data);
      });
  };
};

export const createOnlineRefundSuccess = data => ({
  type: CREATE_ONLINE_REFUND_RECORD,
  currRefund: data
});

export const retrieveRefundsByCustomerId = customerId => {
  return dispatch => {
    axios
      .get(REFUND_BASE_URL + `/retrieveRefundsByCustomerId/${customerId}`)
      .then(response => {
        const data = jsog.decode(response.data);
        dispatch(retrieveRefundsByCustomerIdSuccess(data));
      })
      .catch(err => {
        dispatchErrorMapError(err, dispatch);
      });
  };
};

const retrieveRefundsByCustomerIdSuccess = data => ({
  type: types.GET_REFUNDS_BY_CUSTOMER_ID,
  refunds: data
});

export const retrieveRefundById = refundId => {
  return dispatch => {
    //redux thunk passes dispatch
    axios
      .get(REFUND_BASE_URL + `/retrieveRefundById/${refundId}`)
      .then(response => {
        const data = jsog.decode(response.data);
        console.log(data);
        dispatch(retrieveRefundByIdSuccess(data));
      })
      .catch(err => {
        dispatchErrorMapError(err, dispatch);
      });
  };
};

const retrieveRefundByIdSuccess = data => ({
  type: types.RETRIEVE_REFUND_BY_ID,
  currRefund: data
});

export const createRefundLabel = request => {
  return dispatch => {
    axios
      .post(NODE_API_URL + REFUND_BASE_URL + "/generateRefundPDF", request)
      .then(({ data }) => {
        dispatch(createRefundLabelSuccess(data));
      })
      .catch(err => {
        dispatchErrorMapError(err, dispatch);
      });
  };
};
const createRefundLabelSuccess = data => ({
  type: types.CREATE_REFUND_LABEL_SUCCESS,
  createPDF: data
});

export const sendRefundLabel = request => {
  return dispatch => {
    axios
      .post(NODE_API_URL + EMAIL_BASE_URL + "/sendRefundLabel", request)
      .then(({ data }) => {
        dispatch(sendRefundLabelSuccess(data));
      })
      .catch(err => {
        dispatchErrorMapError(err, dispatch);
      });
  };
};
const sendRefundLabelSuccess = data => ({
  type: types.CREATE_REFUND_LABEL_SUCCESS,
  createPDF: data
});

export const retrieveRefundsByTransactionId = transactionId => {
  return axios
    .get(REFUND_BASE_URL + `/retrieveRefundsByTransactionId`, {
      params: { transactionId }
    })
    .then(response => {
      const data = jsog.decode(response.data);
      console.log(data);
      return data;
      // dispatch(retrieveRefundsByTransactionIdSuccess(data));
    })
    .catch(err => {
      // console.log(err.response);
    });
};

const retrieveRefundsByTransactionIdSuccess = data => ({
  type: types.GET_REFUND_BY_TRANSACTION_ID,
  refundsToCheck: data
});
