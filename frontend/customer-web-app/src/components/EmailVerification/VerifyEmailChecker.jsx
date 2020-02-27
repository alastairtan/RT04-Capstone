import React, { useEffect } from "react";
import { updateEmail, verify } from "redux/actions/customerActions";
import { useDispatch, useSelector } from "react-redux";
import { Redirect, useHistory, useRouteMatch } from "react-router-dom";
import LoadingOverlay from "react-loading-overlay";
import VerifyEmailConfirmation from "components/EmailVerification/VerifyEmailConfirmation";
import VerifyEmailFailure from "components/EmailVerification/VerifyEmailFailure";
import dg2 from "assets/img/dg2.jpg";
import GridContainer from "components/Layout/components/Grid/GridContainer";
import { makeStyles } from "@material-ui/core/styles";
import headersStyle from "assets/jss/material-kit-pro-react/views/sectionsSections/headersStyle";

const useStyles = makeStyles(headersStyle);

const _ = require("lodash");

function VerifyEmailChecker(props) {
  const { isUpdateEmail } = props;
  //Hooks
  const classes = useStyles();
  const match = useRouteMatch();
  const history = useHistory();

  //Redux
  const dispatch = useDispatch();
  const isSendingEmail = useSelector(state => state.customer.isSendingEmail);
  const errors = useSelector(state => state.errors);
  const verificationStatus = useSelector(
    state => state.customer.verificationStatus
  );

  //State

  //Effects
  useEffect(() => {
    const verificationCode = match.params.verificationCode;
    if (isUpdateEmail) {
      dispatch(updateEmail(verificationCode, history));
    } else {
      console.log("running verify");
      dispatch(verify(verificationCode, history));
    }
  }, []);

  useEffect(() => {
    window.scrollTo(0, 0);
    document.body.scrollTop = 0;
  }, []);

  //Misc
  const loadingText =
    isSendingEmail && _.isEmpty(errors)
      ? "Sending you an email..."
      : verificationStatus === null
      ? "Verifying..."
      : "";

  return (
    <LoadingOverlay
      spinner
      active={
        (isSendingEmail && _.isEmpty(errors)) || verificationStatus === null
      }
      text={loadingText}
    >
      <div
        className={classes.pageHeader}
        style={{ backgroundImage: `url("${dg2}")` }}
      >
        <div className={classes.container}>
          <GridContainer>
            {verificationStatus === "SUCCESS" ? (
              isUpdateEmail ? (
                <Redirect
                  to={{
                    pathname: "/account/login",
                    state: { isUpdateEmail: true }
                  }}
                />
              ) : (
                <VerifyEmailConfirmation classes={classes} />
              )
            ) : verificationStatus === "FAILURE" ? (
              isUpdateEmail ? (
                <Redirect
                  to
                  to={{
                    pathname: "/account/login",
                    state: { isUpdateEmail: true, linkExpired: true }
                  }}
                />
              ) : (
                <VerifyEmailFailure classes={classes} />
              )
            ) : null}
          </GridContainer>
        </div>
      </div>
    </LoadingOverlay>
  );
}

export default VerifyEmailChecker;
