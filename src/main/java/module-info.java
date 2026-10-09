module com.personalfinance.dashboard {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.personalfinance.dashboard to javafx.fxml;
    exports com.personalfinance.dashboard;
}
