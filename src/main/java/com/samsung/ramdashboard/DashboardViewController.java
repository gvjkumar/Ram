package com.samsung.ramdashboard;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// A controller for FXML components ( controller classes is required for event handling of the javaFX components )
public class DashboardViewController implements Initializable {

    // For storing device serial no. that we get from 'device selection view controller'
    public String device_serial_no="";

    // for X-AXIS Limit to show until 10 data only otherwise graph will become cluttered after some time
    final int WINDOW_SIZE = 10;

    // Referencing FXML lineChart ID as a variable
    @FXML
    private LineChart<String, Number> systemChart;

    // Referencing FXML choiceBox ID as a variable - for adding option to set frequency for plotting the graph
    @FXML
    private ChoiceBox<String> frequencyChoice;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
//        usageBasedOnFrequency();

        //Plotting Graph Logic
        getSystemMemoryUsage();
    }

//    private void usageBasedOnFrequency() {
//        frequencyChoice.setValue("1s");
//        frequencyChoice.getItems().addAll("1s","10s","30s","1 min","5 min","10 min");
//
//        frequencyChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//            System.out.println("Selected item: " + newValue);
//            frequencyChoice.setValue(newValue);
//
//        });
//
//        if(frequencyChoice.getValue().equals("1s")){
//            Platform.runLater(()->{
//                getSystemMemoryUsage(1);
//            });
//        }else if(frequencyChoice.getValue().equals("10s")){
//            Platform.runLater(()->{
//                getSystemMemoryUsage(10);
//            });
//        }else if(frequencyChoice.getValue().equals("30s")){
//            Platform.runLater(()->{
//                getSystemMemoryUsage(30);
//                System.out.println("30");
//            });
//        }else if(frequencyChoice.getValue().equals("1 min")){
//            Platform.runLater(()->{
//                getSystemMemoryUsage(60);
//            });
//        }else if(frequencyChoice.getValue().equals("5 min")){
//            Platform.runLater(()->{
//                getSystemMemoryUsage(300);
//            });
//        }else{
//            Platform.runLater(()->{
//                getSystemMemoryUsage(600);
//            });
//        }
//    }

    public void setDeviceSerial(String device_serial_no){
        this.device_serial_no = device_serial_no;
    }

    private void getSystemMemoryUsage() {

        systemChart.getXAxis().setLabel("Time/s");
        systemChart.getYAxis().setLabel("Memory Usage (in kB)");

        //defining a series to display data
        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        series1.setName("MemTotal");
        XYChart.Series<String, Number> series2 = new XYChart.Series<>();
        series2.setName("MemFree");
        XYChart.Series<String, Number> series3 = new XYChart.Series<>();
        series3.setName("MemAvailable");
        XYChart.Series<String, Number> series4 = new XYChart.Series<>();
        series4.setName("SwapTotal");
        XYChart.Series<String, Number> series5 = new XYChart.Series<>();
        series5.setName("Cached");
        XYChart.Series<String, Number> series6 = new XYChart.Series<>();
        series6.setName("Buffers");

        // add series to chart
        systemChart.getData().addAll(series1,series2,series3,series4,series5,series6);
//        systemChart.setCreateSymbols(false);
        // this is used to display time in HH:mm:ss format
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

        // setup a scheduled executor to periodically put data into the chart
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        Runnable systemMemoryPlot = () -> {

            // Update the chart in the UI
            Platform.runLater(() -> {
                // get current time
                Date now = new Date();
                // put memory usage with current time
                series1.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), fetchYData(device_serial_no, 0)));
                series2.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), fetchYData(device_serial_no, 1)));
                series3.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), fetchYData(device_serial_no, 2)));
                series4.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), fetchYData(device_serial_no, 14)));
                series5.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), fetchYData(device_serial_no, 4)));
                series6.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), fetchYData(device_serial_no, 3)));

                // To remove first Element
                if (series1.getData().size() > WINDOW_SIZE)
                    series1.getData().remove(0);
                if (series2.getData().size() > WINDOW_SIZE)
                    series2.getData().remove(0);
                if (series3.getData().size() > WINDOW_SIZE)
                    series3.getData().remove(0);
                if (series4.getData().size() > WINDOW_SIZE)
                    series4.getData().remove(0);
                if (series5.getData().size() > WINDOW_SIZE)
                    series5.getData().remove(0);
                if (series6.getData().size() > WINDOW_SIZE)
                    series6.getData().remove(0);

            });
        };
        // put data onto graph per second
        scheduledExecutorService.scheduleAtFixedRate(systemMemoryPlot, 0, 1, TimeUnit.SECONDS);
    }


    public int fetchYData(String deviceSerialNo, int index) {
        try {

            int yData;
            // Create ProcessBuilder with the command and any arguments
            ProcessBuilder processBuilder = new ProcessBuilder("adb", "-s", deviceSerialNo, "shell","head", "-15", "/proc/meminfo");

            // Redirect the output to read the command's output
            processBuilder.redirectErrorStream(true);

            // Start the process
            Process process = processBuilder.start();

            // Read the output of the command
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String[] output = new String[15];
            for (int i = 0; (line = reader.readLine()) != null; i++) {
                output[i] = line;
                //System.out.println(output[i]);
            }

            Pattern pattern = Pattern.compile("\\d+");

            // Create a Matcher object with the test string
            Matcher matcher = pattern.matcher(output[index]);


            if (matcher.find()) {
                yData = Integer.parseInt(matcher.group());
            } else {
                yData = 0;
            }

            return yData;

        } catch (Exception e) {// IOException | InterruptedException e) {
//            e.printStackTrace();
            return 0;
        }

    }

}
