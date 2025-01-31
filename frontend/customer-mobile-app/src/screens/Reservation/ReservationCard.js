import React from "react";
import { Block, Text } from "galio-framework";
import { Divider } from "react-native-paper";
import { Dimensions, Image, ScrollView, TouchableOpacity } from "react-native";
import { MaterialIcons } from "@expo/vector-icons";
import { useDispatch } from "react-redux";
import { setViewedReservation } from "src/redux/actions/reservationActions";

const moment = require("moment");
const { width, height } = Dimensions.get("window");

function ReservationCard(props) {
  const { reservation, navigation, setLoading } = props;
  const dispatch = useDispatch();

  const viewReservationDetails = () => {
    dispatch(
      setViewedReservation(
        reservation.reservationId,
        () => navigation.navigate("Reservation Details"),
        setLoading
      )
    );
  };

  return (
    <Block
      flex
      card
      style={{
        backgroundColor: "white",
        width: width,
        marginTop: 5,
        paddingTop: 10,
        paddingBottom: 0,
        paddingLeft: 10,
        paddingRight: 10,
        elevation: 0,
        borderRadius: 0
      }}
    >
      <TouchableOpacity onPress={viewReservationDetails}>
        <Block
          flex
          row
          style={{ alignItems: "center", marginBottom: 5 }}
          space={"between"}
        >
          <Block>
            <Text h5 bold style={{ color: "black" }}>
              {moment(reservation.reservationDateTime).format(
                "D MMM YYYY h:mm A"
              )}
            </Text>
            <Text h5 style={{color: "#404040"}}>
              <Text bold>Location:</Text> {reservation.store.storeName}
            </Text>
            <Text h5 style={{color: "#404040"}}>
              <Text bold>Attended:</Text> {reservation.attended ? "Yes" : "No"}
            </Text>
          </Block>
          <MaterialIcons
            name={"keyboard-arrow-right"}
            color={"grey"}
            size={40}
          />
        </Block>
      </TouchableOpacity>
      <Divider style={{ height: 1.5 }} />
      <Text h6 bold style={{ marginBottom: 8, marginTop: 5 }}>
        {reservation.productVariants.length} item(s)
      </Text>
      <ScrollView
        horizontal={true}
        nestedScrollEnabled={true}
        showsHorizontalScrollIndicator={false}
        fadingEdgeLength={100}
        style={{ marginBottom: 10 }}
      >
        {reservation.productVariants.map(productVariant => (
          <Image
            key={productVariant.productVariantId}
            style={{ width: width * 0.3, height: 150, marginRight: 10 }}
            resizeMethod="resize"
            resizeMode="contain"
            source={{
              uri: productVariant.productImages[0].productImageUrl
            }}
          />
        ))}
      </ScrollView>
    </Block>
  );
}

export default ReservationCard;
