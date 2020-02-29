import React from "react";
import { makeStyles } from "@material-ui/core/styles";
import styles from "assets/jss/material-kit-pro-react/views/ecommerceSections/productsStyle";
import Checkbox from "@material-ui/core/Checkbox";
import { Check } from "@material-ui/icons";
import FormControlLabel from "@material-ui/core/FormControlLabel";

const useStyles = makeStyles(styles);

function CheckboxGroup(props) {
  const classes = useStyles();

  //checkedState is one of: coloursCheckedState, sizesCheckedState, tagsCheckedState
  const { list, checkedState, handleChange } = props;

  return (
    <React.Fragment>
      <div className={classes.customExpandPanel}>
        <div
          className={
            classes.checkboxAndRadio + " " + classes.checkboxAndRadioHorizontal
          }
        >
          {list.map((label, index) => (
            <FormControlLabel
              key={`${label}-${index}`}
              control={
                <Checkbox
                  tabIndex={-1}
                  onChange={handleChange(`${label}`)}
                  checked={checkedState[label]}
                  checkedIcon={<Check className={classes.checkedIcon} />}
                  icon={<Check className={classes.uncheckedIcon} />}
                  classes={{
                    checked: classes.checked,
                    root: classes.checkRoot
                  }}
                />
              }
              classes={{ label: classes.label }}
              label={label}
            />
          ))}
        </div>
      </div>
    </React.Fragment>
  );
}

export default CheckboxGroup;
