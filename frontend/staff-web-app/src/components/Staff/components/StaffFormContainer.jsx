import React, { Component } from "react";
import "moment";
import * as PropTypes from "prop-types";
import withPage from "../../Layout/page/withPage";
import { css } from "@emotion/core";
import { ClipLoader } from "react-spinners";
import { connect } from "react-redux";
import StaffForm from "./StaffForm";
import { clearErrors, updateErrors } from "../../../redux/actions";
import {
    createNewStaff
} from "../../../redux/actions/staffActions";

