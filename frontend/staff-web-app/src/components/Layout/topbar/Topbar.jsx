import React, { PureComponent } from "react";
import { Link } from "react-router-dom";
import * as PropTypes from "prop-types";
import TopbarSidebarButton from "./TopbarSidebarButton";
import TopbarProfile from "./TopbarProfile";
import TopbarMail from "./TopbarMail";
import TopbarNotification from "./TopbarNotification";
import TopbarSearch from "./TopbarSearch";

class Topbar extends PureComponent {
  static propTypes = {
    changeMobileSidebarVisibility: PropTypes.func.isRequired,
    changeSidebarVisibility: PropTypes.func.isRequired,
    loggedInStaff: PropTypes.object,
  };

  render() {
    const {
      changeMobileSidebarVisibility,
      changeSidebarVisibility,
    } = this.props;

    return (
      <div className="topbar">
        <div className="topbar__wrapper">
          <div className="topbar__left">
            <TopbarSidebarButton
              changeMobileSidebarVisibility={changeMobileSidebarVisibility}
              changeSidebarVisibility={changeSidebarVisibility}
            />

            <Link to="/dashboard">
              <img
                style={{ width: "50%" }}
                src="https://res.cloudinary.com/rt04capstone/image/upload/v1583433855/rsz_1apricot-nut-logo-word_jzhocy.png"
              />
            </Link>
          </div>
          <div className="topbar__right">
            {/* <TopbarSearch />
            <TopbarNotification />
            <TopbarMail new /> */}
            <TopbarProfile loggedInStaff={this.props.loggedInStaff} />
          </div>
        </div>
      </div>
    );
  }
}

export default Topbar;
