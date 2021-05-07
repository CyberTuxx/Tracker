package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.TreeMap;

public class Controller implements Initializable {
    @FXML
    private ComboBox<String> combo_ct;
    @FXML
    private Button validate;
    @FXML
    private LineChart<LocalDate, Double> lineChart;
    @FXML
    private Button remove;
    @FXML
    private ComboBox<String> combo_dataset;
    @FXML
    private Button clear;

    private JSONObject data;
    private TreeMap<String, String> key_code_ct;
    private HashMap<String, XYChart.Series> map_ct_series;

    private String[] dataset = {"total_cases", "new_cases", "new_cases_smoothed", "total_deaths", "new_deaths",
            "new_deaths_smoothed", "total_cases_per_million", "new_cases_per_million", "new_cases_smoothed_per_million",
            "total_deaths_per_million", "new_deaths_per_million", "new_deaths_smoothed_per_million",
            "reproduction_rate", "icu_patients", "icu_patients_per_million", "hosp_patients", "hosp_patients_per_million",
            "weekly_icu_admissions", "weekly_icu_admissions_per_million", "weekly_hosp_admissions", "weekly_hosp_admissions_per_million",
            "total_tests", "new_tests", "total_tests_per_thousand", "new_tests_per_thousand", "new_tests_smoothed",
            "new_tests_smoothed_per_thousand", "positive_rate", "tests_per_case", "tests_units", "total_vaccinations",
            "people_vaccinated", "people_fully_vaccinated", "new_vaccinations", "new_vaccinations_smoothed",
            "total_vaccinations_per_hundred", "people_vaccinated_per_hundred", "people_fully_vaccinated_per_hundred",
            "new_vaccinations_smoothed_per_million", "stringency_index"};

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        get_data();
        key_code_ct = get_ct_list();
        combo_ct.getItems().addAll(key_code_ct.keySet());
        combo_dataset.getItems().addAll(dataset);
        map_ct_series = new HashMap<>();
        validate.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                ///lineChart.getData().clear();
                XYChart.Series s = make_line_charts(key_code_ct.get(combo_ct.getValue()), combo_ct.getValue(), combo_dataset.getValue());
                lineChart.layout();
                lineChart.getData().addAll(s);
            }
        });
        remove.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                lineChart.getData().remove(map_ct_series.get(key_code_ct.get(combo_ct.getValue().toString())+combo_dataset.getValue().toString()));
            }
        });
        clear.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                lineChart.getData().clear();
            }
        });
    }

    private void get_data(){
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader("C:\\Users\\Mathis\\Desktop\\COVID_Vaccine\\data.json"));

            // A JSON object. Key value pairs are unordered. JSONObject supports java.util.Map interface.
            data = (JSONObject) obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TreeMap<String, String> get_ct_list(){
        TreeMap<String, String> list = new TreeMap<>();
        data.forEach((t, u) -> {
            JSONObject d = (JSONObject) u;
            list.put(d.get("location").toString(), t.toString());
        });

        /**Iterator<JSONObject> iterator = companyList.iterator();
         while (iterator.hasNext()) {
         System.out.println(iterator.next());
         **/
        return list;
    }

    private XYChart.Series<LocalDate, Double> make_line_charts(String code_ct, String name, String value){
        JSONObject ct = (JSONObject) data.get(code_ct);
        XYChart.Series<LocalDate, Double> series = new XYChart.Series<>();
        series.setName(name);
        ArrayList<JSONObject> list = (ArrayList<JSONObject>) ct.get("data");
        TreeMap<LocalDate, Double> tMap = new TreeMap<>();
        list.forEach((key) -> {
            String date_str = key.get("date").toString();
            LocalDate date = LocalDate.parse(date_str);
            Double num = (Double) key.get(value);
            if(num != null){
                tMap.put(date, num);
                ///series.getData().add(new XYChart.Data(date_str, num));
            }
        });
        tMap.forEach((keys, values) -> {
            series.getData().add(new XYChart.Data(keys.toString(), values));
        });
        map_ct_series.put(code_ct+value, series);
        return series;
    }

    public ObservableList<XYChart.Data<LocalDate, Double>> plot(Double... y){
        final ObservableList<XYChart.Data<LocalDate, Double>> dataset = FXCollections.observableArrayList();
        int i = 0;
        while (i < y.length) {
            final XYChart.Data<LocalDate, Double> data = new XYChart.Data<LocalDate, Double>(LocalDate.parse("2020-12-12"), 2.3);
            data.setNode(
                    new HoveredThresholdNode(
                            22, 22
                    )
            );
            dataset.add(data);
            i++;
        }

        return dataset;
    }

    class HoveredThresholdNode extends StackPane {
        HoveredThresholdNode(int priorValue, int value) {
            setPrefSize(15, 15);

            final Label label = createDataThresholdLabel(priorValue, value);

            setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override public void handle(MouseEvent mouseEvent) {
                    getChildren().setAll(label);
                    setCursor(Cursor.NONE);
                    toFront();
                }
            });
            setOnMouseExited(new EventHandler<MouseEvent>() {
                @Override public void handle(MouseEvent mouseEvent) {
                    getChildren().clear();
                    setCursor(Cursor.CROSSHAIR);
                }
            });
        }

        private Label createDataThresholdLabel(int priorValue, int value) {
            final Label label = new Label(value + "");
            label.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line");
            label.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

            if (priorValue == 0) {
                label.setTextFill(Color.DARKGRAY);
            } else if (value > priorValue) {
                label.setTextFill(Color.FORESTGREEN);
            } else {
                label.setTextFill(Color.FIREBRICK);
            }

            label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
            return label;
        }
    }
}
