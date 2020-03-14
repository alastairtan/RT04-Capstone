import React from "react";
import { Route, Switch } from "react-router-dom";
import { useSelector } from "react-redux";
import WrappedRoutes from "./WrappedRoutes";
import MainWrapper from "./MainWrapper";
import LogInForm from "../../shared/components/Login/LogInForm";
import LoginCard from "../../shared/components/Login/LoginCard";
import StaffLoginForm from "../../components/Staff/components/StaffLoginForm";
import SecureRoute from "./SecureRoute";
import Backdrop from "@material-ui/core/Backdrop";
import CircularProgress from "@material-ui/core/CircularProgress";
import QrCodes from "../../components/QR/QrCodes";

const Router = () => {
  const open = useSelector(state => state.util.circularProgressOpen);

  return (
    <MainWrapper>
      <main>
        <Backdrop style={{ zIndex: 10000000 }} open={open}>
          <CircularProgress color="inherit" />
        </Backdrop>
        <Switch>
          <Route path="/login" component={StaffLoginForm} />
          <SecureRoute path="/qrCodes" component={QrCodes} />
          <SecureRoute path="/" component={WrappedRoutes} />
        </Switch>
      </main>
    </MainWrapper>
  );
};

export default Router;
