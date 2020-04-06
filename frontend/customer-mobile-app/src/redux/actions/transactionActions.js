import { SPRING_BACKEND_URL } from "src/constants/routes";
import { RETRIEVE_TXNS, UPDATE_VIEWED_TXN } from "src/redux/actions/types";
import axios from "axios";

const jsog = require("jsog");

const TRANSACTION_BASE_URL = SPRING_BACKEND_URL + "/api/transaction";

export const setViewedTransaction = (
  transactionId,
  redirectFunction,
  setLoading,
  onFinish
) => {
  if (setLoading) setLoading(true);
  return dispatch => {
    axios
      .get(TRANSACTION_BASE_URL + `/retrieveTransactionById/${transactionId}`)
      .then(response => {
        const { data } = jsog.decode(response);
        dispatch(updateViewedTransaction(data));
        if (redirectFunction) redirectFunction();
        if (onFinish) onFinish();
        if (setLoading) setLoading(false);
      })
      .catch(err => {
        console.log(err);
      });
  };
};

const updateViewedTransaction = data => ({
  type: UPDATE_VIEWED_TXN,
  transaction: data
});

export const retrieveCustomerInStoreTransactions = (customerId, setLoading) => {
  if (setLoading) setLoading(true);

  return dispatch => {
    axios
      .get(TRANSACTION_BASE_URL + `/retrieveCustomerInStoreTransactions`, {
        params: { customerId }
      })
      .then(response => {
        const { data } = jsog.decode(response);
        dispatch(updateTransactions(data));
        if (setLoading) setLoading(false);
      })
      .catch(err => {
        console.log(err);
      });
  };
};

export const retrieveCustomerInStoreCollectionTransactions = (
  customerId,
  setLoading
) => {
  if (setLoading) setLoading(true);

  return dispatch => {
    axios
      .get(
        TRANSACTION_BASE_URL + `/retrieveCustomerInStoreCollectionTransactions`,
        {
          params: { customerId }
        }
      )
      .then(response => {
        const { data } = jsog.decode(response);
        dispatch(updateTransactions(data));
        if (setLoading) setLoading(false);
      })
      .catch(err => {
        console.log(err);
      });
  };
};

const updateTransactions = data => ({
  type: RETRIEVE_TXNS,
  transactions: data
});
