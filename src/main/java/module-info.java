module com.example.utsfinalnarkotika {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.utsfinalnarkotika to javafx.fxml;
    exports com.example.utsfinalnarkotika;
}