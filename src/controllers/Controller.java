package controllers;

import counters.*;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.fxmisc.richtext.InlineCssTextArea;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class Controller {
    @FXML CheckBox displayCounterCheckBox;
    @FXML InlineCssTextArea textArea;
    @FXML MenuItem newMenuItem;
    @FXML MenuItem openMenuItem;
    @FXML MenuBar menuBar;
    @FXML CheckMenuItem wordCountMenuItem;
    @FXML Label wordCountLabel;
    @FXML Label wordCountText;
    @FXML CheckMenuItem syllableCountMenuItem;
    @FXML Label syllableCountLabel;
    @FXML Label syllableText;
    @FXML CheckMenuItem sentenceCountMenuItem;
    @FXML Label sentenceCountLabel;
    @FXML Label sentenceText;
    @FXML CheckMenuItem fleschScoreMenuItem;
    @FXML Label fleschScoreLabel;
    @FXML Label fleschScoreText;
    @FXML ColorPicker colorPicker;
    @FXML Spinner<Integer> fontSizeSpinner;
    @FXML ComboBox fontChoiceBox;
    @FXML String path = "";
    static String paragraphText;
    private double x = 0, y = 0;

    int syllableCount = 0; int wordCount = 0; int sentenceCount = 0; double fleschScore = 0;
    PauseTransition waitForFinishedInput = new PauseTransition(Duration.seconds(0.5));

    private void exit(Stage primaryStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,"Are you sure you want to exit?",ButtonType.YES,
                ButtonType.NO);
        alert.setTitle("Are You Sure You Want To Exit?");
        alert.setHeaderText("You are about to exit your project. All unsaved progress may be lost.");
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            primaryStage.close();
        }
    }

    private void newButton () {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Creating a new document may cause unsaved changes to " +
                "be lost.",
                ButtonType.YES,
                ButtonType.NO);
        alert.setTitle("New?");
        alert.setHeaderText("Are you sure you want to create a new document?");
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            save(new ActionEvent());
            textArea.replaceText("");
            path = "";
        }
    }

    private void open(Stage primaryStage) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "...txt"));
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            path = selectedFile.getPath();
            String fileString = new String(Files.readAllBytes(selectedFile.toPath()));
            textArea.replaceText(fileString);
            updateStatusBarNumbers();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void save(Stage primaryStage) {
        if (!path.equals("")) {
            saveFile(new File(path));
        } else {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "...txt"));
            fileChooser.setTitle("Save");
            File file = fileChooser.showSaveDialog(primaryStage);
            path = file.getPath();
            saveFile(file);
        }
    }

    private void saveFile(File f) {
        try {
            String textContent = textArea.getText();
            BufferedWriter writer = new BufferedWriter(new FileWriter(f));
            writer.write(textContent);
            writer.close();
        } catch (IOException e) {
            System.out.println("Cannot Save");
        }
    }

    public void open(ActionEvent actionEvent) {
        Stage stage = (Stage) menuBar.getScene().getWindow();
        open(stage);
    }
    public void newButton(ActionEvent actionEvent) {
        newButton();
    }
    public void exit(ActionEvent actionEvent) {
        Stage stage = (Stage) menuBar.getScene().getWindow();
        exit(stage);
    }
    public void cut(ActionEvent actionEvent) {
        textArea.cut();
    }
    public void copy(ActionEvent actionEvent) {
        textArea.copy();
    }
    public void paste(ActionEvent actionEvent) {
        textArea.paste();
    }

    public void save(ActionEvent actionEvent) {
        Stage primaryStage = (Stage)menuBar.getScene().getWindow();
        save(primaryStage);
    }

    public void statusBarListener(KeyEvent keyEvent) {
        waitForFinishedInput.setOnFinished(e -> updateStatusBarNumbers());
        waitForFinishedInput.playFromStart();
    }

    private void updateStatusBarNumbers() {
        getWordCount();
        getSentenceCount();
        getSyllablesCount();
        getFleschScore();
    }

    private void getWordCount() {
        Word word = new Word();
        wordCount = word.getWordCount(textArea.getText());
        wordCountLabel.setText(Integer.toString(wordCount));
    }

    private void getSentenceCount() {
        Sentence sentence = new Sentence();
        sentenceCount = sentence.getSentenceCount(textArea.getText());
        sentenceCountLabel.setText(Integer.toString(sentenceCount));
    }

    private void getSyllablesCount() {
        Syllable syllable = new Syllable();
        syllableCount = syllable.getSyllables(textArea.getText());
        syllableCountLabel.setText(Integer.toString(syllableCount));
    }

    private void getFleschScore() {
        FleschScore fleschScoreDriver = new FleschScore();
        fleschScore = fleschScoreDriver.getFleschScore(sentenceCount, syllableCount, wordCount);
        fleschScoreLabel.setText(Double.toString(fleschScore));
    }

    public void changeColor(ActionEvent actionEvent) {
        IndexRange selection = textArea.getSelection();
        String color = colorPicker.getValue().toString();
        color = color.replaceAll("0x","");
        String currentStyle = textArea.getDocument().getStyleAtPosition(selection.getEnd());

        if (!currentStyle.contains("-fx-fill")) {
            textArea.setStyle(selection.getStart(), selection.getEnd(), currentStyle + "-fx-fill: #" + color + ";");
        } else {
            textArea.setStyle(textArea.getSelection().getStart(), textArea.getSelection().getEnd(),
                    changeColor(currentStyle, color));
        }
    }

    private String changeColor(String style, String color) {
        return style.replaceAll("(-fx-fill: #\\S+;)", "-fx-fill: #" + color + ";");
    }

    public void changeFontSize(MouseEvent mouseEvent) {
        IndexRange selection = textArea.getSelection();
        int fontSize = fontSizeSpinner.getValue();
        String currentStyle = textArea.getDocument().getStyleAtPosition(selection.getEnd());

        if (!currentStyle.contains("-fx-font-size")) {
            textArea.setStyle(selection.getStart(), selection.getEnd(), currentStyle + "-fx-font-size: " + fontSize + "px;");
        } else {
            textArea.setStyle(textArea.getSelection().getStart(), textArea.getSelection().getEnd(),
                    changeFontAction(currentStyle, fontSize));
        }
    }

    private String changeFontAction(String style,int fontSize) {
        return style.replaceAll("(-fx-font-size: \\d+px;)", "-fx-font-size: " + fontSize + "px;");
    }

    public void showWordCount(ActionEvent actionEvent) {
        if (wordCountMenuItem.isSelected() || displayCounterCheckBox.isSelected()) {
            wordCountLabel.setOpacity(1);
            wordCountText.setOpacity(1);
        }
        else {
            wordCountLabel.setOpacity(0);
            wordCountText.setOpacity(0);
        }
    }

    public void showSyllableCounter(ActionEvent actionEvent) {
        if (syllableCountMenuItem.isSelected() || displayCounterCheckBox.isSelected()) {
            syllableCountLabel.setOpacity(1);
            syllableText.setOpacity(1);
        }
        else {
            syllableCountLabel.setOpacity(0);
            syllableText.setOpacity(0);
        }
    }

    public void showSentenceCount(ActionEvent actionEvent) {
        if (sentenceCountMenuItem.isSelected() || displayCounterCheckBox.isSelected()) {
            sentenceCountLabel.setOpacity(1);
            sentenceText.setOpacity(1);
        }
        else {
            sentenceCountLabel.setOpacity(0);
            sentenceText.setOpacity(0);
        }
    }

    public void showFleschScore(ActionEvent actionEvent) {
        if (fleschScoreMenuItem.isSelected() || displayCounterCheckBox.isSelected()) {
            fleschScoreLabel.setOpacity(1);
            fleschScoreText.setOpacity(1);
        }
        else {
            fleschScoreText.setOpacity(0);
            fleschScoreLabel.setOpacity(0);
        }
    }

    public void generateText(ActionEvent actionEvent) {
        Parent root;
        try {
            root = FXMLLoader.load(getClass().getClassLoader().getResource("view/markov.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Markov Generator");
            stage.initStyle(StageStyle.UNDECORATED);
            root.setEffect(new DropShadow());
            Scene scene = new Scene(root);
            stage.setScene(scene);

            root.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    x = stage.getX() - mouseEvent.getScreenX();
                    y = stage.getY() - mouseEvent.getScreenY();
                }
            });
            root.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    stage.setX(mouseEvent.getScreenX() + x);
                    stage.setY(mouseEvent.getScreenY() + y);
                }
            });

            paragraphText = textArea.getText();
            stage.showAndWait();
            if (!paragraphText.isEmpty()) {
                textArea.replaceText(paragraphText);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeFontAction(ActionEvent actionEvent) {
        String font = fontChoiceBox.getValue().toString();
        IndexRange selection = textArea.getSelection();
        String currentStyle = textArea.getDocument().getStyleAtPosition(selection.getEnd());

        if (!currentStyle.contains("-fx-font-family")) {
            String style = currentStyle + "-fx-font-family: '" + font + "';";
            textArea.setStyle(selection.getStart(), selection.getEnd(),  style);
        } else {
            textArea.setStyle(textArea.getSelection().getStart(), textArea.getSelection().getEnd(),
                    changeFont(currentStyle, font));
        }
    }

    private String changeFont(String currentStyle, String font) {
        return currentStyle.replaceAll("(-fx-font-family: '.*';)", "-fx-font-family: '" + font + "';");
    }

    public void displayCounters(ActionEvent actionEvent) {
        if (displayCounterCheckBox.isSelected()) {
            fleschScoreMenuItem.setSelected(true);
            sentenceCountMenuItem.setSelected(true);
            syllableCountMenuItem.setSelected(true);
            wordCountMenuItem.setSelected(true);
        } else {
            fleschScoreMenuItem.setSelected(false);
            sentenceCountMenuItem.setSelected(false);
            syllableCountMenuItem.setSelected(false);
            wordCountMenuItem.setSelected(false);
        }
        showFleschScore(new ActionEvent());
        showSentenceCount(new ActionEvent());
        showSyllableCounter(new ActionEvent());
        showWordCount(new ActionEvent());
    }
}
