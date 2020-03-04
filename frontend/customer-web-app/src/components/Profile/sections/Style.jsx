import React, { useEffect, useRef, useState } from "react";
import { clearErrors } from "redux/actions";
import { useDispatch, useSelector } from "react-redux";
import signupPageStyle from "assets/jss/material-kit-pro-react/views/signupPageStyle";
import { makeStyles } from "@material-ui/core/styles";
import Button from "components/UI/CustomButtons/Button";
import { useSnackbar } from "notistack";
import {
  addStylePreferences,
  deleteStylePreferences,
  updateStylePreferences
} from "redux/actions/customerActions";
const useStyles = makeStyles(signupPageStyle);
const _ = require("lodash");

//Quiz Questions
const questionBank = [
  {
    id: 1,
    question: "Which clothing color scheme do you love",
    answers: [
      "Shades of green and pink",
      "Earth tone",
      "Basics",
      "Bright & bold",
      "Black & neutral"
    ]
  },
  {
    id: 2,
    question: "Describe your clothing style",
    answers: [
      "Retro",
      "Loose-flowing clothes",
      "Modern & sleek",
      "Exaggerated prints",
      "Elegant & smart"
    ]
  },
  {
    id: 3,
    question: "Describe your personality",
    answers: ["Nostalgic", "Liberal", "Direct", "Creative", "Confident"]
  }
];

function Style(props) {
  //Hooks
  const classes = useStyles();
  const { enqueueSnackbar, closeSnackbar } = useSnackbar();

  //Redux
  const dispatch = useDispatch();
  const errors = useSelector(state => state.errors);
  const customer = useSelector(state => state.customer.loggedInCustomer);

  //State
  const [inputState, setInputState] = useState({
    question1: questionBank[0].question,
    question2: questionBank[1].question,
    question3: questionBank[2].question,
    answer1: questionBank[0].answers,
    answer2: questionBank[1].answers,
    answer3: questionBank[2].answers,
    answers: [],
    error: "",
    test: [],
    style: ""
  });

  const [hasAddStyle, setAddedStyle] = useState(false);
  const [updateMode, setUpdateMode] = useState(false);
  const [button1NotClicked, setButton1NotClicked] = useState(true);
  const [button2NotClicked, setButton2NotClicked] = useState(true);
  const [button3NotClicked, setButton3NotClicked] = useState(true);

  useEffect(() => {
    if (customer.style != null) {
      if (hasAddStyle == false) {
        //when customer logged in
        setInputState(inputState => ({
          ...inputState,
          style: customer.style.styleName,
          answer1:
            questionBank[0].answers[customer.stylePreference.split(",")[0]],
          answer2:
            questionBank[1].answers[customer.stylePreference.split(",")[1]],
          answer3:
            questionBank[2].answers[customer.stylePreference.split(",")[2]]
        }));
      }
      console.log(customer);
      setInputState(inputState => ({
        ...inputState,
        style: customer.style.styleName
      }));
      setAddedStyle(true);
      console.log(inputState);
    }
  }, [customer]);

  const handleSelectAnswer1 = (text, index) => {
    setButton1NotClicked(false);
    setInputState(inputState => ({
      ...inputState,
      answer1: [text]
    }));
    inputState.answers.push(index);
  };

  const handleSelectAnswer2 = (text, index) => {
    setButton2NotClicked(false);
    setInputState(inputState => ({
      ...inputState,
      answer2: [text]
    }));
    inputState.answers.push(index);
  };

  const handleSelectAnswer3 = (text, index) => {
    setButton3NotClicked(false);
    setInputState(inputState => ({
      ...inputState,
      answer3: [text]
    }));
    inputState.answers.push(index);
  };

  const handleSubmitStylePreferences = () => {
    if (button3NotClicked) {
      enqueueSnackbar("Answer all questions", {
        variant: "error",
        autoHideDuration: 1200
      });
    } else {
      const customerId = customer.customerId;
      const stylePreference = inputState.answers.toString();
      const req = { customerId, stylePreference };
      dispatch(addStylePreferences(req, enqueueSnackbar, setAddedStyle));
      setAddedStyle(true);
      setButton1NotClicked(true);
      setButton2NotClicked(true);
      setButton3NotClicked(true);
    }
  };

  const updateStyle = () => {
    setInputState(inputState => ({
      ...inputState,
      answers: [],
      answer1: questionBank[0].answers,
      answer2: questionBank[1].answers,
      answer3: questionBank[2].answers
    }));
    setAddedStyle(false);
    setUpdateMode(true);
  };

  const cancelUpdateStylePreferences = () => {
    setAddedStyle(true);
    setUpdateMode(false);
    console.log(customer);
    const chosenAns1 =
      questionBank[0].answers[customer.stylePreference.split(",")[0]];
    const chosenAns2 =
      questionBank[1].answers[customer.stylePreference.split(",")[1]];
    const chosenAns3 =
      questionBank[2].answers[customer.stylePreference.split(",")[2]];
    setInputState(inputState => ({
      ...inputState,
      answer1: [chosenAns1],
      answer2: [chosenAns2],
      answer3: [chosenAns3]
    }));
    console.log(inputState);
  };

  const cancelSubmitStylePreferences = () => {
    setInputState(inputState => ({
      ...inputState,
      answers: [],
      answer1: questionBank[0].answers,
      answer2: questionBank[1].answers,
      answer3: questionBank[2].answers
    }));
    setButton1NotClicked(true);
    setButton2NotClicked(true);
    setButton3NotClicked(true);
  };

  const handleUpdateStylePreferences = () => {
    if (
      inputState.answer1.toString() ==
        questionBank[0].answers[customer.stylePreference.split(",")[0]] ||
      inputState.answer2.toString() ==
        questionBank[1].answers[customer.stylePreference.split(",")[1]] ||
      inputState.answer3.toString() ==
        questionBank[2].answers[customer.stylePreference.split(",")[2]]
    ) {
      enqueueSnackbar("No changes in style preferences", {
        variant: "error",
        autoHideDuration: 1200
      });
    } else if (button3NotClicked) {
      enqueueSnackbar("Answer all questions", {
        variant: "error",
        autoHideDuration: 1200
      });
    } else {
      const customerId = customer.customerId;
      const stylePreference = inputState.answers.toString();
      const req = { customerId, stylePreference };
      dispatch(updateStylePreferences(req, enqueueSnackbar, setAddedStyle));
      setAddedStyle(true);
      setButton1NotClicked(true);
      setButton2NotClicked(true);
      setButton3NotClicked(true);
    }
  };

  const handleDeleteStylePreferences = () => {
    setUpdateMode(false);
    const customerId = customer.customerId;
    const styleChosen = customer.style.styleName;
    const req = { customerId, styleChosen };
    dispatch(deleteStylePreferences(req, enqueueSnackbar, setAddedStyle));
    setInputState(inputState => ({
      ...inputState,
      answers: [],
      style: "",
      answer1: questionBank[0].answers,
      answer2: questionBank[1].answers,
      answer3: questionBank[2].answers
    }));
  };

  return (
    <div className={classes.textCenter}>
      <h4>Style Quiz</h4>
      <small>Receive recommendations on products that suit your style</small>
      {hasAddStyle ? ( //if added style
        <div>
          <div>
            <h5>
              <i>
                Your Style is <b>{inputState.style}</b>
              </i>
            </h5>
            <div>
              <h6>{inputState.question1}</h6>
              <Button color="rose">{inputState.answer1}</Button>
              <h6>{inputState.question2}</h6>
              <Button color="rose">{inputState.answer2}</Button>
              <h6>{inputState.question3}</h6>
              <Button color="rose">{inputState.answer3}</Button>
            </div>
            <div className={classes.textCenter} style={{ marginTop: 20 }}>
              <Button round color="primary" onClick={updateStyle}>
                Redo Style Quiz
              </Button>
              <Button
                round
                color="primary"
                onClick={handleDeleteStylePreferences}
              >
                Delete Style Preferences
              </Button>
            </div>
          </div>
        </div>
      ) : (
        <div>
          <form>
            <h6>{inputState.question1}</h6>
            {inputState.answer1.map((text, index) => (
              <Button
                key={index}
                color="rose"
                onClick={() => handleSelectAnswer1(text, index)}
              >
                {text}
              </Button>
            ))}
            <h6>{inputState.question2}</h6>
            {inputState.answer2.map((text, index) => (
              <Button
                key={index}
                disabled={button1NotClicked}
                color="rose"
                onClick={() => handleSelectAnswer2(text, index)}
              >
                {text}
              </Button>
            ))}
            <h6>{inputState.question3}</h6>
            {inputState.answer3.map((text, index) => (
              <Button
                key={index}
                disabled={button2NotClicked}
                color="rose"
                onClick={() => handleSelectAnswer3(text, index)}
              >
                {text}
              </Button>
            ))}
            <div className={classes.textCenter} style={{ marginTop: 20 }}>
              {updateMode ? (
                <React.Fragment>
                  <Button
                    onClick={handleUpdateStylePreferences}
                    round
                    color="primary"
                  >
                    Update Style Preferences
                  </Button>

                  <Button
                    onClick={cancelUpdateStylePreferences}
                    round
                    color="primary"
                  >
                    Cancel
                  </Button>
                </React.Fragment>
              ) : (
                <React.Fragment>
                  <Button
                    onClick={handleSubmitStylePreferences}
                    round
                    color="primary"
                  >
                    Find Out Your Style
                  </Button>

                  <Button
                    onClick={cancelSubmitStylePreferences}
                    disabled={button1NotClicked}
                    round
                    color="primary"
                  >
                    Cancel
                  </Button>
                </React.Fragment>
              )}
            </div>
          </form>
        </div>
      )}
    </div>
  );
}

export default Style;
