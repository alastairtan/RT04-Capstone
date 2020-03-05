import makeStyles from "@material-ui/core/styles/makeStyles";
import { useDispatch, useSelector } from "react-redux";
import { useSnackbar } from "notistack";
import { useHistory } from "react-router-dom";
import {
  cardLink,
  cardSubtitle,
  cardTitle
} from "../../assets/jss/material-kit-pro-react";
import React, { useEffect, useState } from "react";
import CardBody from "../UI/Card/CardBody";
import GridContainer from "../Layout/components/Grid/GridContainer";
import GridItem from "../Layout/components/Grid/GridItem";
import Button from "@material-ui/core/Button";
import {Add, Clear, Delete, Edit, Favorite, Reply, Star} from "@material-ui/icons";
import FormControlLabel from "@material-ui/core/FormControlLabel";
import Switch from "@material-ui/core/Switch";
import Card from "../UI/Card/Card";
import sectionCommentsStyle from "../../assets/jss/material-kit-pro-react/views/blogPostSections/sectionCommentsStyle";
import Media from "../Layout/components/Media/Media";
import Tooltip from "@material-ui/core/Tooltip";
import CustomInput from "../UI/CustomInput/CustomInput";
import Rating from "@material-ui/lab/Rating";
import Typography from "@material-ui/core/Typography";
import Box from "@material-ui/core/Box";
import {
  checkIfCanWriteReview,
  deleteReviewDetails
} from "../../redux/actions/reviewAction";
import AddEditReview from "./AddEditReview";

const useStyles = makeStyles(sectionCommentsStyle);

export default function ReviewCard(props) {
  const { reviews } = props;
  const classes = useStyles();

  // console.log(reviews);

  //Redux
  const dispatch = useDispatch();
  const history = useHistory();
  const { enqueueSnackbar, closeSnackbar } = useSnackbar();
  const [addNewReview, setAddNewReview] = useState(false);
  const [editReview, setEditReview] = useState(false);
  const [currReview, setCurrReview] = useState('');

  const currCustomer = useSelector(state => state.customer.loggedInCustomer);
  const currentProductId = useSelector(
    state => state.product.currentProductDetail.product.productId
  );
  useEffect(() => {
    if (currCustomer !== null) {
      // wait for transaction to be made then test
      // dispatch(checkIfCanWriteReview(currentProductId, currCustomer.customerId));
    }
  });

  const onDelete = item => {
    // console.log(item);
    dispatch(
      deleteReviewDetails(
        item.product.productId,
        item.reviewId,
        enqueueSnackbar
      )
    );
  };

  const handleAddNewReview = () => {
    setAddNewReview(!addNewReview);
  };
  const handleEditReview = () => {
    setEditReview(!editReview);
  };
  const handleCurrReview = (item) => {
    setCurrReview(item);
    console.log(item);
  };

  return (
    <div className={classes.section}>
      <GridContainer justify="center">
        <GridItem xs={12} sm={10} md={8}>
          <div>
            {addNewReview ? (
            <h3 className={classes.title}>
              Reviews
              <Button
                onClick={handleAddNewReview}
                style={{ position: "absolute", right: "15px" }}
              >
                <Clear></Clear>Cancel
              </Button>
            </h3>) :
              (<h3 className={classes.title}>
              Reviews
              <Button
                onClick={handleAddNewReview}
                style={{ position: "absolute", right: "15px" }}
              >
                <Add></Add>Add a review
              </Button>
            </h3>) }
            {addNewReview ? (
              <span>
                <AddEditReview reviews={reviews} editReview={editReview} currReview={currReview} setCurrReview={setCurrReview}/>
              </span>
            ) : (
              ""
            )}
            {currReview ? (
              <span>
                <AddEditReview reviews={reviews} editReview={editReview} currReview={currReview} setCurrReview={setCurrReview}/>
              </span>
            ) : (
              ""
            )}

            {reviews !== null
              ? reviews.map(function(item, i) {
                  // console.log(item);
                  const months = [
                    "January",
                    "February",
                    "March",
                    "April",
                    "May",
                    "June",
                    "July",
                    "August",
                    "September",
                    "October",
                    "November",
                    "December"
                  ];
                  const dateTime = new Date(item.createdDateTime);
                  const formatted_date =
                    dateTime.getDate() +
                    " " +
                    months[dateTime.getMonth()] +
                    " " +
                    dateTime.getFullYear();


                  if (item.staff) {
                    return (
                      <Media
                        key={i}
                        title={
                          <span>
                            {item.customer.firstName} {item.customer.lastName}{" "}
                            <small>· {formatted_date}</small>
                            {currCustomer !== null &&
                            item.customer.customerId ===
                              currCustomer.customerId ? (
                              <span style={{ float: "right" }}>
                                <Button>
                                  <Edit onClick={() => handleCurrReview(item)}/>
                                </Button>
                                <Button onClick={() => onDelete(item)}>
                                  <Delete />
                                </Button>
                              </span>
                            ) : (
                              ""
                            )}
                            <br />
                            <Rating
                              name="read-only"
                              value={item.rating}
                              readOnly
                              size="small"
                            />
                          </span>
                        }
                        body={
                          <p className={classes.color555}>{item.content}</p>
                        }
                        innerMedias={[
                          <Media
                            key={Date.now()}
                            title={
                              <span>
                                {item.staff.firstName} {item.staff.lastName}{" "}
                                <small>· {formatted_date}</small>
                              </span>
                            }
                            body={
                              <span className={classes.color555}>
                                {item.response}
                              </span>
                            }
                          />
                        ]}
                      />
                    );
                  } else {
                    return (
                      <Media
                        title={
                          <span>
                            {item.customer.firstName} {item.customer.lastName}{" "}
                            <small>· {formatted_date}</small>
                            {currCustomer !== null &&
                            item.customer.customerId ===
                              currCustomer.customerId ? (
                              <span style={{ float: "right" }}>
                                <Button>
                                  <Edit onClick={handleEditReview} />
                                </Button>
                                <Button onClick={() => onDelete(item)}>
                                  <Delete />
                                </Button>
                              </span>
                            ) : (
                              ""
                            )}
                            <br />
                            <Rating
                              name="read-only"
                              value={item.rating}
                              readOnly
                              size="small"
                            />
                          </span>
                        }
                        body={
                          <p className={classes.color555}>{item.content}</p>
                        }
                      />
                    );
                  }
                })
              : ""}
          </div>
        </GridItem>
      </GridContainer>
    </div>
  );
}
