package viewmodel;

import dao.DbConnectivityClass;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Person;
import service.MyLogger;

import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;



public class DB_GUI_Controller implements Initializable {

    @FXML
    TextField first_name, last_name, department, major, email, imageURL;
    @FXML
    ImageView img_view;
    @FXML
    MenuBar menuBar;
    @FXML
    private TableView<Person> tv;
    @FXML
    Button addBtn, editBtn, delBtn, clearBtn;
    @FXML
    private TableColumn<Person, Integer> tv_id;
    @FXML
    private MenuButton MajorMenu;
    @FXML
    private MenuItem math,computerscience,cpis,english,science;
    @FXML
    private TableColumn<Person, String> tv_fn, tv_ln, tv_department, tv_major, tv_email;
    private final DbConnectivityClass cnUtil = new DbConnectivityClass();
    private final ObservableList<Person> data = cnUtil.getData();
    private String majorField = "";
    @FXML
    private Label errorLBL,successLBL;
    @FXML
    private MenuItem importing,exporting;

    public void setMajorField(ActionEvent event) {
        MenuItem m = (MenuItem) event.getSource();
        this.majorField =  m.getText();;
        MajorMenu.setText(this.majorField);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tv_department.setCellValueFactory(new PropertyValueFactory<>("department"));
            tv_major.setCellValueFactory(new PropertyValueFactory<>("major"));
            tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));
            tv.setItems(data);
            successLBL.setVisible(true);
            successLBL.setText("Successfully loaded Database");
        } catch (Exception e) {
            errorLBL.setVisible(true);
            errorLBL.setText("Error: Could not load database");
            throw new RuntimeException(e);
        }
    }

    @FXML
    protected void addNewRecord() {
        errorLBL.setVisible(false);
        successLBL.setVisible(false);

            Person p = new Person(first_name.getText(), last_name.getText(), department.getText(),
                    MajorMenu.getText(), email.getText(), imageURL.getText());
            try {
                cnUtil.insertUser(p);
                cnUtil.retrieveId(p);
                p.setId(cnUtil.retrieveId(p));
                data.add(p);
                successLBL.setVisible(true);
                successLBL.setText("Successfully added: " + first_name.getText() + " to the DB");
            } catch (Exception e ) {
                errorLBL.setVisible(true);
                errorLBL.setText("Unable to add user, Try again");
            }
            clearForm();

    }

    @FXML
    protected void clearForm() {
        first_name.setText("");
        last_name.setText("");
        department.setText("");
        MajorMenu.setText("Major");
        majorField = "";
        email.setText("");
        imageURL.setText("");
    }

    @FXML
    protected void logOut(ActionEvent actionEvent) {
        System.out.println("Logout");
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").getFile());
            Stage window = (Stage) menuBar.getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void closeApplication() {
        System.exit(0);
    }

    @FXML
    protected void displayAbout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/about.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root, 600, 500);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void editRecord() {
        errorLBL.setVisible(false);
        successLBL.setVisible(false);
        System.out.println("Edit");
        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        Person p2 = new Person(index + 1, first_name.getText(), last_name.getText(), department.getText(),
                MajorMenu.getText(), email.getText(),  imageURL.getText());
        try {
            cnUtil.editUser(p.getId(), p2);
            data.remove(p);
            data.add(index, p2);
            tv.getSelectionModel().select(index);
            successLBL.setVisible(true);
            successLBL.setText("Successfuly edited user");
        } catch (Exception e) {
            errorLBL.setVisible(false);
            errorLBL.setText("Error: Could not edit user");

        }
    }

    @FXML
    protected void deleteRecord() {
        System.out.println("Delete");
        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        cnUtil.deleteRecord(p);
        data.remove(index);
        tv.getSelectionModel().select(index);
    }

    @FXML
    protected void showImage() {
        System.out.println("showImage");
        File file = (new FileChooser()).showOpenDialog(img_view.getScene().getWindow());
        if (file != null) {
            img_view.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    protected void addRecord() {
        System.out.println("AddRecord");

        showSomeone();
    }

    @FXML
    protected void selectedItemTV(MouseEvent mouseEvent) {
        System.out.println("selectedItemTV");
        Person p = tv.getSelectionModel().getSelectedItem();
        errorLBL.setVisible(false);
        successLBL.setVisible(false);
        if(p != null) {
            clearBtn.setDisable(false);
            editBtn.setDisable(false);
            delBtn.setDisable(false);
            first_name.setText(p.getFirstName());
            last_name.setText(p.getLastName());
            department.setText(p.getDepartment());
            MajorMenu.setText(p.getMajor());
            email.setText(p.getEmail());
            imageURL.setText(p.getImageURL());
        } else {
            errorLBL.setVisible(true);
            errorLBL.setText("Error: No selection");
        }
    }

    public void lightTheme(ActionEvent actionEvent) {
        System.out.println("LightTheme");
        try {
            Scene scene = menuBar.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.getScene().getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
            System.out.println("light " + scene.getStylesheets());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void darkTheme(ActionEvent actionEvent) {
        System.out.println("DarkTheme");
        try {
            Stage stage = (Stage) menuBar.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/darkTheme.css").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showSomeone() {
        Dialog<Results> dialog = new Dialog<>();
        dialog.setTitle("New User");
        dialog.setHeaderText("Please specify…");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField textField1 = new TextField("Name");
        TextField textField2 = new TextField("Last Name");
        TextField textField3 = new TextField("Email ");
        ObservableList<Major> options =
                FXCollections.observableArrayList(Major.values());
        ComboBox<Major> comboBox = new ComboBox<>(options);
        comboBox.getSelectionModel().selectFirst();
        dialogPane.setContent(new VBox(8, textField1, textField2,textField3, comboBox));
        Platform.runLater(textField1::requestFocus);
        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                return new Results(textField1.getText(),
                        textField2.getText(), comboBox.getValue());
            }
            return null;
        });
        Optional<Results> optionalResult = dialog.showAndWait();
        optionalResult.ifPresent((Results results) -> {
            MyLogger.makeLog(
                    results.fname + " " + results.lname + " " + results.major);
        });
    }

    private static enum Major {Business, CSC, CPIS}

    private static class Results {

        String fname;
        String lname;
        Major major;

        public Results(String name, String date, Major venue) {
            this.fname = name;
            this.lname = date;
            this.major = venue;
        }
    }
    @FXML
    void checkValid(KeyEvent event) {
        String firstName = first_name.getText();
        String lastName = last_name.getText();
        String dept = department.getText();
        String majors = MajorMenu.getText();
        String emails = email.getText();
        if(regex("(\\w{3,25})",firstName) && regex("(\\w{3,25})",lastName) && regex("(\\w+)(@)(\\w+)(\\.)(\\w+)",emails) && !dept.equals("")&& !majors.equals("")) {
            addBtn.setDisable(false);
            editBtn.setDisable(false);
        } else {
            addBtn.setDisable(true);
            editBtn.setDisable(true);
        }
    }
    private boolean regex(String regExpression, String input) {

        final Pattern pattern = Pattern.compile(regExpression, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(input);
        boolean found = false;
        while (matcher.find()) {
            found = true;
        }
        return found;
    }
    @FXML
    void grayOut(MouseEvent event) {
        clearBtn.setDisable(true);
        delBtn.setDisable(true);
        editBtn.setDisable(true);
    }

    @FXML
    void clearSelection(MouseEvent event) {
        tv.getSelectionModel().clearSelection();
        grayOut(event);
    }
    @FXML
    private TextField csvTXT;
    @FXML
    void importCSV(ActionEvent event) {
        System.out.println("importCSV");
        errorLBL.setVisible(false);
        successLBL.setVisible(false);
        if(csvTXT.getText().isEmpty()) {
            errorLBL.setVisible(true);
            errorLBL.setText("Please enter a CSV file name in the CSV text field.");
        }
        try(CSVReader r = new CSVReader(new FileReader(csvTXT.getText()))) {
            String except = "";
            List<String[]> rows = r.readAll();
            for(int i = 0; i < rows.size(); i++) {
                String[] row = rows.get(i);
                Person p = new Person(row[0], row[1], row[2], row[3], row[4], row[5]);
                if (regex("(\\w{3,25})", row[0]) && regex("(\\w{3,25})", row[1]) && regex("(\\w+)(@)(\\w+)(\\.)(\\w+)", row[4]) && !row[2].equals("") && !row[3].equals("")) {
                    cnUtil.insertUser(p);
                    cnUtil.retrieveId(p);
                    p.setId(cnUtil.retrieveId(p));
                    data.add(p);
                    successLBL.setVisible(true);
                    successLBL.setText("Successfully added: " + first_name.getText() + " to the DB");
                } else {
                    except = ", But excluded a few users due to incorrect format";
                }
            }
            successLBL.setVisible(true);
            successLBL.setText("Successfully exported file to DB" + except);
        } catch (IOException | CsvException e) {
            errorLBL.setVisible(true);
            errorLBL.setText("Error: Something went wrong with your CSV file. Please check format, and try again");
            throw new RuntimeException(e);
        }
        clearForm();
    }
    @FXML
    void exportCSV(ActionEvent event) throws FileNotFoundException {
        System.out.println("exportCSV");
        errorLBL.setVisible(false);
        successLBL.setVisible(false);
        ArrayList<String[]> al = new ArrayList<String[]>();
        for(int i = 0; i < data.size(); i++) {
            Person p = (Person) data.get(i);
            String[] s = new String[6];
            s[0] = p.getFirstName();
            s[1] = p.getLastName();
            s[2] = p.getDepartment();
            s[3] = p.getMajor();
            s[4] = p.getEmail();
            s[5] = p.getImageURL();
            al.add(s);

        }
        File f = new File("CSVOut.csv");
        try(PrintWriter p = new PrintWriter(f)) {
            for(int i = 0; i < al.size(); i++) {
                p.println(al.get(i)[0] + "," + al.get(i)[1] + "," + al.get(i)[2] + "," + al.get(i)[3] + "," + al.get(i)[4] + "," + al.get(i)[5]);
            }
            successLBL.setVisible(true);
            successLBL.setText("Successfully exported file to CSV (CSVOut.csv)");
        } catch (FileNotFoundException e) {
            errorLBL.setVisible(true);
            errorLBL.setText("Error, Please try again");
            e.printStackTrace();
        }

    }
    @FXML
    Button pdfBtn;
    @FXML
    void genPdf(ActionEvent event) throws IOException {
        System.out.println("genPdf");
        errorLBL.setVisible(false);
        successLBL.setVisible(false);
        ArrayList<String> count = new ArrayList<>();
        ArrayList<String[]> al = new ArrayList<String[]>();
        for(int i = 0; i < data.size(); i++) {
            Person p = (Person) data.get(i);
            String[] s = new String[6];
            s[0] = p.getFirstName();
            s[1] = p.getLastName();
            s[2] = p.getDepartment();
            s[3] = p.getMajor();
            count.add(s[3]);
            s[4] = p.getEmail();
            s[5] = p.getImageURL();
            al.add(s);
            File f = new File("gen.txt");
        }
        count.sort(String.CASE_INSENSITIVE_ORDER);
        Map<String,Integer> m = new HashMap<>();
        for(String string : count) {
            m.put(string, m.getOrDefault(string, 0) + 1);
        }
        File f = new File("gen.txt");
        try(PrintWriter p = new PrintWriter(f)) {
            for(int i = 0; i < al.size(); i++) {
                p.println(al.get(i)[0] + "," + al.get(i)[1] + "," + al.get(i)[2] + "," + al.get(i)[3] + "," + al.get(i)[4] + "," + al.get(i)[5]);
            }
            p.println("*******************************************************************************************************************");
            for(String str : m.keySet()) {
                p.println(str + " | " + m.get(str));
            }
        } catch (FileNotFoundException e) {
            errorLBL.setVisible(true);
            errorLBL.setText("Error, Please try again");
        }
        PdfDocument pdf = new PdfDocument(new PdfWriter("Report.pdf"));
        Document document = new Document(pdf);

        File file = new File("gen.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;

        while ((line = br.readLine()) != null) {
            document.add(new Paragraph(line));
        }
        document.close();
        file.delete();
        successLBL.setVisible(true);
        successLBL.setText("Successfully exported file to PDF");
    }

}