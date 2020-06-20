package view;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTimePicker;
import controller.Control;
import controller.account.AdminControl;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.existence.Discount;
import notification.Notification;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class DiscountProcessor extends Processor implements Initializable, changeTextFieldFeatures {
    private static AdminControl adminControl = AdminControl.getController();

    public Label discountCodeLabel;
    public Label maxDiscountLabel;
    public Label maxRepetitionLabel;
    public Label discountPercentLabel;
    public Label finishDateLabel;
    public Label startDateLabel;

    public JFXTextField discountCodeTextField;
    public JFXTextField maxDiscountTextField;
    public JFXTextField maxRepetitionTextField;
    public JFXTextField discountPercentTextField;

    public JFXDatePicker startDatePicker;
    public JFXTimePicker startTimePicker;

    public JFXDatePicker finishDatePicker;
    public JFXTimePicker finishTimePicker;

    public ImageView saveChangeButton;

    public BorderPane discountMainPane;
    public Pane discountInfoPane, discountCustomersPane;

    private Discount discount;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String locationFile = location.getFile();

        if(locationFile.contains("DiscountMenuInfo")) {
            setFieldsSpecifications();
        } else if(locationFile.contains("DiscountMenu")) {
            discountInfoMouseClicked(null);
            adminControl.addDiscountToHashMap(discount);
        }
    }


    private void setFieldsSpecifications() {
        setDoubleFields(discountPercentTextField, 100.000001);
        setIntegerFields(maxRepetitionTextField, Integer.MAX_VALUE);
        setDoubleFields(maxDiscountTextField, Double.MAX_VALUE);

        startTimePicker.set24HourView(true);
        finishTimePicker.set24HourView(true);
    }

    private void setDoubleFields(TextField priceTextField, double maxValue) {
        priceTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                //Todo Checking

                if(newValue.equals(".")) {
                    priceTextField.setText("0.");
                } else if (!newValue.matches("\\d+(.(\\d)+)?")) {
                    if(priceTextField.getText().contains(".")) {
                        priceTextField.setText(removeDots(priceTextField.getText()));
                    } else {
                        priceTextField.setText(newValue.replaceAll("[^\\d\\.]", ""));
                    }
                } else if(newValue.matches("\\d+(.(\\d)+)?") && Double.parseDouble(newValue) >= maxValue) {
                    //Todo checking
                    priceTextField.setText(oldValue);
                }

            }
        });
    }

    private void setIntegerFields(TextField priceTextField, Integer maxValue) {
        priceTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                //Todo Checking

                int newValueLength = newValue.length(), maxValueLength = Integer.toString(maxValue).length();

                if (!newValue.matches("\\d+")) {
                    priceTextField.setText(newValue.replaceAll("[^\\d]", ""));
                } else if(newValue.matches("\\d+") && (newValueLength > maxValueLength ||
                        (newValueLength == maxValueLength && newValue.compareTo(Integer.toString(maxValue)) >= 0))) {
                    priceTextField.setText(oldValue);
                }
            }
        });
    }


    private void setFields() {
        Discount mainDiscount = ((DiscountProcessor) parentProcessor).discount;

        if(mainDiscount == null) {
            ((DiscountProcessor) parentProcessor).discount = new Discount();
        } else {
            discountCodeTextField.setText(mainDiscount.getCode());

            if(mainDiscount.getDiscountPercent() != 0)
                discountPercentTextField.setText(Double.toString(mainDiscount.getDiscountPercent()));

            if(mainDiscount.getMaxRepetition() != 0)
                maxRepetitionTextField.setText(Integer.toString(mainDiscount.getMaxRepetition()));

            if(mainDiscount.getMaxDiscount() != 0)
                maxDiscountTextField.setText(Double.toString(mainDiscount.getMaxDiscount()));

            if(mainDiscount.getStartDate() != null)
                setDateFieldsFromDate(startDatePicker, startTimePicker, mainDiscount.getStartDate());

            if(mainDiscount.getFinishDate() != null)
                setDateFieldsFromDate(finishDatePicker, finishTimePicker, mainDiscount.getFinishDate());

            if(Control.getType().equals("Admin")) {
                if(mainDiscount.getID() != null) {
                    startDatePicker.setEditable(false);
                    startTimePicker.setEditable(false);
                }
            } else  {
                discountCodeLabel.setDisable(true);
                discountCodeTextField.setDisable(true);
                discountPercentLabel.setDisable(true);
                discountPercentTextField.setDisable(true);
                maxRepetitionLabel.setDisable(true);
                maxRepetitionTextField.setDisable(true);
                maxDiscountLabel.setDisable(true);
                maxDiscountTextField.setDisable(true);

                startDateLabel.setDisable(true);
                startDatePicker.setDisable(true);
                startTimePicker.setDisable(true);

                finishDateLabel.setDisable(true);
                finishDatePicker.setDisable(true);
                finishTimePicker.setDisable(true);

                Pane pane = (Pane)discountMainPane.getCenter();
                pane.getChildren().remove(saveChangeButton);
            }
        }
    }

    private void setDateFieldsFromDate(JFXDatePicker datePicker, JFXTimePicker timePicker, Date date) {
        LocalDateTime localDateTime = new Timestamp(date.getTime()).toLocalDateTime();
        datePicker.setValue(localDateTime.toLocalDate());
        timePicker.setValue(localDateTime.toLocalTime());
    }


    public void discountCustomersMouseClicked(MouseEvent mouseEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("TableViewMenu.fxml"));
            Parent root = loader.load();
            discountInfoPane.setStyle("");
            discountCustomersPane.setStyle("-fx-background-color: #90CAF9;   -fx-background-radius: 0 10 10 0;");

            TableViewProcessor processor = loader.getController();
            processor.setParentProcessor(this);
            processor.initProcessor(TableViewProcessor.TableViewType.DISCOUNT_CUSTOMERS);
            discountMainPane.setCenter(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void discountInfoMouseClicked(MouseEvent mouseEvent) {
        try {
            if(discountMainPane.getCenter() == null || discountMainPane.getCenter().getId().equals("mainBorderPane")) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("DiscountMenuInfo.fxml"));
                discountMainPane.setCenter(loader.load());
                discountCustomersPane.setStyle("");
                discountInfoPane.setStyle("-fx-background-color: #90CAF9;   -fx-background-radius: 0 10 10 0;");

                DiscountProcessor discountProcessor = loader.getController();
                discountProcessor.parentProcessor = this;
                discountProcessor.setFields();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void AddDiscountMouseClicked(MouseEvent mouseEvent) {
        //Todo Setting Notifications
        Notification notification = adminControl.addAddedDiscount(discount);

        Optional<ButtonType> optionalButtonType = notification.getAlert().showAndWait();

        if(optionalButtonType.get() == ButtonType.OK) {
            if(notification.equals(Notification.ADD_DISCOUNT)) {
                updateParentTable();
                this.myStage.close();
            } else
                discountInfoMouseClicked(null);
        }
    }

    public void saveChangesMouseClicked(MouseEvent mouseEvent) {
        Discount discount = ((DiscountProcessor) parentProcessor).discount;

        if(!isDateTimeEmpty(startDatePicker, startTimePicker)) {
            LocalDateTime localStartDateTime = LocalDateTime.of(startDatePicker.getValue(), startTimePicker.getValue());
            Date startDate = new Date(Timestamp.valueOf(localStartDateTime).getTime());
            discount.setStartDate(startDate);
        }

        if(!isDateTimeEmpty(finishDatePicker, finishTimePicker)) {
            LocalDateTime localFinishDateTime = LocalDateTime.of(finishDatePicker.getValue(), finishTimePicker.getValue());
            Date finishDate = new Date(Timestamp.valueOf(localFinishDateTime).getTime());
            discount.setFinishDate(finishDate);
        }

        if(!isTextFieldEmpty(discountCodeTextField))
            discount.setCode(discountCodeTextField.getText());
        if(!isTextFieldEmpty(discountPercentTextField))
            discount.setDiscountPercent(Double.parseDouble(discountPercentTextField.getText()));
        if(!isTextFieldEmpty(maxDiscountTextField))
            discount.setMaxDiscount(Double.parseDouble(maxDiscountTextField.getText()));
        if(!isTextFieldEmpty(maxRepetitionTextField))
            discount.setMaxRepetition(Integer.parseInt(maxRepetitionTextField.getText()));

        ((DiscountProcessor) parentProcessor).discountCustomersMouseClicked(null);
    }

    private boolean isDateTimeEmpty(JFXDatePicker datePicker, JFXTimePicker timePicker) {
        return datePicker.getValue() == null || timePicker.getValue() == null;
    }

    private boolean isTextFieldEmpty(TextField textField) {
        return textField.getText() == null || textField.getText().isEmpty();
    }


    public ArrayList<String> getDiscountAddedUsers() {
        return adminControl.getDiscountsAddedUsers().get(discount);
    }

    public boolean isAccountAddedInDiscount(String userName) {
        return adminControl.isUserAddedInDiscount(discount, userName);
    }

    public void addUserToDiscount(String userName) {
        adminControl.addUserToDiscountAddedUsers(discount, userName);
    }

    public void removeUserFromDiscount(String userName) {
        adminControl.removeUserFromDiscountAddedUsers(discount, userName);
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;

        //Todo Jesus
//        setFields();
    }

    @Override
    public void setMyStage(Stage myStage) {
        this.myStage = myStage;
        myStage.setOnCloseRequest(event -> {
            parentProcessor.removeSubStage(myStage);
            adminControl.removeDiscountFromHashMap(discount);
        });
    }

    public void updateParentTable() {
        TableViewProcessor<Discount> parentTableViewProcessor = (TableViewProcessor<Discount>) parentProcessor;
        parentTableViewProcessor.removeSubStage(myStage);
        parentTableViewProcessor.updateTable();
        parentTableViewProcessor.updateSelectedItem();
    }

}