/*eslint-disable*/
import React, { useEffect, useState } from "react";
import { useHistory, useLocation } from "react-router-dom";
// nodejs library that concatenates classes
import classNames from "classnames";
// @material-ui/core components
import { makeStyles } from "@material-ui/core/styles";
import Typography from "@material-ui/core/Typography";
import Divider from "@material-ui/core/Divider";
import TextField from "@material-ui/core/TextField";
import Select from "@material-ui/core/Select";
import MenuItem from "@material-ui/core/MenuItem";
import List from "@material-ui/core/List";
import ListItem from "@material-ui/core/ListItem";
import Tooltip from "@material-ui/core/Tooltip";
import Grid from "@material-ui/core/Grid";
import CancelIcon from "@material-ui/icons/Cancel";
import IconButton from "@material-ui/core/IconButton";
import AddIcon from "@material-ui/icons/Add";
import AddBoxIcon from "@material-ui/icons/AddBox";
import MinusBoxIcon from "@material-ui/icons/IndeterminateCheckBox";
import RemoveIcon from "@material-ui/icons/Remove";
import CardActions from "@material-ui/core/CardActions";
import CardContent from "@material-ui/core/CardContent";
import InputLabel from "@material-ui/core/InputLabel";

// @material-ui/icons
import Favorite from "@material-ui/icons/Favorite";
import Close from "@material-ui/icons/Close";
import Remove from "@material-ui/icons/Remove";
import Add from "@material-ui/icons/Add";
import KeyboardArrowRight from "@material-ui/icons/KeyboardArrowRight";

// redux
import { useDispatch, useSelector } from "react-redux";
import {
  getClientSecret,
  makePayment
} from "../../redux/actions/shoppingCartActions";

// core components
import Parallax from "components/UI/Parallax/Parallax.js";
import GridContainer from "components/Layout/components/Grid/GridContainer.js";
import GridItem from "components/Layout/components/Grid/GridItem.js";
import Button from "components/UI/CustomButtons/Button.js";
import Card from "components/UI/Card/Card";
import CardHeader from "components/UI/Card/CardHeader";
import CardBody from "components/UI/Card/CardBody";

// external libraries
import Cards from "react-credit-cards";
import "react-credit-cards/es/styles-compiled.css";
import { useStripe, useElements, CardElement } from "@stripe/react-stripe-js";

// local files
import checkoutStyle from "assets/jss/material-kit-pro-react/views/checkoutStyle.js";
import UpdateShoppingCartRequest from "../../models/shoppingCart/UpdateShoppingCartRequest.js";
import AddressCard from "./../Profile/sections/AddressCard";
import CustomDropdown from "components/UI/CustomDropdown/CustomDropdown.js";
import CardSection from "./../ShoppingCart/CardSection";
import PaymentRequest from "./../../models/payment/PaymentRequest";

const useStyles = makeStyles(checkoutStyle);

export default function CheckOutPage() {
  const classes = useStyles();
  // Redux dispatch to call actions
  const dispatch = useDispatch();
  // Redux mapping state to props
  const errors = useSelector(state => state.errors);
  const customer = useSelector(state => state.customer.loggedInCustomer);

  const stripe = useStripe();
  const elements = useElements();
  const history = useHistory();

  // Updating shopping cart information
  useEffect(() => {
    window.scrollTo(0, 0);
    document.body.scrollTop = 0;
  }, []);

  const { onlineShoppingCart, creditCards, shippingAddresses } = customer;
  const [clientSecret, setClientSecret] = useState(null);
  const [creditCardIndex, setCreditCardIndex] = useState(0);
  const [selectedCard, setSelectedCard] = useState(null);

  useEffect(() => {
    // setShoppingCartItems(
    //   _.get(customer, "onlineShoppingCart.shoppingCartItems", [])
    // );
  }, [customer, clientSecret]);

  let expiryMonth, expiryYear, last4, issuer, creditCardId;
  if (creditCards[creditCardIndex]) {
    const creditCard = creditCards[creditCardIndex];
    expiryMonth = creditCard.expiryMonth;
    expiryYear = creditCard.expiryYear;
    last4 = creditCard.last4;
    issuer = creditCard.issuer;
    creditCardId = creditCard.creditCardId;
  }

  expiryMonth = expiryMonth > 10 ? expiryMonth : `0${expiryMonth}`;

  console.log(onlineShoppingCart);
  console.log(creditCards);
  console.log(shippingAddresses);
  console.log(clientSecret);

  /*
    Client secret need to updated and stored in redux store
    1. On page load
    2. On applying / removing of promo code
    3. On changing of card
  */

  const [addCard, setAddCard] = useState(false);

  const handleMakePaymentWithNewCard = () => {
    const { initialTotalAmount } = onlineShoppingCart;
    // Send back to server to get client_secret to complete payment
    getClientSecret(initialTotalAmount, setClientSecret);
  };

  const handleConfirmPayment = async event => {
    // We don't want to let default form submission happen here,
    // which would refresh the page.
    event.preventDefault();

    if (clientSecret !== null) {
      if (!stripe || !elements) {
        // Stripe.js has not yet loaded.
        // Make sure to disable form submission until Stripe.js has loaded.
        return;
      }

      const result = await stripe.confirmCardPayment(clientSecret, {
        payment_method: {
          card: elements.getElement(CardElement),
          billing_details: {
            name: `${customer.firstName} ${customer.lastName}`
          }
        }
      });

      if (result.error) {
        // Show error to your customer (e.g., insufficient funds)
        console.log(result.error.message);
      } else {
        // The payment has been processed!
        console.log("Payment succeed!!");
        if (result.paymentIntent.status === "succeeded") {
          // Show a success message to your customer
          // There's a risk of the customer closing the window before callback
          // execution. Set up a webhook or plugin to listen for the
          // payment_intent.succeeded event that handles any business critical
          // post-payment actions.
          console.log("YAY succeed!!");
          console.log(result);
        }
      }
    } else {
      console.log("Payment with saved card!");
      const {
        shoppingCartId,
        initialTotalAmount: totalAmount
      } = onlineShoppingCart;
      const { paymentMethodId } = creditCards[creditCardIndex];
      const { customerId } = customer;
      // Stripe take in cents
      totalAmount = totalAmount * 100;
      const paymentRequest = new PaymentRequest(
        customerId,
        paymentMethodId,
        totalAmount,
        shoppingCartId
      );
      console.log(paymentRequest);
      dispatch(makePayment(paymentRequest, history));
    }
  };

  const onSelectCreditCard = e => {
    console.log(e);
    setCreditCardIndex(e.target.value);
    setClientSecret(null);
  };

  const toggleAddNewCard = e => {
    setAddCard(!addCard);
    setClientSecret(null);
  };

  return (
    <div>
      <Parallax image={require("assets/img/bg6.jpg")} filter="dark" small>
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
              <h2 className={classes.title}>Check Out</h2>
            </GridItem>
          </GridContainer>
        </div>
      </Parallax>
      <div className={classNames(classes.main, classes.mainRaised)}>
        <div className={classes.container}>
          <Card plain>
            <CardBody plain>
              <h3 className={classes.cardTitle}>Check Out</h3>
              <Grid container spacing={6}>
                <Grid item md={6}>
                  <Card>
                    <CardContent>
                      <GridContainer>
                        <GridItem xs={7}>
                          <Typography
                            className={classes.checkoutTitle}
                            variant="h4"
                            gutterBottom
                          >
                            TOTAL TO PAY
                          </Typography>
                        </GridItem>
                        <GridItem xs={5} style={{ textAlign: "right" }}>
                          <Typography
                            className={classes.checkoutTitle}
                            variant="h4"
                            gutterBottom
                          >
                            SGD${onlineShoppingCart.initialTotalAmount}
                          </Typography>
                        </GridItem>
                      </GridContainer>
                      <Divider style={{ marginBottom: "5%" }} />
                      <Grid container>
                        <Grid item xs={12}>
                          <Typography variant="h6" component="h2">
                            Promo Code
                          </Typography>
                          <TextField fullWidth />
                        </Grid>
                        <Grid item xs={12}>
                          <Select
                            style={{ minWidth: 120 }}
                            defaultValue=""
                            // MenuProps={{
                            //   className: classes.selectMenu
                            // }}
                            // classes={{
                            //   select: classes.select
                            // }}
                            // onChange={onChange}
                            name="contactUsCategory"
                          >
                            {[1, 2, 3, 4].map(function(item, i) {
                              return (
                                <MenuItem
                                  classes={{
                                    root: classes.selectMenuItem,
                                    selected: classes.selectMenuItemSelected
                                  }}
                                  value={item}
                                >
                                  {item}
                                </MenuItem>
                              );
                            })}
                          </Select>
                          {/* <AddressCard /> */}
                        </Grid>
                        <Grid
                          container
                          item
                          xs={12}
                          alignItems="center"
                          justifyItems="center"
                        >
                          <GridItem xs={6}>
                            <InputLabel>
                              {addCard
                                ? "Use a new card"
                                : "Select payment card"}
                            </InputLabel>
                          </GridItem>
                          <GridItem xs={6} style={{ textAlign: "right" }}>
                            <Button onClick={toggleAddNewCard}>
                              {addCard ? "Cancel" : "Use a new card"}{" "}
                            </Button>
                          </GridItem>
                          {customer.creditCards && !addCard && (
                            <GridItem xs={12}>
                              <Select
                                style={{
                                  margin: "5% 0",
                                  textAlign: "center",
                                  fontSize: "24px"
                                }}
                                fullWidth
                                // style={{ width: 200 }}
                                defaultValue={creditCardIndex}
                                onChange={onSelectCreditCard}
                                name="credit-card"
                              >
                                {customer.creditCards.map(
                                  ({ last4, creditCardId }, index) => {
                                    return (
                                      <MenuItem
                                        classes={{
                                          root: classes.selectMenuItem,
                                          selected:
                                            classes.selectMenuItemSelected
                                        }}
                                        value={index}
                                      >
                                        **** **** **** {last4}
                                      </MenuItem>
                                    );
                                  }
                                )}
                              </Select>
                              <GridItem
                                xs={12}
                                style={{ transform: "scale(0.8)" }}
                              >
                                <Cards
                                  cvc={" "}
                                  expiry={`${expiryMonth}/${expiryYear}`}
                                  // focus={this.state.focus}
                                  name=" "
                                  number={`************${last4}`}
                                  preview={true}
                                  issuer={issuer}
                                />
                              </GridItem>
                            </GridItem>
                          )}
                          {addCard && (
                            <GridItem container xs={12} style={{ padding: 0 }}>
                              <GridItem xs={12}>
                                <CardSection />
                              </GridItem>
                              <GridItem xs={12} style={{ textAlign: "right" }}>
                                <Button
                                  color="github"
                                  onClick={handleMakePaymentWithNewCard}
                                  disabled={!stripe}
                                  // className={classes.firstButton}
                                >
                                  Use this card
                                </Button>
                              </GridItem>
                            </GridItem>
                          )}
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
                        onClick={handleConfirmPayment}
                        // disabled={true}
                      >
                        Confirm payment
                      </Button>
                    </CardActions>
                  </Card>
                </Grid>
                <Grid item md={1} />
                <Grid item md={5}>
                  {customer.onlineShoppingCart.shoppingCartItems.map(
                    (cartItem, index) => {
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
                        <div>
                          <Card plain>
                            <GridContainer
                              alignItems="center"
                              style={{ textAlign: "center" }}
                            >
                              {/* Photo */}
                              <Grid item md={2}>
                                {/* Modified CSS */}
                                <div className={classes.imgContainer}>
                                  <img
                                    className={classes.img}
                                    src={productImages[1].productImageUrl}
                                  />
                                </div>
                              </Grid>
                              {/* Name */}
                              <GridItem
                                container
                                md={6}
                                style={{ textAlign: "left" }}
                              >
                                <GridItem md={12}>
                                  <h3 className={classes.productName}>
                                    {product.productName}
                                  </h3>
                                </GridItem>
                                <GridItem md={12}>
                                  <h3 style={{ marginTop: "10px" }}>
                                    ${product.price}
                                  </h3>
                                </GridItem>
                                <GridItem md={12}>
                                  {colour}, {sizeDetails.productSize}
                                </GridItem>
                              </GridItem>
                              {/* Quantity */}
                              <GridItem md={1}>
                                <h3>{quantity}</h3>
                              </GridItem>
                              {/* Amount */}
                              <GridItem md={3}>
                                <h3>
                                  ${(product.price * quantity).toFixed(2)}
                                </h3>
                              </GridItem>
                            </GridContainer>
                          </Card>
                          {index !==
                            customer.onlineShoppingCart.shoppingCartItems
                              .length -
                              1 && <Divider style={{ margin: "0 5%" }} />}
                        </div>
                      );
                    }
                  )}
                </Grid>
              </Grid>
            </CardBody>
          </Card>
        </div>
      </div>
    </div>
  );
}
