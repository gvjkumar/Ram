package com.samsung.ramdashboard;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// A controller for FXML components ( controller classes is required for event handling of the javaFX components )
public class DeviceSelectionViewController implements Initializable {

    // For adding options to checkBox
    @FXML
    private ChoiceBox<String> deviceChoice;

    // using this fxml-id as a variable
    @FXML
    private Button startBtn;

    // Invoked when refresh button clicked
    @FXML
    void refreshBtnClicked() {
        showChoices(); // shows the options present in the choiceBox
    }

    // Invoked when Start button clicked
    @FXML
    void startBtnClicked(ActionEvent event) throws IOException {

        //extracting value of chosen option of choiceBox
        String selected = deviceChoice.getValue();     //[ selected stores string of form 'deviceID+tabspace+status' ]

        //Storing the splitted substring ie. deviceID and status will be stored
        String[] line = selected.split("\\t");

        //Storing the deviceID in device_serial_no
        System.out.println("'"+line[0]+"'");
        String device_serial_no = line[0];

        // For loading new UI scene
        FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard-view.fxml"));
        Parent root = loader.load();

        // Accessing others controller to pass data from this scene to the new scene
        DashboardViewController dvc = loader.getController();
        dvc.setDeviceSerial(device_serial_no);

        // Showing new scene
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow() ;
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    //Initialize it when FXML file loaded
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        showChoices(); // For showing available Options of the choiceBox
    }

    private void showChoices() {
        String[] deviceList = showDeviceList(); // Store multiple device List

        //when this method called again first clear the old values and show the new values
        deviceChoice.getItems().clear();
        deviceChoice.getItems().addAll(deviceList);
        if(deviceList.length==1){
            //If there is one option available in choiceBox then set it as default
            deviceChoice.setValue(deviceList[0]);
        }else{
            //Otherwise  user will choose
            deviceChoice.setValue(" -- Choose -- ");
        }

        //Logic for choiceBox option selection
        deviceChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Selected item: " + newValue);
            deviceChoice.setValue(newValue);
        });

        //Logic to enable the start button if selected a valid Option ---->

        //extracting value of chosen option of choiceBox
        String selected = deviceChoice.getValue();     //[ selected stores string of form 'deviceID+tabspace+status' ]
        //Storing the splitted substring ie. deviceID and status will be stored
        String[] line = selected.split("\\t");
        //Storing the device status in device_serial_no
        try {
            String status = line[1];
            if(status.equals("device")){
                System.out.println(status);
                startBtn.setDisable(false);
            }
        }catch (Exception e){
            startBtn.setDisable(true);
        }

    }

    //Logic for device listing
    private String[] showDeviceList() {
        try {

            //For executing commands using JAVA
            ProcessBuilder processBuilder = new ProcessBuilder("adb", "devices");

            // Redirect the output to read the command's output
            processBuilder.redirectErrorStream(true);

            // Start the process
            Process process = processBuilder.start();

            // Read the output of the command
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            List<String> devicesList = new ArrayList<>();

            while((line = reader.readLine()) != null) {

                //There are different device statuses as well to show that they are connected but not usable..
                //So user might give required permission according to the status.
                Pattern pattern = Pattern.compile("device|unauthorized|no permissions|offline");
                Matcher matcher = pattern.matcher(line);

                if (matcher.find()) {
                    {
                        devicesList.add(line);
                    }
                }
                //System.out.println(devicesList);
            }
            devicesList.remove("List of devices attached "); // To remove this string from list
            devicesList.remove("");
            System.out.println(devicesList);

            return devicesList.toArray(new String[devicesList.size()]); // return string array
        }catch (Exception e){
            e.printStackTrace();
            return new String[0]; //If exception then return empty string
        }
    }

}