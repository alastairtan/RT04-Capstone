import React, { useEffect, useState } from "react";
import { useDispatch } from "react-redux";
import { useHistory } from "react-router-dom";
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogActions from "@material-ui/core/DialogActions";
import DialogContent from "@material-ui/core/DialogContent";
import Button from "@material-ui/core/Button";
import Chip from "@material-ui/core/Chip";
import Grid from "@material-ui/core/Grid";
import Select from "@material-ui/core/Select";
import MenuItem from "@material-ui/core/MenuItem";
import Divider from "@material-ui/core/Divider";
import InputLabel from "@material-ui/core/InputLabel";
import MaterialTable from "material-table";
import { useConfirm } from "material-ui-confirm";
import dateformat from "dateformat";
import {
  Add,
  AddBox,
  Check,
  ChevronLeft,
  ChevronRight,
  Clear,
  DeleteOutline,
  Edit,
  FirstPage,
  LastPage,
  Remove,
  SaveAlt,
  Search,
  ViewColumn
} from "@material-ui/icons";
import { confirmTransactionDelivery } from "../../../redux/actions/deliveryActions";
import { getDeliveryStatusColour } from "../../../redux/actions/restockOrderAction";

const _ = require("lodash");
const tableIcons = {
  Add: AddBox,
  Check: Check,
  Clear: Clear,
  Delete: DeleteOutline,
  DetailPanel: ChevronRight,
  Edit: Edit,
  Export: SaveAlt,
  Filter: Search,
  FirstPage: FirstPage,
  LastPage: LastPage,
  NextPage: ChevronRight,
  PreviousPage: ChevronLeft,
  ResetSearch: Clear,
  Search: Search,
  SortArrow: () => <div />,
  ThirdStateCheck: Remove,
  ViewColumn: ViewColumn
};

const TransactionDetailsDialog = ({ elements, open, onClose }) => {
  const dispatch = useDispatch();
  const history = useHistory();
  const confirmDialog = useConfirm();
  const [itemsByStore, setItemsByStore] = useState([]);
  const [stores, setStores] = useState([]);
  const [selectedStore, setSelectedStore] = useState("");

  useEffect(() => {
    const stores = elements.map(e => _.get(e, "storeToCollect.storeName", ""));
    if (stores) setStores(_.uniq(stores));
    setItemsByStore(elements);
  }, []);

  const data = itemsByStore.map(item => {
    let {
      transactionId,
      orderNumber,
      createdDateTime,
      collectionMode,
      totalQuantity,
      deliveryStatus,
      deliveryAddress,
      storeToCollect
    } = item;
    let date;
    if (createdDateTime)
      date = dateformat(new Date(createdDateTime), "dd'-'mmm'-'yyyy");
    const { line1, line2, postalCode } = deliveryAddress
      ? deliveryAddress
      : storeToCollect.address;
    const address = `${line1}, ${line2}${line2 ? "," : ""} ${postalCode}`;
    return {
      transactionId,
      orderNumber,
      createdDateTime: date,
      collectionMode: collectionMode.split("_").join(" "),
      totalQuantity,
      deliveryStatus: deliveryStatus.split("_").join(" "),
      deliveryAddress: address,
      storeToCollect
    };
  });

  const onSelectStore = ({ target: input }) => {
    const { value } = input;
    if (value === "") {
      setItemsByStore(elements);
      setSelectedStore(value);
    } else {
      setItemsByStore(
        elements.filter(
          item => _.get(item, "storeToCollect.storeName") === input.value
        )
      );
      setSelectedStore(value);
    }
  };

  const handleConfirmDelivery = data => {
    const transactionIds = data.map(e => e.transactionId);
    confirmDialog({
      description: "The selected transaction(s) will be marked as delivered"
    })
      .then(() => {
        dispatch(confirmTransactionDelivery({ transactionIds }));
        onClose();
      })
      .catch(() => null);
  };

  return (
    <Dialog onClose={onClose} open={open} fullWidth maxWidth={"md"}>
      <DialogTitle style={{ textAlign: "center" }}>Order details</DialogTitle>
      <DialogContent>
        <Grid container spacing={2}>
          <Grid item xs={false} md={10} />
          <Grid item xs={12} md={2}>
            <InputLabel>Select store: </InputLabel>
            <Select fullWidth defaultValue={""} onChange={onSelectStore}>
              <MenuItem key={""} value={""}>
                <span style={{ visibility: "hidden" }}>blank</span>
              </MenuItem>
              {stores.map(store => {
                return (
                  <MenuItem key={store} value={store}>
                    {store}
                  </MenuItem>
                );
              })}
            </Select>
          </Grid>
        </Grid>
        <Divider style={{ marginTop: "5%" }} />
        <MaterialTable
          title=""
          style={{ boxShadow: "none" }}
          icons={tableIcons}
          columns={[
            { title: "Transaction ID", field: "transactionId" },
            {
              title: "Order number",
              field: "orderNumber"
            },
            { title: "Created Date", field: "createdDateTime" },
            { title: "Collection mode", field: "collectionMode" },
            {
              title: "Delivery status",
              field: "deliveryStatus",
              render: ({ deliveryStatus }) => {
                const style = getDeliveryStatusColour(deliveryStatus);
                return (
                  <Chip
                    style={{ ...style, color: "white", width: "100%" }}
                    label={deliveryStatus}
                  />
                );
              }
            },
            {
              title: "Delivery address",
              field: "deliveryAddress"
            }
          ]}
          data={data}
          options={{
            paging: false,
            headerStyle: { textAlign: "center" }, //change header padding
            cellStyle: { textAlign: "center" },
            draggable: false,
            selection: !selectedStore ? false : true,
            actionsColumnIndex: -1
          }}
          actions={[
            selectedStore
              ? rowData => ({
                  icon: Add,
                  tooltip: "Confirm delivery",
                  onClick: (event, rowData) =>
                    handleConfirmDelivery(itemsByStore),
                  disabled: itemsByStore.every(
                    item => item.deliveryStatus === "DELIVERED"
                  )
                })
              : rowData => ({
                  icon: SaveAlt,
                  tooltip: "Confirm delivery",
                  onClick: (event, rowData) => handleConfirmDelivery([rowData]),
                  disabled: rowData.deliveryStatus === "DELIVERED"
                })
          ]}
        />
      </DialogContent>
      <DialogActions>
        <Button autoFocus onClick={onClose} color="secondary">
          Close
        </Button>
        {/* <Button
          color="primary"
          onClick={() => handleConfirmDelivery(itemsByStore)}
          disabled={
            itemsByStore.length === 0 ||
            itemsByStore[0].deliveryStatus === "DELIVERED" ||
            itemsByStore.every(item => item.status === "DELIVERED")
          }
        >
          Confirm delivery
        </Button> */}
      </DialogActions>
    </Dialog>
  );
};

export default TransactionDetailsDialog;
