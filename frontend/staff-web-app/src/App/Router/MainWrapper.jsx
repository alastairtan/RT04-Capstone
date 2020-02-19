import React, { PureComponent } from 'react';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';
import classNames from 'classnames';
import * as PropTypes from 'prop-types';
import { CustomizerProps, ThemeProps, RTLProps } from '../../shared/prop-types/ReducerProps';

class MainWrapper extends PureComponent {
  static propTypes = {
    children: PropTypes.element.isRequired,
    location: PropTypes.shape({
      pathname: PropTypes.string,
    }).isRequired,
  };

  render() {
    const {
      children, location
    } = this.props;
    
    return (
      <div className="theme-light ltr-support" dir="ltr">
        <div className="wrapper blocks-with-shadow-theme">
          {children}
        </div>
      </div>
    );
  }
}

export default withRouter(MainWrapper);
