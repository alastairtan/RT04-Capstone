import React from "react";
import { Easing, Dimensions } from "react-native";
import { createStackNavigator } from "@react-navigation/stack";
import { createDrawerNavigator } from "@react-navigation/drawer";

import { Icon, Header } from "../components/";
import theme from "src/constants/Theme";

// screens

import LogIn from "src/screens/LogIn";
import Home from "src/screens/Home";
import { useSelector } from "react-redux";
import CustomDrawerContent from "src/navigation/CustomDrawerContent";
import Product from "src/screens/Product";
import CustomHeader from "src/components/CustomHeader";
import ProductDetails from "src/screens/ProductDetails/ProductDetails";
import { createMaterialBottomTabNavigator } from "@react-navigation/material-bottom-tabs";
import { MaterialCommunityIcons } from "react-native-vector-icons";

const { width } = Dimensions.get("screen");

const Stack = createStackNavigator();
const Drawer = createDrawerNavigator();
const Tab = createMaterialBottomTabNavigator();

export default function AppStack(props) {
  const customer = useSelector(state => state.customer.loggedInCustomer);
  return (
    <Stack.Navigator initialRouteName={customer ? "AppDrawer" : "Login"}>
      {!customer ? (
        <Stack.Screen
          name="Login"
          component={LogIn}
          options={{
            headerShown: false
          }}
        />
      ) : (
        <Stack.Screen
          name="AppDrawer"
          component={AppDrawer}
          options={{
            headerShown: false
          }}
        />
      )}
    </Stack.Navigator>
  );
}

function AppDrawer(props) {
  const { navigation: baseNavigation } = props;
  return (
    <Drawer.Navigator
      drawerContent={props => (
        <CustomDrawerContent {...props} baseNavigation={baseNavigation} />
      )}
      drawerContentOptions={{
        activeTintColor: "white",
        inactiveTintColor: "#000",
        activeBackgroundColor: theme.COLORS.ACTIVE,
        inactiveBackgroundColor: "transparent",
        itemStyle: {
          width: width * 0.74,
          paddingHorizontal: 12,
          // paddingVertical: 4,
          justifyContent: "center",
          alignContent: "center",
          // alignItems: 'center',
          overflow: "hidden"
        },
        labelStyle: {
          fontSize: 18,
          fontWeight: "normal"
        }
      }}
      initialRouteName="ShopStack"
    >
        <Drawer.Screen
            name="ShopStack"
            drawerLabel="Shop"
            component={ShopStack}
        />
      <Drawer.Screen
        name="ProfileStack"
        drawerLabel="Profile"
        component={ProfileStack}
      />
      {/*<Drawer.Screen name="TransactionStack" drawerLabel="Transactions" component={}/>*/}
    </Drawer.Navigator>
  );
}

function ProfileStack(props) {
  return (
    <Stack.Navigator mode="card" headerMode="screen">
      <Stack.Screen
        name="Profile"
        component={Home}
        options={{
          header: props => <CustomHeader title="Profile" {...props} />,
          headerStyle: { height: 100 }
        }}
      />
    </Stack.Navigator>
  );
}

function ShopStack(props) {
  return (
    <Stack.Navigator mode="card" headerMode="screen" initialRouteName="Shop">
      <Stack.Screen
        name="Shop"
        component={ProductScanTabs}
        options={{
          header: props => <CustomHeader title="Shop" {...props} />,
          headerStyle: { height: 100 }
        }}
      />
      <Stack.Screen
        name="Product Details"
        component={ProductDetails}
        options={{
          header: props => (
            <CustomHeader title="Product Details" back {...props} />
          )
        }}
      />
    </Stack.Navigator>
  );
}

function ProductScanTabs(props) {
  return (
    <Tab.Navigator
      initialRouteName="Add to Cart"
      activeColor={theme.COLORS.PRIMARY}
      barStyle={{ backgroundColor: theme.COLORS.CAPTION }}
    >
      <Tab.Screen
        name="Add to Cart"
        component={Product}
        options={{
          tabBarLabel: "Add to Cart",
          tabBarIcon: ({ color }) => (
            <MaterialCommunityIcons name="cart-plus" color={color} size={26} />
          )
        }}
      />
      <Tab.Screen
        name="View Details"
        component={Product}
        options={{
          tabBarLabel: "View Details",
          tabBarIcon: ({ color }) => (
            <MaterialCommunityIcons name="magnify" color={color} size={26} />
          )
        }}
      />
    </Tab.Navigator>
  );
}
