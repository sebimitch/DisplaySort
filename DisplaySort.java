/*
Program displaying a graphical view of step-by-step sorting of a list of numbers using common sorting algorithms.
Uses Java 8, which includes JavaFX
Tested on Java version: "1.8.0_231"
*/

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import java.util.Random;
import java.util.Arrays;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import javafx.scene.layout.Pane;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.application.Platform;
import java.lang.Math;
import javafx.geometry.Bounds;
import javafx.scene.shape.Line;
import java.util.ArrayList;
import java.util.HashMap;

public class DisplaySort extends Application {
    private int[] array;
    private int minTall = 20;
    private int maksTall = 1000;
    private long speedValue = 5;
    private TextArea textArea = new TextArea();
    private Line pivotLine;
    private Pane pane;
    private HashMap<Integer, String> colours = new HashMap<Integer, String>();

    private CategoryAxis xAxis = new CategoryAxis();
    private NumberAxis yAxis = new NumberAxis(0.0, 1000.0, 50.0);   // lowerBound, upperBound, tickUnit
    private BarChart<String, Number> barChart = new BarChart<String, Number>(xAxis, yAxis);
    private XYChart.Series<String, Number> dataSeries = new XYChart.Series<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        /* Sets up the GUI, and adds event handlers */

        barChart.getXAxis().setTickLabelsVisible(false); barChart.getXAxis().setOpacity(0); // remove label on xAxis
        barChart.setAnimated(false);
        barChart.setLegendVisible(false);
        yAxis.setLabel("Value");
        newArray(100);  //default starting point
        initiateChart();

        Label customArrayLabel = new Label("Custom array (for ex: '200,90,500,300,700,100')");
        TextField customArrayTextField = new TextField("");
        customArrayTextField.setPrefWidth(200);

        Button selectionButton = new Button("Selection Sort");
        selectionButton.setOnAction(e -> {
            // Threads are being used because I'm pausing the sorting algorithm, while the GUI is updating
            Thread thread = new Thread(() -> {
                selectionSort();
            });
            thread.start();
        });

        Button quickButton = new Button("Quick Sort");
        quickButton.setOnAction(e -> {
            Thread thread = new Thread(() -> {
                quickSort(0, array.length-1);
            });
            thread.start();
        });

        Button mergeButton = new Button("Merge Sort");
        mergeButton.setOnAction(e -> {
            Thread thread = new Thread(() -> {
                mergeSort(array);
                colours.clear();
                updateAndPause(1);
            });
            thread.start();
        });

        Button newButton = new Button("Generate New Array");
        newButton.setOnAction(e -> {
            colours.clear();
            if (!customArrayTextField.getText().equals("")) {
                readArray(customArrayTextField.getText());     // for ex. "3, 4, 1, 9, 0"
            } else {
                newArray(array.length);
            }
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

        Label speedLabel = new Label("Speed of sorting:");
        Slider speedSlider = new Slider(1, 10, speedValue);   //minValue, maxValue, initValue
        speedSlider.setShowTickLabels(true); speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(2); speedSlider.setMinorTickCount(1);

        speedSlider.valueProperty().addListener(e -> { // true while slider is being dragged/clicked
            long value = Math.round(speedSlider.getValue());
            speedSlider.setValue(value);

            if (!slider.isValueChanging()) { // activates when clicking on the slider
                speedValue = value;
            }
        });

        speedSlider.valueChangingProperty().addListener(e -> { // true at start/end of slider being dragged
            if (!speedSlider.isValueChanging()) { // true if slider is not currently changing
                long value = Math.round(speedSlider.getValue());
                speedSlider.setValue(value);
                speedValue = value;
            }
        });

        textArea.setWrapText(true);
        textArea.setEditable(false);
        textArea.setText("Array:\n" + Arrays.toString(array));

        // Ordering how the components are being displayed
        HBox sizeRow = new HBox(20, slider, sliderValue);
        HBox speedRow = new HBox(20, speedLabel, speedSlider);
        HBox sortRow = new HBox(20, selectionButton, quickButton, mergeButton);
        HBox newArrayRow = new HBox(20, customArrayLabel, customArrayTextField, newButton);
        pane = new Pane(barChart);
        VBox vbox = new VBox(20, pane, sizeRow, speedRow, textArea, newArrayRow, new HBox(20, new Label("Sort with:"), sortRow));

        // Makes the barChart scalable in the x direction
        vbox.widthProperty().addListener(e -> {
            barChart.setPrefWidth(vbox.getWidth());
        });

        Scene scene = new Scene(vbox);

        stage.setScene(scene);
        stage.setTitle("DisplaySort");
        stage.show();
    }

    private void initiateChart() {
        /* First setup of barChart, adds a dataSeries */
        for (int i = 0; i < array.length; i++) {
            dataSeries.getData().add(new XYChart.Data(Integer.toString(i), array[i]));
        }
        barChart.getData().add(dataSeries);
    }

    private void updateChart() {
        /* Removes current chart data, and adds the updated array */
        dataSeries.getData().clear();
        for (int i = 0; i < array.length; i++) {
            // adds with default colour
            dataSeries.getData().add(new XYChart.Data(Integer.toString(i), array[i]));
        }
        textArea.setText("Array:\n" + Arrays.toString(array));

        // Colours index/colour pairs present in HashMap "colours"
        int index = 0;
        ArrayList<Integer> alreadyColoured = new ArrayList<Integer>();
        for (Node n : barChart.lookupAll(".default-color0.chart-bar")) {
            if (alreadyColoured.contains(index)) {
                continue;   // don't recolour a bar
            }
            if (colours.containsKey(index)) {
                String style = "-fx-bar-fill: "+ colours.get(index) + ";";
                n.setStyle(style);
                alreadyColoured.add(index);
            }
            index += 1;
        }
    }

    private void newArray(int size) {
        /* Updates "array" with a new int array of size "size", and the text field */
        array = new int[size];
        Random random = new Random();
        for (int i = 0; i < array.length; i++) {
            // Number in range [minTall, maksTall]
            int num = random.nextInt(maksTall - minTall) + minTall;
            // don't want a number that is already in the array, makes displaying mergeSort simpler
            while (intInArray(num, array)) {
                num = random.nextInt(maksTall - minTall) + minTall;
            }
            array[i] = num;
        }
        textArea.setText("Array:\n" + Arrays.toString(array));
    }

    private void readArray(String s) {
        /* Converts a string s with format "x, x2, x3" into an int[] */
        s = s.replaceAll("\\s+", "");   // remove all whitespaces
        String[] strArray = s.split(",");
        array = new int[strArray.length];
        for (int i = 0; i < array.length; i++) {
            array[i] = Integer.parseInt(strArray[i]);
        }
    }

    private Boolean intInArray(int key, int[] list) {
        /* Checks if "list" contains "key" */
        for (int value : list) {
            if (key == value) {
                return true;
            }
        }
        return false;
    }

    private void printArray() {
        /* Prints a int[] array to terminal */
        System.out.println(Arrays.toString(array));
    }

    private void pause(int howManyPauses) {
        /* Pauses thread for x milliseconds, where x is decided by the speedValue slider.
           The variable howManyPauses can be used to make the pauses longer */
        try {
            long x = speedValue * 40;
            for (int i = 0; i < howManyPauses; i++) {
                Thread.sleep(x);
            }
        } catch (InterruptedException e) {
            System.out.println("Sleep interrupted");
            System.exit(-1);
        }
    }

    private void updateAndPause(int howManyPauses) {
        /* Runs both updateChart and pause(howManyPauses) */
        Platform.runLater(() -> updateChart());
        pause(howManyPauses);
    }

    private void swap(int index1, int index2) {
        /* Swaps the elements at index1 and index2 in "array", and displays the swap with
           colours on the barChart with pauses so one can see what is happening */
        Platform.runLater(() -> colours.put(index1, "blue"));
        Platform.runLater(() -> colours.put(index2, "blue"));
        updateAndPause(1);

        int temp = array[index1];
        array[index1] = array[index2];
        array[index2] = temp;
        updateAndPause(1);

        Platform.runLater(() -> colours.remove(index1));
        Platform.runLater(() -> colours.remove(index2));
        updateAndPause(1);
    }

    private void selectionSort() {
        /* Sorts "array" using the Selection Sort algorithm
           Translated from pseudo-code to Java: Algorithm 5.3 p. 160 in the book
           "Algorithm Design and Applications" - Michael T. Goodrich, Roberto Tamassia (ISBN 978-1-118-33591-8, 2014) */
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
                // Swap array[i] and array[index]
                swap(i, index);
            }
        }
    }

    private void quickSort(int left, int right) {
        /* Sorts "array" using the QuickSort algorithm
        Translated from pseudo-code to Java,
        source: https://en.wikipedia.org/wiki/Quicksort#Hoare_partition_scheme */
        if (left < right) {
            int pivot = partition(left, right);
            quickSort(left, pivot);
            quickSort(pivot+1, right);
        }
    }

    private int partition(int left, int right) {
        /* Sorts "array" using the QuickSort algorithm
        Translated from pseudo-code to Java,
        source: https://en.wikipedia.org/wiki/Quicksort#Hoare_partition_scheme */
        int pivotIndex = (left + right) / 2;
        int pivot = array[pivotIndex];
        Platform.runLater(() -> colours.put(pivotIndex, "green"));
        showPivotLine(pivotIndex);
        updateAndPause(1);

        int i = left - 1;
        int j = right + 1;

        while (true) {
            do {
                i += 1;
            }
            while (array[i] < pivot);
            do {
                j -= 1;
            }
            while (array[j] > pivot);
            if (i >= j) {
                Platform.runLater(() -> colours.remove(pivotIndex));
                removeLine();
                updateAndPause(1);
                return j;
            }
            swap(i, j);
        }
    }

    private int[] mergeSort(int[] list) {
        /* Sorts "list" by recursively dividing the input into two halves until the sublists
           are trivially sorted (1 element), and then merges them into one sorted list with merge().
           Inspired by TopDownMergeSort2() on: https://en.wikipedia.org/wiki/Merge_sort*/
        if (list.length == 1) { // basic case
            return list; // already sorted
        }else if (list.length == 0) {
            return null;
        }

        // Splits array in 2
        int middle = list.length / 2;
        int[] left = Arrays.copyOfRange(list, 0, middle);
        int[] right = Arrays.copyOfRange(list, middle, list.length);

        // Sort left and right part, recursively
        left = mergeSort(left);
        right = mergeSort(right);

        // Give left green colour, and right blue colour
        int index = findEarliestIndex(left);
        for (int i = 0; i < left.length; i++) {
            final int t = index + i;
            Platform.runLater(() -> colours.put(t, "green"));
        }
        index = findEarliestIndex(right);
        for (int i = 0; i < right.length; i++) {
            final int t = index + i;
            Platform.runLater(() -> colours.put(t, "blue"));
        }
        updateAndPause(1);

        // Merge the 2 sorted lists
        return merge(left, right);
    }

    private int[] merge(int[] part1, int[] part2) {
        /* Merges two sorted arrays part1 and part2, into a sorted array, and returns it
           Inspired by algorithm 8.3 p.245 in the book:
           "Algorithm Design and Applications" - Michael T. Goodrich, Roberto Tamassia (ISBN 978-1-118-33591-8, 2014)*/
        int i = 0; int j = 0;
        int n1 = part1.length; int n2 = part2.length;
        int[] result = new int[n1 + n2];

        // Find the corresponding earliest index in variable "array" for the elements in part1/part2
        // With this index "array" can be updated with correct values
        int correspondingIndex = findEarliestIndex(concatenateArray(part1, part2));

        while (i < n1 && j < n2) {
            if (part1[i] <= part2[j]) {
                result[i + j] = part1[i];
                i++;
            }
            else {
                result[i + j] = part2[j];
                j++;
            }
        }
        while (i < n1) {
            result[i + j] = part1[i];
            i++;
        }
        while (j < n2) {
            result[i + j] = part2[j];
            j++;
        }

        updateAndPause(1);
        for (int u = 0; u < result.length; u++) {
            array[correspondingIndex + u] = result[u];
        }
        updateAndPause(1);
        Platform.runLater(() -> colours.clear());
        return result;
    }

    private int findEarliestIndex(int[] numbers) {
        /* Returns the smallest index in "array" for a number present in "numbers" */
        int earliestIndex = array.length - 1;
        for (int i = 0; i < array.length; i++) {
            for (int element : numbers) {
                if (element == array[i]) {
                    if (i < earliestIndex) {
                        earliestIndex = i;
                    }
                }
            }
        }
        return earliestIndex;
    }

    private int[] concatenateArray(int[] a, int[] b) {
        /* Return the int[] sum of a and b */
        int[] result = new int[a.length + b.length];
        int index = 0;
        for (int elem : a) {
            result[index] = elem;
            index += 1;
        }
        for (int elem : b) {
            result[index] = elem;
            index += 1;
        }
        return result;
    }

    private void showPivotLine(int pivotIndex) {
        /* Draws a line on the value of the pivot */
        int i = 0;
        for (Node n : barChart.lookupAll(".default-color0.chart-bar")) {
            if (i == pivotIndex) {
                Bounds boundsBar = n.localToScene(n.getBoundsInLocal());
                double y = boundsBar.getMinY(); // height of pivot element

                Bounds boundsAxis = xAxis.localToScene(xAxis.getBoundsInLocal());
                double lineStart = boundsAxis.getMinX(); // x starting coordinate of xAxis

                double width = xAxis.getWidth();
                pivotLine = new Line(lineStart, y, lineStart + width, y);

                // Adds the line as a child of the pane where barChart is
                Platform.runLater(() -> pane.getChildren().add(pivotLine));
            }
            i += 1;
        }
    }

    private void removeLine() {
        /* Removes any children of "pane" that is not the barChart.
           Intended for removing pivot line from showPivotLine() */
        int size = pane.getChildren().size();
        if (size > 1) {
            Platform.runLater(() -> pane.getChildren().remove(1, size));
        }
    }
}
