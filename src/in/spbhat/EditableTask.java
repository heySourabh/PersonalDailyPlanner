/*
 * Copyright (c) 2020.
 * @author Sourabh P. Bhat ( https://spbhat.in/ )
 */

package in.spbhat;

import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

    public SimpleObjectProperty<Duration> expectedDuration;
    public SimpleObjectProperty<Duration> actualDuration;

    public enum EditableTaskStatus {
        INCOMPLETE, IN_PROCESS, COMPLETE
    }

    public EditableTask(Pane parent, String taskDescription, EditableTaskStatus status, int expectedDurationMinutes, int actualDurationMinutes) {
        this.parent = parent;
        taskCompleted = new CheckBox();
        taskCompleted.setSelected(status == EditableTaskStatus.COMPLETE);
        taskCompleted.setAllowIndeterminate(true);
        taskCompleted.setIndeterminate(status == EditableTaskStatus.IN_PROCESS);
        taskCompleted.setOnAction(event -> taskCompleted());

        taskField = new TextField(taskDescription);
        styleTextField();
        taskField.setFont(Section.writeAreaFont);
        taskField.setPromptText("Task description");
        taskField.setPrefColumnCount(22);
        taskField.setPadding(Insets.EMPTY);

        expectedDuration = new SimpleObjectProperty<>(Duration.ofMinutes(expectedDurationMinutes));
        actualDuration = new SimpleObjectProperty<>(Duration.ofMinutes(actualDurationMinutes));

        HBox.setHgrow(taskField, Priority.ALWAYS);

        Button removeTaskBtn = new Button("Remove", new ImageView(new Image(PrioritiesSection.class
                .getResource("icons/remove.png").toString(),
                20, -1, true, true)));
        removeTaskBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        removeTaskBtn.setOnAction(event -> removeTask());
        removeTaskBtn.setTooltip(new Tooltip("Remove this Task"));

        ClockPane clockPane = new ClockPane(20);
        Button timerBtn = new Button();
        timerBtn.setGraphic(clockPane);
        timerBtn.setOnAction(e -> editDurations());

        getChildren().addAll(taskCompleted, taskField, timerBtn, removeTaskBtn);
        setSpacing(2);
        setAlignment(Pos.CENTER);

        actualDuration.addListener((observable, oldValue, newValue) -> clockPane.update());
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
            System.out.println("Task completed");
            taskField.setEditable(false);
            taskField.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, null, null)));
            taskField.setStyle("-fx-text-inner-color: white;");
        } else if (taskCompleted.isIndeterminate()) {
            System.out.println("Task is in progress");
            taskField.setEditable(true);
            taskField.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, null, null)));
            taskField.setStyle("-fx-text-inner-color: black;");
        } else {
            System.out.println("Task is incomplete");
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
                parent.getChildren().remove(this);
            }
        });
    }

    private void editDurations() {
        TextField expectedDurationField = new TextField();
        expectedDurationField.setPrefColumnCount(4);
        expectedDurationField.setText(String.valueOf(expectedDuration.get().toMinutes()));

        TextField actualDurationField = new TextField();
        actualDurationField.setPrefColumnCount(4);
        actualDurationField.setText(String.valueOf(actualDuration.get().toMinutes()));

        HBox expectedDurationBox = new HBox(new Label("Expected:  "), expectedDurationField, new Label(" Minutes"));
        expectedDurationBox.setSpacing(5);
        HBox actualDurationBox = new HBox(new Label("Actual:       "), actualDurationField, new Label(" Minutes"));
        actualDurationBox.setSpacing(5);
        VBox durationsBox = new VBox(expectedDurationBox, actualDurationBox);
        durationsBox.setSpacing(10);
        durationsBox.setPadding(new Insets(20));

        DialogPane dialogPane = new DialogPane();
        Node graphic = new ImageView(new Image(PrioritiesSection.class
                .getResource("icons/sliders.png").toString()));
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
                    if (expectedMinutes > 0 && actualMinutes > 0) {
                        expectedDuration.set(Duration.ofMinutes(expectedMinutes));
                        actualDuration.set(Duration.ofMinutes(actualMinutes));
                    }
                } catch (Exception ignore) {
                    //ignore in case of any error in parsing text to integer
                }
            }
        });
    }

    class ClockPane extends Pane {
        Circle expectedDurationGraphic;
        Arc actualDurationGraphic;

        ClockPane(double size) {
            double radius = size / 2;
            expectedDurationGraphic = new Circle(radius, radius, radius);
            expectedDurationGraphic.setFill(Color.GREENYELLOW);
            expectedDurationGraphic.setStroke(Color.BLACK);
            expectedDurationGraphic.getStrokeDashArray().addAll(4.0, 3.0);
            actualDurationGraphic = new Arc(radius, radius, radius, radius, 90, 0);
            actualDurationGraphic.setFill(Color.INDIANRED);
            actualDurationGraphic.setType(ArcType.ROUND);
            actualDurationGraphic.setStroke(Color.BLACK);
            getChildren().addAll(expectedDurationGraphic, actualDurationGraphic);
            update();
        }

        void update() {
            double durationRatio = Math.min(actualDuration.get().toSeconds() / (double) expectedDuration.get().toSeconds(), 1);
            actualDurationGraphic.lengthProperty().set(-360 * durationRatio);
        }
    }
}
