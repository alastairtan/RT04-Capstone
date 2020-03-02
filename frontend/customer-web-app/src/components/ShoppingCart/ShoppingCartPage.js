/*eslint-disable*/
import React, { useEffect, useState } from "react";
import { useHistory, useLocation } from "react-router-dom";
// nodejs library that concatenates classes
import classNames from "classnames";
// @material-ui/core components
import { makeStyles } from "@material-ui/core/styles";
import Typography from "@material-ui/core/Typography";
import Divider from "@material-ui/core/Divider";
import List from "@material-ui/core/List";
import ListItem from "@material-ui/core/ListItem";
import Tooltip from "@material-ui/core/Tooltip";
import Grid from "@material-ui/core/Grid";
import CancelIcon from "@material-ui/icons/Cancel";
import IconButton from "@material-ui/core/IconButton";
import AddIcon from "@material-ui/icons/Add";
import RemoveIcon from "@material-ui/icons/Remove";
import CardActions from "@material-ui/core/CardActions";
import CardContent from "@material-ui/core/CardContent";

// @material-ui/icons
import Favorite from "@material-ui/icons/Favorite";
import Close from "@material-ui/icons/Close";
import Remove from "@material-ui/icons/Remove";
import Add from "@material-ui/icons/Add";
import KeyboardArrowRight from "@material-ui/icons/KeyboardArrowRight";

// redux
import { useDispatch, useSelector } from "react-redux";

// core components
import Parallax from "components/UI/Parallax/Parallax.js";
import GridContainer from "components/Layout/components/Grid/GridContainer.js";
import GridItem from "components/Layout/components/Grid/GridItem.js";

import Button from "components/UI/CustomButtons/Button.js";
import Card from "components/UI/Card/Card";
import CardHeader from "components/UI/Card/CardHeader";
import CardBody from "components/UI/Card/CardBody";

import shoppingCartStyle from "assets/jss/material-kit-pro-react/views/shoppingCartStyle.js";
import {
  updateShoppingCart,
  checkOut
} from "./../../redux/actions/shoppingCartActions";
import { saveCard } from "./../../redux/actions/customerActions";
import UpdateShoppingCartRequest from "../../models/shoppingCart/UpdateShoppingCartRequest.js";
import CreditCardDialog from "./CreditCardDialog.js";

const useStyles = makeStyles(shoppingCartStyle);

export default function ShoppingCartPage() {
  const classes = useStyles();
  // Redux dispatch to call actions
  const dispatch = useDispatch();
  // Redux mapping state to props
  const errors = useSelector(state => state.errors);
  const customer = useSelector(state => state.customer.loggedInCustomer);
  const clientSecret = useSelector(state => state.customer.clientSecret);

  // Updating shopping cart information
  useEffect(() => {
    window.scrollTo(0, 0);
    document.body.scrollTop = 0;
  }, []);

  useEffect(() => {
    setShoppingCartItems(
      _.get(customer, "onlineShoppingCart.shoppingCartItems", [])
    );
  }, [customer]);

  const [showCreditCardDialog, setShowCreditCardDialog] = useState(false);
  const [shoppingCartItems, setShoppingCartItems] = useState(
    _.get(customer, "onlineShoppingCart.shoppingCartItems", [])
  );

  console.log(customer.onlineShoppingCart);
  console.log(clientSecret);

  const handleDelete = (e, productVariantId) => {
    const request = new UpdateShoppingCartRequest(
      0,
      productVariantId,
      customer.customerId,
      "online"
    );
    dispatch(updateShoppingCart(request));
  };

  const handleUpdateQuantity = (quantity, productVariantId) => {
    const request = new UpdateShoppingCartRequest(
      quantity,
      productVariantId,
      customer.customerId,
      "online"
    );
    dispatch(updateShoppingCart(request));
  };

  const handleCheckout = () => {
    // dispatch(checkOut({ totalAmount: 1500 }, setShowCreditCardDialog));
    dispatch(saveCard(customer.customerId, setShowCreditCardDialog));
  };

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
              <h2 className={classes.title}>Shopping Cart</h2>
            </GridItem>
          </GridContainer>
        </div>
      </Parallax>
      <div className={classNames(classes.main, classes.mainRaised)}>
        <div className={classes.container}>
          <Card plain>
            <CardBody plain>
              <h3 className={classes.cardTitle}>Shopping Cart</h3>
              <Grid container spacing={3}>
                <Grid item md={9}>
                  {shoppingCartItems.map(cartItem => {
                    // console.log(cartItem);
                    const {
                      productImages,
                      product,
                      sizeDetails,
                      colour,
                      productVariantId
                    } = cartItem.productVariant;
                    const { quantity } = cartItem;
                    return (
                      <Card>
                        <GridContainer
                          alignItems="center"
                          style={{ textAlign: "center" }}
                        >
                          {/* Photo */}
                          <Grid md={2}>
                            {/* Modified CSS */}
                            <div className={classes.imgContainer}>
                              <img
                                className={classes.img}
                                src={productImages[1].productImageUrl}
                              />
                            </div>
                          </Grid>
                          {/* Name */}
                          <GridItem md={2}>
                            <p>{product.productName}</p>
                          </GridItem>
                          {/* Colour */}
                          <GridItem md={1}>
                            <p>{colour}</p>
                          </GridItem>
                          {/* Size */}
                          <GridItem md={1}>
                            <p>{sizeDetails.productSize}</p>
                          </GridItem>
                          {/* Price */}
                          <GridItem md={1}>
                            <p>${product.price}</p>
                          </GridItem>
                          {/* Quantity */}
                          <GridItem md={1}>
                            <IconButton
                              style={{ marginTop: "-15%" }}
                              onClick={e =>
                                handleUpdateQuantity(
                                  quantity - 1,
                                  productVariantId
                                )
                              }
                            >
                              <RemoveIcon />
                            </IconButton>
                          </GridItem>
                          <GridItem md={1}>
                            <p>{quantity}</p>
                          </GridItem>
                          <GridItem md={1}>
                            <IconButton
                              style={{ marginTop: "-15%" }}
                              onClick={e =>
                                handleUpdateQuantity(
                                  quantity + 1,
                                  productVariantId
                                )
                              }
                            >
                              <AddIcon />
                            </IconButton>
                          </GridItem>
                          {/* Amount */}
                          <GridItem md={1}>
                            <p>${(product.price * quantity).toFixed(2)}</p>
                          </GridItem>
                          {/* Action */}
                          <GridItem md={1}>
                            <IconButton
                              style={{ marginTop: "-15%" }}
                              onClick={e => handleDelete(e, productVariantId)}
                            >
                              <CancelIcon style={{ color: "red" }} />
                            </IconButton>
                          </GridItem>
                        </GridContainer>
                      </Card>
                    );
                  })}
                </Grid>
                <Grid item md={3}>
                  <Card>
                    <CardContent>
                      <Typography variant="h4" gutterBottom>
                        TOTAL
                      </Typography>
                      <Divider style={{ marginBottom: "5%" }} />
                      <Grid container>
                        <Grid item xs={6}>
                          <Typography variant="h6" component="h2">
                            Sub-total
                          </Typography>
                        </Grid>
                        <Grid item xs={6} style={{ textAlign: "right" }}>
                          <Typography variant="h6" component="h2">
                            {customer.onlineShoppingCart.initialTotalAmount}
                          </Typography>
                        </Grid>
                      </Grid>

                      {/* <Typography className={classes.pos} color="textSecondary">
                        adjective
                      </Typography>
                      <Typography variant="body2" component="p">
                        well meaning and kindly.
                        <br />
                        {'"a benevolent smile"'}
                      </Typography> */}
                    </CardContent>
                    <CardActions>
                      <Button
                        color="success"
                        fullWidth
                        onClick={handleCheckout}
                      >
                        Checkout
                      </Button>
                    </CardActions>
                  </Card>
                </Grid>
              </Grid>
            </CardBody>
          </Card>
          {showCreditCardDialog && (
            <CreditCardDialog handleClose={setShowCreditCardDialog} />
          )}
        </div>
      </div>
    </div>
  );
}
