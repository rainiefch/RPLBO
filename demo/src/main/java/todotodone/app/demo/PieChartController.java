package todotodone.app.demo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;

import java.util.ArrayList;

public class PieChartController {
    @FXML private PieChart pieChart;

    @FXML
    public  void initialize() {
        pieChart.setLegendVisible(false);
        pieChart.setAnimated(false);
        pieChart.setLabelsVisible(true);

        PieChart.Data data1 = new PieChart.Data("A", 30);
        PieChart.Data data2 = new PieChart.Data("B", 40);
        PieChart.Data data3 = new PieChart.Data("C", 60);
//        ObservableList pieChartDatas = FXCollections.observableArrayList(data1, data2, data3);
        pieChart.getData().clear();
        pieChart.getData().add(data1);
        pieChart.getData().add(data2);
        pieChart.getData().add(data3);
    }
}
