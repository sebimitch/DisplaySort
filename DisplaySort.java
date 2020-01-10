/*

*/

import javafx.application.Application;
import javafx.stage.Stage; import javafx.scene.Scene;
import javafx.scene.control.Button;
import java.util.Random; import java.util.Arrays;
import java.awt.GraphicsDevice; import java.awt.GraphicsEnvironment;
import javafx.scene.layout.Pane;
import javafx.scene.Node;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.application.Platform;
import java.lang.Math;

public class DisplaySort extends Application {
    protected int[] array;
    private int minTall = 20;
    private int maksTall = 1000;
    private TextArea textArea = new TextArea();

    private CategoryAxis xAxis = new CategoryAxis();
    private NumberAxis yAxis = new NumberAxis(0.0, 1000.0, 50.0);   // lowerBound, upperBound, tickUnit
    final protected BarChart<String, Number> barChart = new BarChart<String, Number>(xAxis, yAxis);

    private XYChart.Series<String, Number> dataSeries = new XYChart.Series<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        // Sets up the GUI, and adds event handlers

        barChart.getXAxis().setTickLabelsVisible(false); barChart.getXAxis().setOpacity(0); // remove label on xAxis
        barChart.setAnimated(false);
        barChart.setLegendVisible(false);
        newArray(100);
        initiateChart();

        xAxis.setLabel("Array index");
        yAxis.setLabel("Value");

        Button selectionButton = new Button("Selection Sort");
        selectionButton.setOnAction(e -> {
            Thread thread = new Thread(() -> {
                selectionSort();
            });
            thread.start();
        });

        Button mergeButton = new Button("Merge Sort");
        mergeButton.setOnAction(e -> {

        });

        Button quickButton = new Button("Quick Sort");
        quickButton.setOnAction(e -> {

        });

        Button button = new Button("Generate New Array");
        button.setOnAction(e -> {
            newArray(array.length);
            updateChart();
        });

        Slider slider = new Slider(0, 200, array.length);   //minValue, maxValue, initValue
        slider.setPrefWidth(300);
        slider.setMajorTickUnit(50); slider.setMinorTickCount(1); slider.setBlockIncrement(1);
        slider.setShowTickLabels(true); slider.setShowTickMarks(true);
        Label sliderValue = new Label("Size of array: " + Integer.toString((int) slider.getValue()));

        slider.valueProperty().addListener(e -> { // true while slider is being dragged/clicked
            int value = (int) slider.getValue();
            slider.setValue(value);
            sliderValue.setText("Size of array: " + Integer.toString(value));

            if (!slider.isValueChanging()) { // activates when clicking on the slider
                newArray(value);
                updateChart();
            }
        });

        slider.valueChangingProperty().addListener(e -> { // true at start/end of slider being dragged
            if (!slider.isValueChanging()) { // true if slider is not currently changing
                int value = (int) slider.getValue();
                slider.setValue(value);
                newArray(value);
                updateChart();
            }
        });

        textArea.setWrapText(true);
        textArea.setEditable(false);
        textArea.setMouseTransparent(true);
        textArea.setFocusTraversable(false);
        textArea.setText("Array:\n" + Arrays.toString(array));

        HBox hbox = new HBox(20, slider, sliderValue);
        HBox sortRow = new HBox(20, selectionButton); // TODO quickButton, mergeButton
        VBox vbox = new VBox(20, barChart, hbox, textArea, button, new Label("Sort with:"), sortRow);

        Scene scene = new Scene(vbox);

        stage.setScene(scene);
        stage.setTitle("SortDisplay");
        stage.show();
    }

    private void initiateChart() {
        // First setup of barChart, adding a dataSeries
        for (int i = 0; i < array.length; i++) {
            dataSeries.getData().add(new XYChart.Data(Integer.toString(i), array[i]));
        }
        barChart.getData().add(dataSeries);
    }

    private void updateChart() {
        // Removes current chart data, and adds the updated array
        dataSeries.getData().clear();
        for (int i = 0; i < array.length; i++) {
            dataSeries.getData().add(new XYChart.Data(Integer.toString(i), array[i]));
        }
        textArea.setText("Array:\n" + Arrays.toString(array));
    }

    private void newArray(int size) {
        // Updates var array with a new int array of size "size", and text field
        array = new int[size];
        Random random = new Random();
        for (int i = 0; i < array.length; i++) {
            // Number in range [minTall, maksTall]
            array[i] = random.nextInt(maksTall - minTall) + minTall;
        }
        textArea.setText("Array:\n" + Arrays.toString(array));
    }

    private void colourBars(int[] indexes) {
        /*
        Colours all indexes given by array "indexes" to blue, the rest is coloured orange
        */
        int counter = 0;
        for (Node n : barChart.lookupAll(".default-color0.chart-bar")) {
            if (intInArray(counter, indexes)) { // checks that counter is in indexes
                n.setStyle("-fx-bar-fill: blue;");
            } else {
                n.setStyle(".default-color0.chart-bar");
            }
            counter += 1;
        }
    }

    private Boolean intInArray(int key, int[] list) {
        // checks if "list" contains "key".
        for (int value : list) {
            if (key == value) {
                return true;
            }
        }
        return false;
    }

    private void printArray() {
        System.out.println(Arrays.toString(array));
    }

    private void pause() {
        // Pauses thread for x milliseconds, where x is decided by size of array
        try {
            long x = Math.round(500 / array.length * 0.2) + 200;
            Thread.sleep(x);
        } catch (InterruptedException e) {
            System.out.println("Sleep interrupted");
            System.exit(-1);
        }
    }

    private void selectionSort() {
        /*
        Sorts "array" using the Selection Sort algorithm
        Translated from pseudo-code to Java ; Algorithm 5.3 page 160 in the book
        "Algorithm Design and Applications" - Michael T. Goodrich, Roberto Tamassia (ISBN 978-1-118-33591-8, 2014)
        */
        for (int i = 0; i < array.length-1; i++) {
            int index = i;

            // Finds the index of the smallest element in 'array'
            for (int j = i+1; j < array.length; j++) {
                if (array[j] < array[index]) {
                    index = j;
                }
            }
            // If i != index, then array[index] is smaller than array[i]
            // If i == index, then array[index] is already the smallest element in rest of array (from i)
            if (i != index) {
                colourBars(new int[]{i, index}); // set indexes about to swap to blue
                pause();

                // Swap array[i] and array[index]
                int temp = array[index];
                array[index] = array[i];
                array[i] = temp;

                Platform.runLater(() -> updateChart()); // update after swap (all orange)
                final int t = i; final int t2 = index;  // must be final in lambda
                Platform.runLater(() -> colourBars(new int[]{t, t2})); // set indexes that just were swapped to blue
                pause();

                Platform.runLater(() -> updateChart()); // set all bars to orange
                pause(); pause();
            }
        }
    }
}
