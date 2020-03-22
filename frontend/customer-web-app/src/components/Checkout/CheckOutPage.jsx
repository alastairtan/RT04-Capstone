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
import Grid from "@material-ui/core/Grid";
import CardActions from "@material-ui/core/CardActions";
import CardContent from "@material-ui/core/CardContent";
import InputLabel from "@material-ui/core/InputLabel";

// redux
import { useDispatch, useSelector } from "react-redux";
import {
  getClientSecret,
  makePaymentWithSavedCard,
  completeDirectPayment
} from "../../redux/actions/shoppingCartActions";

// core components
import Parallax from "components/UI/Parallax/Parallax.js";
import GridContainer from "components/Layout/components/Grid/GridContainer.js";
import GridItem from "components/Layout/components/Grid/GridItem.js";
import Button from "components/UI/CustomButtons/Button.js";
import Card from "components/UI/Card/Card";
import CardBody from "components/UI/Card/CardBody";

// external libraries
import Cards from "react-credit-cards";
import "react-credit-cards/es/styles-compiled.css";
import { useStripe, useElements, CardElement } from "@stripe/react-stripe-js";

// local files
import checkoutStyle from "assets/jss/material-kit-pro-react/views/checkoutStyle.js";
import CardSection from "./../ShoppingCart/CardSection";
import PaymentRequest from "./../../models/payment/PaymentRequest";
import AddressCardForCheckOut from "./AddressCardForCheckOut";
import CheckoutProdVariantCard from "components/Checkout/CheckoutProdVariantCard";
import AddNewAddressForCheckOut from "./AddNewAddressForCheckout";
import { refreshCustomerId } from "../../redux/actions/customerActions";
import { updateShoppingCart } from "redux/actions/shoppingCartActions";
import UpdateShoppingCartRequest from "../../models/shoppingCart/UpdateShoppingCartRequest.js";

const useStyles = makeStyles(checkoutStyle);

export default function CheckOutPage() {
  const classes = useStyles();
  // Redux dispatch to call actions
  const dispatch = useDispatch();
  // Redux mapping state to props
  const errors = useSelector(state => state.errors);
  const customer = useSelector(state => state.customer.loggedInCustomer);
  const currAddress = useSelector(state => state.transaction.currAddress);
  const [billingAsShipping, setBillingAsShipping] = useState(
    customer.shippingAddresses.length === 0
  );
  const stripe = useStripe();
  const elements = useElements();
  const history = useHistory();

  const { onlineShoppingCart, creditCards, shippingAddresses } = customer;
  const [clientSecret, setClientSecret] = useState(null);
  const [creditCardIndex, setCreditCardIndex] = useState(
    creditCards.length > 0 ? 0 : null
  );
  const [addNewAddress, setAddNewAddress] = useState(false);
  const [currShippingAddress, setCurrShippingAddress] = useState(null);
  const [currBillingAddress, setCurrBillingAddress] = useState(null);
  const [addCard, setAddCard] = useState(false);
  const [editCurrAddress, setEditCurrAddress] = useState("");

  // Ensure the price of the products is correct
  useEffect(() => {
    window.scrollTo(0, 0);
    document.body.scrollTop = 0;
    _.get(customer, "onlineShoppingCart.shoppingCartItems", []).map(item => {
      const request = new UpdateShoppingCartRequest(
        -1,
        item.productVariant.productVariantId,
        customer.customerId,
        "online"
      );
      dispatch(updateShoppingCart(request));
    });
  }, []);

  useEffect(() => {
    dispatch(refreshCustomerId(customer.customerId));
  }, [currAddress]);

  useEffect(() => {}, [customer, clientSecret]);

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

  const handleMakePaymentWithNewCard = () => {
    const { initialTotalAmount } = onlineShoppingCart;
    // Send back to server to get client_secret to complete payment
    getClientSecret(initialTotalAmount, setClientSecret);
  };

  const handleConfirmPayment = async event => {
    event.preventDefault();

    let {
      shoppingCartId,
      initialTotalAmount: totalAmount
    } = onlineShoppingCart;
    let paymentMethodId;
    if (creditCards[creditCardIndex]) {
      paymentMethodId = creditCards[creditCardIndex].paymentMethodId;
    }
    const { customerId } = customer;
    // TODO: Process the amount to include finalTotalAmount
    // Stripe take in cents
    totalAmount = totalAmount * 100;
    const paymentRequest = new PaymentRequest(
      customerId,
      paymentMethodId,
      totalAmount,
      null,
      currShippingAddress,
      currBillingAddress
    );

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
          dispatch(completeDirectPayment(paymentRequest, history));
        }
      }
    } else {
      console.log("Payment with saved card!");
      console.log(paymentRequest);
      dispatch(makePaymentWithSavedCard(paymentRequest, history));
    }
  };

  /*
    Client secret need to updated in state when
    1. On applying / removing of promo code
    2. On changing of card
  */
  const onSelectCreditCard = e => {
    setCreditCardIndex(e.target.value);
    setClientSecret(null);
  };

  const toggleAddNewCard = e => {
    const addCardBoolean = addCard;
    setAddCard(!addCard);
    setClientSecret(null);
    if (!addCardBoolean) {
      setCreditCardIndex(null);
    } else {
      if (creditCards.length > 0) setCreditCardIndex(0);
    }
  };

  /*
    Disable the complete payment button if
    1. clientSecret === null && creditCardIndex === null\
      - no new card & no card selected
    2. no address selected
  */
  const disabled =
    (clientSecret === null && creditCardIndex === null) ||
    !currBillingAddress ||
    !currShippingAddress;

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
              <Grid container spacing={5}>
                <Grid item md={6}>
                  <Card>
                    <CardContent style={{ padding: "0 5%" }}>
                      <Grid container>
                        <Grid item xs={7}>
                          <Typography
                            className={classes.checkoutTitle}
                            variant="h4"
                            gutterBottom
                          >
                            TOTAL TO PAY
                          </Typography>
                        </Grid>
                        <Grid item xs={5} style={{ textAlign: "right" }}>
                          <Typography
                            className={classes.checkoutTitle}
                            variant="h4"
                            gutterBottom
                          >
                            SGD$
                            {onlineShoppingCart.finalTotalAmount.toFixed(2)}
                          </Typography>
                        </Grid>
                      </Grid>
                      <Divider style={{ marginBottom: "5%" }} />
                      <Grid container>
                        <Grid container item xs={12}>
                          <Grid item xs={6}>
                            <Typography variant="h6" component="h2">
                              Promo Code
                            </Typography>
                          </Grid>
                          <Grid
                            item
                            xs={6}
                            style={{ textAlign: "right", width: "100%" }}
                          >
                            <Button
                              onClick={null}
                              className={classes.checkoutButton}
                            >
                              Apply Promo Code
                            </Button>
                          </Grid>
                          <TextField fullWidth style={{ margin: "5% 0" }} />
                        </Grid>
                        <Grid item container xs={12}>
                          {addNewAddress ? (
                            <Grid item container xs={12}>
                              <Grid item xs={false} md={2} />
                              <Grid item xs={12} md={8}>
                                <AddNewAddressForCheckOut
                                  addNewAddress={[
                                    addNewAddress,
                                    setAddNewAddress
                                  ]}
                                  currShippingAddress={[
                                    currShippingAddress,
                                    setCurrShippingAddress
                                  ]}
                                  currBillingAddress={[
                                    currBillingAddress,
                                    setCurrBillingAddress
                                  ]}
                                  currAddress={currAddress}
                                  billingAsShipping={[
                                    billingAsShipping,
                                    setBillingAsShipping
                                  ]}
                                  editCurrAddress={[
                                    editCurrAddress,
                                    setEditCurrAddress
                                  ]}
                                />
                              </Grid>
                              <Grid item xs={false} md={2} />
                            </Grid>
                          ) : (
                            <Grid item xs={12} container>
                              <Grid item xs={12}>
                                <h5>Shipping & Billing</h5>
                                <AddressCardForCheckOut
                                  addNewAddress={[
                                    addNewAddress,
                                    setAddNewAddress
                                  ]}
                                  setCurrShippingAddress={
                                    setCurrShippingAddress
                                  }
                                  setCurrBillingAddress={setCurrBillingAddress}
                                  currAddress={currAddress}
                                  billingAsShipping={[
                                    billingAsShipping,
                                    setBillingAsShipping
                                  ]}
                                  editCurrAddress={[
                                    editCurrAddress,
                                    setEditCurrAddress
                                  ]}
                                />
                              </Grid>
                            </Grid>
                          )}
                        </Grid>

                        <Grid container item xs={12} alignItems="center">
                          <Grid item xs={6}>
                            <InputLabel>
                              {addCard ? "Use new card" : "Select payment card"}
                            </InputLabel>
                          </Grid>
                          <Grid
                            item
                            xs={6}
                            style={{ textAlign: "right", width: "100%" }}
                          >
                            <Button
                              onClick={toggleAddNewCard}
                              className={classes.checkoutButton}
                            >
                              {addCard ? "Cancel" : "Use a new card"}{" "}
                            </Button>
                          </Grid>
                          {customer.creditCards.length > 0 && !addCard && (
                            <Grid item xs={12}>
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
                                        key={index}
                                        classes={{
                                          root: classes.selectMenuItem,
                                          selected:
                                            classes.selectMenuItemSelected
                                        }}
                                        value={index}
                                      >
                                        •••• •••• •••• {last4}
                                      </MenuItem>
                                    );
                                  }
                                )}
                              </Select>
                              <Grid
                                item
                                xs={12}
                                style={{ transform: "scale(0.8)" }}
                              >
                                <Cards
                                  cvc={" "}
                                  expiry={`${expiryMonth}/${expiryYear}`}
                                  // focus={this.state.focus}
                                  name=" "
                                  number={`••••••••••••${last4}`}
                                  preview={true}
                                  issuer={issuer}
                                />
                              </Grid>
                            </Grid>
                          )}
                          {addCard && (
                            <GridContainer
                              xs={12}
                              item
                              style={{ padding: 0, margin: 0 }}
                            >
                              <Grid item xs={12}>
                                <CardSection
                                  disabled={!stripe || clientSecret !== null}
                                />
                              </Grid>
                              <Grid item xs={12} style={{ textAlign: "right" }}>
                                <Button
                                  color="github"
                                  onClick={handleMakePaymentWithNewCard}
                                  disabled={!stripe || clientSecret !== null}
                                >
                                  Use this card
                                </Button>
                              </Grid>
                            </GridContainer>
                          )}
                        </Grid>
                      </Grid>
                    </CardContent>
                    <CardActions style={{ padding: "4% 5%" }}>
                      <Button
                        color="success"
                        fullWidth
                        onClick={handleConfirmPayment}
                        disabled={disabled}
                      >
                        Confirm payment
                      </Button>
                    </CardActions>
                  </Card>
                </Grid>
                <Grid item md={6}>
                  {_.get(
                    customer,
                    "onlineShoppingCart.shoppingCartItems",
                    []
                  ).map((cartItem, index) => (
                    <CheckoutProdVariantCard
                      key={index}
                      cartItem={cartItem}
                      index={index}
                      customer={customer}
                    />
                  ))}
                </Grid>
              </Grid>
            </CardBody>
          </Card>
        </div>
      </div>
    </div>
  );
}
