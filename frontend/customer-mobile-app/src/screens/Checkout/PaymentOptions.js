import React from "react";
import { Block, Text } from "galio-framework";
import { Dimensions, TouchableOpacity } from "react-native";
import { CardView } from "react-native-credit-card-input";
import AddressCardCheckout from "src/screens/Checkout/AddressCardCheckout";

const { width, height } = Dimensions.get("window");

function PaymentOptions(props) {
  const {
    customer,
    creditCard,
    setCreditCard,
    billingAddress,
    setBillingAddress,
    setCreditCardModalVisible,
    setAddressModalMode
  } = props;

  const renderDefaultCreditCard = () => {
    if (creditCard) return renderCreditCard(creditCard);
    if (customer.creditCards.length === 0) {
      return null;
    }

    const defaultCreditCard = customer.creditCards.find(
      item => item.defaultCard
    );
    if (defaultCreditCard) {
      setCreditCard(defaultCreditCard);
    } else {
      setCreditCard(customer.creditCards[0]);
    }
  };

  const renderCreditCard = item => {
    let month = item.expiryMonth;
    month = ("0" + month).slice(-2);
    let issuer = item.issuer.toLowerCase();
    issuer =
      issuer === "visa" ? "visa" : issuer === "mastercard" ? "master-card" : "";
    return (
      <CardView
        brand={issuer}
        name={customer.firstName + " " + customer.lastName}
        number={"•••• •••• •••• " + item.last4}
        expiry={`${month}/${item.expiryYear.toString().slice(-2)}`}
        scale={0.8}
      />
    );
  };

  const getDefaultShippingAddress = () => {
    if (billingAddress) return billingAddress;
    if (customer.shippingAddresses.length === 0) return null;

    const defaultBillingAddr = customer.shippingAddresses.find(
      item => item.billing
    );
    if (!defaultBillingAddr) {
      setBillingAddress(customer.shippingAddresses[0]);
    } else {
      setBillingAddress(defaultBillingAddr);
    }
  };

  return (
    <Block
      flex
      card
      style={{
        backgroundColor: "white",
        width: width,
        marginTop: 8,
        padding: 12,
        paddingBottom: 16,
        borderRadius: 0
      }}
    >
      {customer && (
        <>
          <Block flex row space="between" style={{ marginBottom: 5 }}>
            <Text h5 bold>
              Payment
            </Text>
            <TouchableOpacity
              onPress={() => setCreditCardModalVisible(true)}
              hitSlop={{ top: 50, bottom: 50, left: 50, right: 50 }}
            >
              <Text
                h5
                bold
                style={{
                  fontSize: 16,
                  textDecorationLine: "underline",
                  color: "mediumblue"
                }}
              >
                Edit
              </Text>
            </TouchableOpacity>
          </Block>
          <Block flex center>
            {renderDefaultCreditCard() != null ? (
              renderDefaultCreditCard()
            ) : (
              <Text h6 bold style={{ color: "darkred" }}>
                Please add a credit card for payment.
              </Text>
            )}
          </Block>
          <Block flex row space="between" style={{ marginTop: 20 }}>
            <Text h6 bold style={{ fontSize: 16, color: "dimgray" }}>
              Billing Address
            </Text>
            <TouchableOpacity
              onPress={() => setAddressModalMode("Billing")}
              hitSlop={{ top: 50, bottom: 50, left: 50, right: 50 }}
            >
              <Text
                h6
                bold
                style={{
                  fontSize: 16,
                  textDecorationLine: "underline",
                  color: "mediumblue"
                }}
              >
                Edit
              </Text>
            </TouchableOpacity>
          </Block>
          <AddressCardCheckout address={getDefaultShippingAddress()} />
        </>
      )}
    </Block>
  );
}

export default PaymentOptions;
