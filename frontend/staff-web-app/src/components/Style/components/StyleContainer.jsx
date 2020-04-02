import React, { Component } from "react";
import StyleDetails from "./StyleDetails";
import StyleForm from "./StyleForm";
import {
  createNewStyle,
  retrieveAllStyles,
  updateStyle
} from "../../../redux/actions/styleAction";
import { clearErrors } from "../../../redux/actions";
import connect from "react-redux/es/connect/connect";
import withPage from "../../Layout/page/withPage";
import CreateUpdateStyleRequest from "../../../models/CreateUpdateStyleRequest";
import * as PropTypes from "prop-types";
import StyleTable from "./StyleTable";
import { Grid } from "@material-ui/core";

class StyleContainer extends Component {
  static propTypes = {
    mode: PropTypes.oneOf(["viewAll", "viewOne"]),
    currentStyle: PropTypes.object,
    errors: PropTypes.object,
    clearErrors: PropTypes.func.isRequired,
    createNewStyle: PropTypes.func,
    updateStyle: PropTypes.func
  };

  componentDidMount() {
    const { mode, history } = this.props;
    if (mode === "viewOne") {
      // const storeId = this.props.match.params.storeId;
      // this.props.retrieveTagById(tagId, history);
    }
  }

  // this method makes the api calls
  handleSubmit = (e, formState) => {
    e.preventDefault();
    const { styleName } = formState;
    console.log(formState);
    const req = new CreateUpdateStyleRequest(styleName);

    switch (this.props.mode) {
      case "viewAll":
        this.props.createNewStyle(req, this.props.history);
        this.props.retrieveAllStyles();
        formState.styleName = "";
        break;
      case "viewOne":
        req.styleId = this.props.currentStyle.styleId;
        this.props.updateStyle(req, this.props.history);
        break;
      default:
    }
  };

  render() {
    const { errors, clearErrors, mode, currentStyle, renderLoader } = this.props;
    const header =
      mode === "viewAll"
        ? "Style Information"
        : mode === "viewOne"
        ? "Style Information"
        : "";
    return (
      <React.Fragment>
        <div className="card__title">
          <h5 className="bold-text">{header}</h5>
        </div>
        {mode === "viewAll" ? (
          //View All, Create, Update, Delete
          <div>
            <Grid container spacing={3}>
              <Grid item xs={12} md={3}>
                <StyleForm
                  handleSubmit={this.handleSubmit}
                  clearErrors={clearErrors}
                  errors={errors}
                  history={this.props.history}
                />
              </Grid>
              <Grid item xs={12} md={9}>
                <StyleTable
                  history={this.props.history}
                  renderLoader={renderLoader}
                />
              </Grid>
            </Grid>
          </div>
        ) : currentStyle !== null ? (
          <StyleDetails /> //View One Style Details
        ) : (
          renderLoader()
        )}
      </React.Fragment>
    );
  }
}

//mapping global state to this component
const mapStateToProps = state => ({
  errors: state.errors
});

const mapDispatchToProps = {
  createNewStyle, 
  updateStyle,
  clearErrors,
  retrieveAllStyles
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withPage(StyleContainer, "Style Management"));
