module es.um.tds.FirmaFx {
    requires javafx.controls;
    requires javafx.fxml;
	requires java.desktop;
	requires org.apache.pdfbox;
	requires javafx.swing;

    opens es.um.tds.FirmaFx to javafx.fxml;
    exports es.um.tds.FirmaFx;
}
