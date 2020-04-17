import axios from "axios";
import * as types from "./types";
import { toast } from "react-toastify";
import {retrieveAllLeaves} from "./leaveActions";
axios.defaults.baseURL = process.env.REACT_APP_SPRING_API_URL;

const PAYROLL_BASE_URL = "/api/payroll";
const jsog = require("jsog");

export const calculateMonthlySalary = (calculateMonthlyPayrollRequest, history) => {
    return dispatch => {
        axios
            .post(PAYROLL_BASE_URL + "/calculateMonthlySalary" , calculateMonthlyPayrollRequest)
            .then(response => {
                const { data } = jsog.decode(response);
                dispatch(calculateSuccess(data));
                toast.success("Success!", {
                    position: toast.POSITION.TOP_CENTER
                });
            })
            .catch(err => {
                dispatch(calculateError(err.response.data));
            });
    };
};

const calculateSuccess = data => ({
    type: types.CALCULATE_MONTHLY_SALARY,
    allSalary: data
});

const calculateError = data => ({
    type: types.GET_ERRORS,
    errorMap: data
});

export const createPayrolls = (createPayrollsRequest, history) => {
    return dispatch => {
        axios
            .post(PAYROLL_BASE_URL + "/createPayrolls" , createPayrollsRequest)
            .then(response => {
                const { data } = jsog.decode(response);
                dispatch(createSuccess(data));
                toast.success("Successfully created payrolls!", {
                    position: toast.POSITION.TOP_CENTER
                });
                history.push(`/payrolls/viewAllHR`);
            })
            .catch(err => {
                if (err.response) {
                    toast.error(err.response.data.errorMessage, {
                        position: toast.POSITION.TOP_CENTER
                    });
                }
                dispatch(createError(err.response.data));
            });
    };
};

const createSuccess = data => ({
    type: types.CREATE_PAYROLLS,
    allPayrolls: data
});

const createError = data => ({
    type: types.GET_ERRORS,
    errorMap: data
});

export const retrieveAllPayrolls = staffId => {
    return dispatch => {
        axios
            .get(PAYROLL_BASE_URL + "/retrieveAllPayrolls/" + staffId)
            .then(response => {
                const { data } = jsog.decode(response);
                dispatch(retrieveAllPayrollsSuccess(data));
            })
            .catch(err => {
                dispatch(retrieveAllPayrollsError(err.response.data));
            });
    };
};

const retrieveAllPayrollsSuccess = data => ({
    type: types.RETRIEVE_ALL_PAYROLLS,
    allPayrollsStaff: data
});

const retrieveAllPayrollsError = data => ({
    type: types.GET_ERRORS,
    errorMap: data
});

export const retrievePayrollsForAMonth = (retrievePayrollsForAMonthRequest) => {
    return dispatch => {
        axios
            .post(PAYROLL_BASE_URL + "/retrievePayrollsForAMonth",  retrievePayrollsForAMonthRequest)
            .then(response => {
                const { data } = jsog.decode(response);
                dispatch(retrievePayrollsForAMonthSuccess(data));
                toast.success("Successfully retrieved payrolls!", {
                    position: toast.POSITION.TOP_CENTER
                });
            })
            .catch(err => {
                dispatch(retrievePayrollsForAMonthError(err.response.data));
            });
    };
};

const retrievePayrollsForAMonthSuccess = data => ({
    type: types.RETRIEVE_PAYROLLS_FOR_A_MONTH,
    allPayrolls: data
});

const retrievePayrollsForAMonthError = data => ({
    type: types.GET_ERRORS,
    errorMap: data
});

export const updatePayrollStatus = (payrollId,staffId) => {
    return dispatch => {
        axios
            .post(PAYROLL_BASE_URL + "/updatePayrollStatus/" + payrollId)
            .then(response => {
                const { data } = jsog.decode(response);
                dispatch(updateSuccess(data));
                toast.success("Successfully verified payroll!", {
                    position: toast.POSITION.TOP_CENTER
                });
                retrieveAllPayrolls(staffId)(dispatch);
            })
            .catch(err => {
                dispatch(updateError(err.response.data));
            });
    };
};

const updateSuccess = data => ({
    type: types.UPDATE_PAYROLL_STATUS,
    payroll: data
});

const updateError = data => ({
    type: types.GET_ERRORS,
    errorMap: data
});
