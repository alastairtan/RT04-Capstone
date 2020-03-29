import React from "react";
import {
  RestockOrderItemTable,
  DeliveryTable
} from "../../../components/Delivery";
import { Route, Switch } from "react-router-dom";
import RetailRoute from "../RetailRoute";

export default () => (
  <Switch>
    <RetailRoute
      exact
      path="/delivery/viewAllDelivery"
      component={DeliveryTable}
    />
    <RetailRoute
      exact
      path="/delivery/viewAllRestockOrderItem"
      component={RestockOrderItemTable}
    />
  </Switch>
);
