/*
 * Copyright (c) 2020.
 * @author Sourabh P. Bhat ( https://spbhat.in/ )
 */

package in.spbhat;

import in.spbhat.icons.Icon;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;

import java.time.Duration;

public class EditableTask extends HBox {
    private final Pane parent;
    public final TextField taskField;
    public final CheckBox taskCompleted;
    public String notes;
    public int priority = PrioritiesSection.defaultPriority;

    public SimpleObjectProperty<Duration> expectedDuration;
    public SimpleObjectProperty<Duration> actualDuration;
    private final Tooltip tooltipClockFace;
    private final Button notesBtn;


    public enum EditableTaskStatus {
        INCOMPLETE, IN_PROCESS, COMPLETE
    }

    public EditableTask(Pane parent, String taskDescription, EditableTaskStatus status, int expectedDurationMinutes, int actualDurationMinutes, String notes) {
        this.parent = parent;
        taskCompleted = new CheckBox();
        taskCompleted.setSelected(status == EditableTaskStatus.COMPLETE);
        taskCompleted.setAllowIndeterminate(true);
        taskCompleted.setIndeterminate(status == EditableTaskStatus.IN_PROCESS);
        taskCompleted.setOnAction(event -> taskCompleted());
        Tooltip statusTooltip = new Tooltip("In process (Green) / Complete (Gray)");
        taskCompleted.setTooltip(statusTooltip);

        taskField = new TextField(taskDescription);
        styleTextField();
        taskField.setFont(Section.writeAreaFont);
        taskField.setPromptText("Task description");
        taskField.setPrefColumnCount(22);
        taskField.setPadding(Insets.EMPTY);
        Tooltip taskTooltip = new Tooltip();
        taskTooltip.setOnShowing(event -> taskTooltip.setText(taskField.getText()));
        taskField.setTooltip(taskTooltip);

        expectedDuration = new SimpleObjectProperty<>(Duration.ofMinutes(expectedDurationMinutes));
        actualDuration = new SimpleObjectProperty<>(Duration.ofMinutes(actualDurationMinutes));

        this.notes = notes;

        HBox.setHgrow(taskField, Priority.ALWAYS);

        ClockFace clockFace = new ClockFace(20);
        Button timerBtn = new Button();
        timerBtn.setGraphic(clockFace);
        timerBtn.setOnAction(e -> editDurationAndPriority());
        tooltipClockFace = new Tooltip();
        tooltipClockFace.setShowDuration(javafx.util.Duration.seconds(20));
        tooltipClockFace.setShowDelay(javafx.util.Duration.seconds(0.5));
        expectedDuration.addListener((observable, oldValue, newValue) -> updateTooltipClockFace());
        actualDuration.addListener((observable, oldValue, newValue) -> updateTooltipClockFace());
        timerBtn.setTooltip(tooltipClockFace);
        updateTooltipClockFace();

        Button removeTaskBtn = new Button("Remove", Icon.graphic("remove.png", 20));
        removeTaskBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        removeTaskBtn.setOnAction(event -> removeTask());
        removeTaskBtn.setTooltip(new Tooltip("Remove this Task..."));

        notesBtn = new Button("Notes", Icon.graphic("notes.png", 20));
        notesBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        notesBtn.setOnAction(event -> showNotes());
        updateNotesBtn();
        notesBtn.setTooltip(new Tooltip("Show additional notes..."));

        getChildren().addAll(taskCompleted, taskField, timerBtn, notesBtn, removeTaskBtn);
        setSpacing(2);
        setAlignment(Pos.CENTER);

        expectedDuration.addListener((observable, oldValue, newValue) -> clockFace.update());
        actualDuration.addListener((observable, oldValue, newValue) -> clockFace.update());
    }

    private void updateNotesBtn() {
        if (notes.isBlank()) {
            notesBtn.setEffect(new Glow(0.9));
        } else {
            notesBtn.setEffect(null);
        }
    }

    private void updateTooltipClockFace() {
        Platform.runLater(() -> tooltipClockFace.setText("Duration Expected: %s, Actual: %s"
                .formatted(format(expectedDuration.get()), format(actualDuration.get()))));
    }

    private void showNotes() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        DialogPane notesDialog = new DialogPane();
        TextArea notesText = new TextArea(notes);
        notesText.setPrefColumnCount(50);
        notesText.setPrefRowCount(10);

        VBox textAreaBox = new VBox(notesText);
        VBox.setVgrow(notesText, Priority.ALWAYS);

        notesDialog.setContent(textAreaBox);
        notesDialog.setMaxWidth(1000);
        notesDialog.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        notesDialog.setHeaderText("Notes for Task: '%s'".formatted(taskField.getText()));
        notesDialog.setGraphic(Icon.graphic("notes.png", 64));

        alert.setDialogPane(notesDialog);
        alert.setResizable(true);
        alert.setTitle("Notes for '%s'".formatted(taskField.getText()));
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                notes = notesText.getText();
                updateNotesBtn();
            }
        });
    }

    private static String format(Duration duration) {
        int hr = duration.toHoursPart();
        int min = duration.toMinutesPart();
        int sec = duration.toSecondsPart();
        if (hr == 0 && min == 0) {
            return "%02d sec".formatted(sec);
        }
        if (hr == 0 && sec == 0) {
            return "%02d min".formatted(min);
        }
        if (min == 0 && sec == 0) {
            return "%02d hr".formatted(hr);
        }
        return (hr != 0 ? "%02d:".formatted(hr) : "") + "%02d:%02d".formatted(min, sec);
    }

    private void taskCompleted() {
        styleTextField();
    }

    @Override
    public void requestFocus() {
        taskField.requestFocus();
    }

    private void styleTextField() {
        if (taskCompleted.isSelected()) {
            System.out.printf("Task '%s' completed%n", taskField.getText());
            taskField.setEditable(false);
            taskField.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, null, null)));
            taskField.setStyle("-fx-text-inner-color: white;");
        } else if (taskCompleted.isIndeterminate()) {
            System.out.printf("Task '%s' is in progress%n", taskField.getText());
            taskField.setEditable(true);
            taskField.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, null, null)));
            taskField.setStyle("-fx-text-inner-color: black;");
        } else {
            System.out.printf("Task '%s' is incomplete%n", taskField.getText());
            taskField.setEditable(true);
            taskField.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
            taskField.setStyle("-fx-text-inner-color: black;");
        }
    }

    private void removeTask() {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setContentText("Are you sure you want to delete the task?\n" + "\"" +
                taskField.getText() + "\"");
        dialog.setHeaderText("Confirm Delete?");
        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType.equals(ButtonType.OK)) {
                PrioritiesSection.writeToLogFile(this);
                parent.getChildren().remove(this);
            }
        });
    }

    private void editDurationAndPriority() {
        TextField expectedDurationField = new TextField();
        expectedDurationField.setPrefColumnCount(4);
        expectedDurationField.setText(String.valueOf(expectedDuration.get().toMinutes()));

        TextField actualDurationField = new TextField();
        actualDurationField.setPrefColumnCount(4);
        actualDurationField.setText(String.valueOf(actualDuration.get().toMinutes()));

        Spinner<Integer> prioritySpinner = new Spinner<>(1, 10, priority);
        prioritySpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
        prioritySpinner.getEditor().setPrefColumnCount(6);
        prioritySpinner.getEditor().setAlignment(Pos.CENTER);

        HBox expectedDurationBox = new HBox(new Label("Expected:  "), expectedDurationField, new Label(" Minutes"));
        expectedDurationBox.setSpacing(5);
        HBox actualDurationBox = new HBox(new Label("Actual:       "), actualDurationField, new Label(" Minutes"));
        actualDurationBox.setSpacing(5);
        HBox priorityBox = new HBox(new Label("Priority:       "), prioritySpinner);

        VBox durationsBox = new VBox(expectedDurationBox, actualDurationBox, priorityBox);
        durationsBox.setSpacing(10);
        durationsBox.setPadding(new Insets(20));

        DialogPane dialogPane = new DialogPane();
        Node graphic = Icon.graphic("sliders.png", 64);
        dialogPane.setPrefWidth(400);
        dialogPane.setGraphic(graphic);
        dialogPane.setHeaderText("Task: " + taskField.getText());
        dialogPane.setContent(durationsBox);
        dialogPane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Edit Durations");
        alert.setDialogPane(dialogPane);
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    int expectedMinutes = Integer.parseInt(expectedDurationField.getText());
                    int actualMinutes = Integer.parseInt(actualDurationField.getText());
                    if (expectedMinutes > 0 && actualMinutes >= 0) {
                        expectedDuration.set(Duration.ofMinutes(expectedMinutes));
                        actualDuration.set(Duration.ofMinutes(actualMinutes));
                    }
                    int newPriority = prioritySpinner.getValue();
                    if (newPriority != this.priority) {
                        this.priority = newPriority;
                        PrioritiesSection.sortByPriority();
                    }
                } catch (Exception ignore) {
                    //ignore in case of any error in parsing text to integer
                }
            }
        });
    }

    class ClockFace extends Pane {
        Circle expectedDurationGraphic;
        Arc actualDurationGraphic;

        ClockFace(double size) {
            double radius = size / 2;
            expectedDurationGraphic = new Circle(radius, radius, radius);
            expectedDurationGraphic.setFill(Color.GREENYELLOW);
            expectedDurationGraphic.setStroke(Color.BLACK);
            actualDurationGraphic = new Arc(radius, radius, radius, radius, 90, 0);
            actualDurationGraphic.setFill(Color.INDIANRED);
            actualDurationGraphic.setType(ArcType.ROUND);
            actualDurationGraphic.setStroke(Color.BLACK);
            getChildren().addAll(expectedDurationGraphic, actualDurationGraphic);
            setPrefSize(size, size);
            update();
        }

        void update() {
            double durationRatio = Math.min(actualDuration.get().toSeconds() / (double) expectedDuration.get().toSeconds(), 1);
            Platform.runLater(() ->
                    actualDurationGraphic.lengthProperty().set(-360 * durationRatio));
        }
    }
}
