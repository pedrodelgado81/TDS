package es.um.tds.FirmaFx;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import es.um.tds.FirmaFx.exception.CamposRequeridosException;
import es.um.tds.FirmaFx.exception.DniMalFormadoException;
import es.um.tds.FirmaFx.exception.NoFirmaException;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class FirmaApp extends Application {

	private double lastX, lastY;

	// Controles de validacion
	private List<TextInputControl> inputsAValidar = new ArrayList<TextInputControl>();
	private boolean firmaEmpezada = false;

	@Override
	public void start(Stage pantallaPrincipal) {
		// 8 digitos + letra
		int longitudDNI= 9;
		
		pantallaPrincipal.setTitle("Formulario con Firma y PDF");

		Label nombreLabel = new Label("Nombre:");

		// Linea de texto formada por los elementos indicados
		TextFlow contenedorNombreLabel = new TextFlow(nombreLabel, crearAsteriscoRojo());

		TextField nombreField = new TextField();
		// Asigno un ID para saber el nombre del campo cuando lo valide
		nombreField.setId("Nombre");
			
		inputsAValidar.add(nombreField);
		nombreField.setPromptText("Escriba su nombre y apellidos");

		Label dniLabel = new Label("DNI:");
		// Asigno un ID para saber el nombre del campo cuando lo valide
		TextFlow contenedorDniLabel = new TextFlow(dniLabel, crearAsteriscoRojo());

		TextField dniField = new TextField();
		dniField.setId("DNI");
		dniLabel.setLabelFor(dniField);
		inputsAValidar.add(dniField);
		dniField.setPromptText("99999999X");
		
		//Limito el numero de caracteres. En JavaFx se recomienda un TextFormatter
		dniField.setTextFormatter(new TextFormatter<String>(valor -> {
		    if (valor.getControlNewText().length() <= longitudDNI) {
		        return valor;
		    } else {
		        return null; //No aniade mas caracteres
		    }
		}));

		Label declaracionLabel = new Label("Declaración de consentimiento:");
		TextArea declaracionArea = new TextArea();
		declaracionArea.setEditable(false);
		declaracionArea.setText(
				"El cliente acepta el contenido de este campo");
		// Quitamos scroll horizontal
		declaracionArea.setWrapText(true);

		Label firmaLabel = new Label("Firma:");

		TextFlow contenedorFirmaLabel = new TextFlow(firmaLabel, crearAsteriscoRojo());

		// Para poder dibujar necesito un elemento de tipo Canva
		Canvas canvas = new Canvas(400, 150);
		// Ponemos el fondo blanco ya que un Canva por defecto no tiene color
		canvas.setStyle("-fx-background-color: white; -fx-border-color: darkgray;");

		// Creamos componente grafico para poder dibujar sobre el Canva
		GraphicsContext gc = canvas.getGraphicsContext2D();

		// Cuando hago click actualizo las coordenadas de inicio de pintado
		canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
			lastX = e.getX();
			lastY = e.getY();
		});

		// Cuando muevo el ratón pinto una linea entre la ultima coordenada al macenada
		// (click de raton) y las coordenadas donde me estoy desplazando
		canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
			double x = e.getX();
			double y = e.getY();
			gc.strokeLine(lastX, lastY, x, y);
			lastX = x;
			lastY = y;

			// Indico que he dibujado algo en el canva
			// Se tiene que hacer cuando arrastro el raton y no con los clics
			firmaEmpezada = true;
		});

		// Creo un VBox para contener el Canva y delimitar de manera visible el area con
		// un borde gris y espaciado
		VBox firmaBox = new VBox(5, canvas);
		firmaBox.setStyle("-fx-padding: 10; -fx-border-color: darkgray; -fx-border-radius: 5;");

		// Boton para generar el PDF con la firma
		Button generarPDFButton = new Button("Firmar");
		generarPDFButton.setMinWidth(150);

		// Necesito un contenedor para poder alinear al centro
		VBox botonera = new VBox(10); // 10 px de separacion
		botonera.setAlignment(Pos.CENTER); // centra el contenido
		botonera.getChildren().add(generarPDFButton);
		// Aniado el StyleClass definido en el fichero estilos.css
		generarPDFButton.getStyleClass().add("boton-verde");
		generarPDFButton.setOnAction(e -> {
			try {
				// Validamos los campos de entrada
				compruebaCamposRequeridos(inputsAValidar);

				generarPDF(nombreField.getText(), dniField.getText(), declaracionArea.getText(), canvas,
						pantallaPrincipal);
				// Mensaje de aviso que confirma la generacion
				Alert alert = new Alert(Alert.AlertType.INFORMATION, "PDF generado correctamente.");
				alert.show();
			} catch (CamposRequeridosException ex) {
				// Mensaje si faltan campos requeridos
				Alert alert = new Alert(Alert.AlertType.ERROR,
						"Revise que ha rellenado todos los campos obligatorios: " + ex.getMessage());
				alert.show();
			} catch (Exception ex) {
				// Mensaje por si falla la generacion de PDF
				Alert alert = new Alert(Alert.AlertType.ERROR, "Error al generar PDF: " + ex.getMessage());
				alert.show();
			}
		});

		VBox root = new VBox(10, contenedorNombreLabel, nombreField, contenedorDniLabel, dniField, declaracionLabel,
				declaracionArea, contenedorFirmaLabel, firmaBox, botonera);
		root.setPadding(new Insets(10));
		// Creo la pantalla
		Scene escena = new Scene(root, 500, 600);
		// Cargo el fichero CSS para que aplique los estilos
		escena.getStylesheets().add(getClass().getResource("/css/estilos.css").toExternalForm());		
		pantallaPrincipal.setScene(escena);
		//Una vez asignada la escena quito el foco para que salga la marca de agua del primer campo, sino el foco estaria en ese campo
		pantallaPrincipal.setOnShown(e -> root.requestFocus());
		
		pantallaPrincipal.show();
	}

	/**
	 * Dada una lista de inputs comprueba si tienen contenido
	 * 
	 * @param inputsAValidar
	 * @throws CamposRequeridosException
	 */
	private void compruebaCamposRequeridos(List<TextInputControl> inputsAValidar) throws CamposRequeridosException {
		StringBuffer mensaje = new StringBuffer();
		// Recorremos los campos a validar
		for (TextInputControl input : inputsAValidar) {
			// Si es nulo o vacio anado mensaje
			if (input.getText() == null || input.getText().isEmpty()) {
				mensaje.append("\nCampo " + input.getId() + " esta vacio");
			}
		}

		// Si tengo texto es porque he aniadido campos que son obligatorios y no tienen
		// contenido
		if (mensaje.length() > 0) {
			throw new CamposRequeridosException(mensaje);
		}
	}

	private void compruebaFirma() throws NoFirmaException {
		if (!firmaEmpezada) {
			throw new NoFirmaException("Falta la firma");
		}
	}

	private void validaDni(String dni) throws DniMalFormadoException {
		String letras = "TRWAGMYFPDXBNJZSQVHLCKE";

		// Quitamos espacios en blanco delante y detras
		dni = dni.trim().toUpperCase();

		// Validar formato espanol: 8 dígitos + letra
		if (!dni.matches("\\d{8}[A-Z]")) {
			throw new DniMalFormadoException("El DNI no sigue el patron 8 digitos + letra");
		}

		// Obtenemos los numeros
		String numeroStr = dni.substring(0, 8);
		// Obtenemos la letra introducida
		char letra = dni.charAt(8);

		// Pasamos a entero
		int numero = Integer.parseInt(numeroStr);
		// Calculamos la letra que le corresponderia segun el string de letras
		char letraCorrecta = letras.charAt(numero % 23);

		if (letra != letraCorrecta) {
			throw new DniMalFormadoException("DNI: " + dni + " invalido, la letra no coincide");
		}

	}

	/**
	 * Metodo para crear un campo tipo Text con un asterisco rojo Hay que crear uno
	 * nuevo por cada asterisco ya que JavaFx no permite reutilizar el mismo objeto
	 * en componentes diferentes
	 * 
	 * @return
	 */
	private Text crearAsteriscoRojo() {
		Text asterisk = new Text("*");
		asterisk.setFill(Color.RED);
		return asterisk;
	}

	/**
	 * Método para generar un PDF a partir de la informacion del Abre un popUp para
	 * seleccionar la ruta donde se guardara el fichero
	 * 
	 * @param nombre
	 * @param dni
	 * @param firmaCanvas
	 * @throws IOException
	 */
	private void generarPDF(String nombre, String dni, String declaracion, Canvas firmaCanvas, Stage stage)
			throws Exception {
		// Comprobamos que todos los campos obligatorios están rellenos
		compruebaCamposRequeridos(inputsAValidar);
		// Validamos el formato del DNI
		validaDni(dni);

		// Comprobamos que el campo del Canva con la firma tenga algo pintado
		compruebaFirma();

		// Preguntar al usuario dónde guardar el PDF
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Guardar PDF");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo PDF", "*.pdf"));
		fileChooser.setInitialFileName("FormularioFirmado.pdf");

		File selectedFile = fileChooser.showSaveDialog(stage);
		if (selectedFile == null) {
			return; // el usuario canceló
		}

		// Guardar la firma como imagen temporal
		WritableImage snapshot = new WritableImage((int) firmaCanvas.getWidth(), (int) firmaCanvas.getHeight());
		firmaCanvas.snapshot(null, snapshot);
		File firmaFile = new File("firma.png");
		ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", firmaFile);

		PDDocument document = new PDDocument();
		PDPage page = new PDPage(PDRectangle.A4);
		document.addPage(page);

		PDPageContentStream contentStream = new PDPageContentStream(document, page);

		contentStream.beginText();
		contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD, 14);
		contentStream.newLineAtOffset(50, 750);
		contentStream.showText("Formulario de Datos con Firma");
		contentStream.endText();

		contentStream.beginText();
		contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 12);
		contentStream.newLineAtOffset(50, 700);
		contentStream.showText("Nombre: " + nombre);
		contentStream.endText();

		contentStream.beginText();
		contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 12);
		contentStream.newLineAtOffset(50, 670);
		contentStream.showText("DNI: " + dni);
		contentStream.endText();

		contentStream.beginText();
		contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 12);
		contentStream.newLineAtOffset(50, 640);
		contentStream.showText(declaracion);
		contentStream.endText();

		PDImageXObject pdImage = PDImageXObject.createFromFile("firma.png", document);
		contentStream.drawImage(pdImage, 50, 450, 200, 75);

		contentStream.close();
		document.save(selectedFile.getAbsolutePath());
		document.close();

		// Borrar la imagen temporal
		firmaFile.delete();
	}

	public static void main(String[] args) {
		launch();
	}
}
