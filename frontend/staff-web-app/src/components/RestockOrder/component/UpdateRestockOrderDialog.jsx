import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogActions from "@material-ui/core/DialogActions";
import DialogContent from "@material-ui/core/DialogContent";
import Button from "@material-ui/core/Button";
import Chip from "@material-ui/core/Chip";
import Grid from "@material-ui/core/Grid";
import Typography from "@material-ui/core/Typography";
import Divider from "@material-ui/core/Divider";
import TextField from "@material-ui/core/TextField";
import MaterialTable from "material-table";
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
  ViewColumn,
  Visibility,
  Delete,
  FiberManualRecord
} from "@material-ui/icons";
import { useConfirm } from "material-ui-confirm";
import {
  updateRestockOrder,
  deleteRestockOrder,
  fulFillRestockOrder,
  getDeliveryStatusColour
} from "../../../redux/actions/restockOrderAction";
import StockIdQuantityMap from "../../../models/restockOrder/StockIdQuantityMap";

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

const UpdateRestockOrderDialog = ({ element, open, onClose, isWarehouse }) => {
  const dispatch = useDispatch();
  const confirmDialog = useConfirm();
  const [items, setItems] = useState([]);
  const {
    inStoreRestockOrderId: restockOrderId,
    inStoreRestockOrderItems,
    disableEdit,
    disableDelete,
    store,
    deliveryStatus,
    orderDateTime
  } = element;
  const { storeId, address } = store;
  const { buildingName, line1, line2, postalCode } = address;
  const disableInTransit =
    deliveryStatus === "DELIVERED" || deliveryStatus === "IN_TRANSIT";

  useEffect(() => {
    setItems(inStoreRestockOrderItems);
  }, []);

  let data = [];
  if (items) {
    data = items.map(e => {
      const {
        productStock,
        inStoreRestockOrderItemId,
        quantity,
        warehouseStockQuantity,
        deliveryStatus
      } = e;
      const { productStockId, productVariant } = productStock;
      return {
        productStockId: productStockId,
        productName: _.get(productVariant, "product.productName", ""),
        sku: _.get(productVariant, "sku", ""),
        quantity: quantity,
        image: _.get(productVariant, "productImages[0].productImageUrl", ""),
        inStoreRestockOrderItemId: inStoreRestockOrderItemId,
        warehouseStockQuantity: warehouseStockQuantity,
        deliveryStatus: deliveryStatus
      };
    });
  }

  // For store only
  const onChange = (e, index) => {
    const elements = [...items];
    const value = e.target.value.replace(/[^0-9]/g, "");
    elements[index].quantity = Number(value);
    setItems(elements);
  };

  // For store only
  const handleRemoveRestockOrder = index => {
    let elements = [...items];
    elements.splice(index, 1);
    setItems(elements);
  };

  // For store only
  const handleUpdateRestockOrder = () => {
    const stockIdQuantityMaps = [];
    if (items.length === 0) {
      confirmDialog({
        description: "No item in restock order, delete restock order?"
      })
        .then(() => {
          return dispatch(deleteRestockOrder(restockOrderId, onClose));
        })
        .catch(err => {});
    } else {
      items.map(item => {
        const { productStock, quantity: orderQuantity } = item;
        const { productStockId } = productStock;
        stockIdQuantityMaps.push(
          new StockIdQuantityMap(productStockId, orderQuantity)
        );
      });
      dispatch(
        updateRestockOrder({ restockOrderId, stockIdQuantityMaps }, storeId)
      );
    }
  };

  // For warehouse only
  const handleFulFillRestockOrder = () => {
    confirmDialog({
      description: `Confirm dispatch of stocks to ${buildingName}`
    })
      .then(() => {
        dispatch(fulFillRestockOrder(restockOrderId, onClose, buildingName));
      })
      .catch(err => {});
  };

  const columns = [
    { title: "Product stock ID.", field: "productStockId" },
    { title: "SKU", field: "sku" },
    { title: "Product name", field: "productName" },
    {
      title: "Image",
      field: "image",
      render: rowData => (
        <img
          style={{
            width: "50%",
            borderRadius: "10%"
          }}
          src={rowData.image}
        />
      )
    },
    {
      title: "Order quantity",
      field: "quantity",
      render: ({ productStockId, quantity, tableData }) => {
        if (isWarehouse || disableEdit || disableDelete) return quantity;
        return (
          <TextField
            name="orderQuantity"
            fullWidth
            margin="normal"
            value={quantity}
            onChange={e => {
              onChange(e, tableData.id);
            }}
            style={{ textAlign: "center" }}
            inputProps={{ style: { textAlign: "center" } }}
          />
        );
      }
    },
    {
      title: "Warehouse stock",
      field: "warehouseStockQuantity"
    },
    {
      title: "Warehouse stock status",
      // field: "deliveryStatus",
      render: ({ warehouseStockQuantity, quantity }) => {
        const insufficient = warehouseStockQuantity - quantity < 0;
        let style;
        if (insufficient) style = { color: "#e1282d" };
        else style = { color: "#33ba0a" };
        return <FiberManualRecord style={{ ...style, fontSize: 30 }} />;
      }
    },
    {
      title: "Line item delivery status",
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
    }
  ];

  return (
    <Dialog
      onClose={onClose}
      open={open}
      fullWidth
      maxWidth={isWarehouse ? "lg" : "md"}
    >
      <DialogTitle style={{ textAlign: "center" }}>Restock Order</DialogTitle>
      <DialogContent>
        <Grid container spacing={2}>
          <Grid item xs={9}>
            <Typography style={{ fontWeight: "bold", fontStyle: "italic" }}>
              {buildingName}
            </Typography>
            <Typography style={{ fontWeight: "bold", fontStyle: "italic" }}>
              {line1} {line2}
            </Typography>
            <Typography style={{ fontWeight: "bold", fontStyle: "italic" }}>
              {postalCode}, Singapore
            </Typography>
          </Grid>
          <Grid item xs={3} style={{ textAlign: "center" }}>
            <Chip
              style={{
                ...getDeliveryStatusColour(deliveryStatus),
                fontWeight: "bold",
                color: "white",
                marginBottom: "5%"
              }}
              label={deliveryStatus}
            />
            <Typography style={{ fontWeight: "bold" }}>
              {orderDateTime}
            </Typography>
          </Grid>
        </Grid>
        <Divider style={{ marginTop: "5%" }} />
        <MaterialTable
          title=""
          style={{ boxShadow: "none" }}
          icons={tableIcons}
          columns={
            isWarehouse
              ? columns
              : [
                  ...columns.splice(0, columns.length - 3),
                  columns[columns.length - 1]
                ]
          }
          data={data}
          options={{
            paging: false,
            headerStyle: { textAlign: "center" }, //change header padding
            cellStyle: {
              justifyContent: "center",
              textAlign: "center",
              alignItems: "center"
            },
            actionsColumnIndex: -1,
            draggable: false
          }}
          actions={[
            isWarehouse || disableEdit || disableDelete
              ? null
              : {
                  icon: Delete,
                  tooltip: "Delete",
                  onClick: (e, rowData) => {
                    const { tableData } = rowData;
                    confirmDialog({
                      description: "Remove item from restock order"
                    })
                      .then(() => {
                        handleRemoveRestockOrder(_.get(tableData, "id", ""));
                      })
                      .catch(err => {});
                  },
                  disabled: disableEdit || disableDelete,
                  iconProps: {
                    style: {
                      justifyContent: "center"
                    }
                  }
                }
          ]}
        />
      </DialogContent>
      <DialogActions>
        <Button autoFocus onClick={onClose} color="secondary">
          Cancel
        </Button>
        {isWarehouse ? (
          <Button
            color="primary"
            onClick={handleFulFillRestockOrder}
            disabled={disableInTransit}
          >
            Send out for delivery
          </Button>
        ) : (
          <Button
            color="primary"
            onClick={handleUpdateRestockOrder}
            disabled={disableEdit || disableDelete}
          >
            Update restock request
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
};

export default UpdateRestockOrderDialog;
