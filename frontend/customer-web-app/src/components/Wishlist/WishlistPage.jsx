/*eslint-disable*/
import React, { useEffect } from "react";
// nodejs library that concatenates classes
import classNames from "classnames";
// @material-ui/core components
import { makeStyles } from "@material-ui/core/styles";
// @material-ui/icons
// redux
import { useDispatch, useSelector } from "react-redux";
// core components
import Parallax from "components/UI/Parallax/Parallax.js";
import GridContainer from "components/Layout/components/Grid/GridContainer.js";
import GridItem from "components/Layout/components/Grid/GridItem.js";
import Card from "components/UI/Card/Card";
import CardBody from "components/UI/Card/CardBody";

import shoppingCartStyle from "assets/jss/material-kit-pro-react/views/shoppingCartStyle.js";
import WishlistItemCard from "components/Wishlist/WishlistItemCard";

const useStyles = makeStyles(shoppingCartStyle);

export default function WishlistPage(props) {
  const classes = useStyles();
  // Redux dispatch to call actions
  const dispatch = useDispatch();
  // Redux mapping state to props
  const errors = useSelector(state => state.errors);
  const customer = useSelector(state => state.customer.loggedInCustomer);
  const { wishlistItems } = customer;

  useEffect(() => {
    window.scrollTo(0, 0);
    document.body.scrollTop = 0;
  }, []);

  return (
    <div>
      <Parallax
        image={require("assets/img/examples/bg2.jpg")}
        filter="dark"
        small
      >
        <div className={classes.container}>
          <GridContainer>
            <GridItem
              md={8}
              sm={8}
              className={classNames(
                classes.mlAuto,
                classes.mrAuto,
                classes.textCenter
              )}
            >
              <h2 className={classes.title}>My Wishlist</h2>
            </GridItem>
          </GridContainer>
        </div>
      </Parallax>
      <div className={classNames(classes.main, classes.mainRaised)}>
        <div className={classes.container}>
          <Card plain>
            <CardBody plain>
              <h3 className={classes.cardTitle}>My Wishlist</h3>
              {wishlistItems.map(productVariant => (
                <WishlistItemCard productVariant={productVariant} />
              ))}
            </CardBody>
          </Card>
        </div>
      </div>
    </div>
  );
}
