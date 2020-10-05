/*
 * Copyright (c) 2020.
 * @author Sourabh P. Bhat ( https://spbhat.in/ )
 */

package in.spbhat;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class EditableTask extends HBox {
    private final Pane parent;
    private final TextField taskField;
    private final CheckBox taskCompleted;

    public EditableTask(Pane parent, String taskDescription) {
        this.parent = parent;
        taskCompleted = new CheckBox();
        taskCompleted.setSelected(false);
        taskCompleted.setOnAction(event -> taskCompleted());

        taskField = new TextField(taskDescription);
        styleTextField();
        taskField.setFont(Section.writeAreaFont);
        taskField.setPromptText("Task description");
        taskField.setPrefColumnCount(22);
        taskField.setPadding(Insets.EMPTY);

        HBox.setHgrow(taskField, Priority.ALWAYS);

        Button removeTaskBtn = new Button("Remove", new ImageView(new Image(PrioritiesSection.class
                .getResource("icons/remove.png").toString(),
                20, -1, true, true)));
        removeTaskBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        removeTaskBtn.setOnAction(event -> removeTask());

        getChildren().addAll(taskCompleted, taskField, removeTaskBtn);
        setSpacing(2);
        setAlignment(Pos.CENTER);
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
}
