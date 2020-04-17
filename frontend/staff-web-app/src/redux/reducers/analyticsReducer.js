import * as types from "../actions/types";

const initialState = {
  salesByDay: null,
  reservationsByTimeslot: null,
};

export default function(state = initialState, action) {
  switch (action.type) {
    case types.RETRIEVE_SALES_BY_DAY:
      return {
        ...state,
        salesByDay: action.salesByDay
      };
    case types.RETRIEVE_RESERVATIONS_BY_TIMESLOT:
      return {
        ...state,
        reservationsByTimeslot: action.reservationsByTimeslot
      }
    default:
      return state;
  }
}
