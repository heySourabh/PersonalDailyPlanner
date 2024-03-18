/*
 * Copyright (c) 2024.
 * @author Sourabh P. Bhat ( https://spbhat.in/ )
 * Created on 16 Mar, 2024
 */

package in.spbhat;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

public class InfoWidget extends Stage {
    private final Stage primaryStage;
    private List<String> inProcessTasks = new ArrayList<>();
    VBox inProcessTasksView = new VBox();

    public InfoWidget(Stage primaryStage) {
        this.primaryStage = primaryStage;

        Parent root = createContent();
        Scene scene = new Scene(root, Color.TRANSPARENT);
        scene.setCursor(Cursor.OPEN_HAND);
        scene.setOnMousePressed(this::mousePressed);
        scene.setOnMouseDragged(this::mouseDragged);

        super.setScene(scene);
        super.setResizable(false);
        super.setTitle("Pomodoro Info Widget");
        super.setAlwaysOnTop(true);
        super.initStyle(StageStyle.TRANSPARENT);

        updateInProcessTasksThread();
    }

    private Parent createContent() {
        Label timer = new Label();
        timer.setFont(Font.font(null, 20));
        timer.textProperty().bind(PomodoroSection.titleTextLabel.textProperty());
        VBox root = new VBox(timer, inProcessTasksView);
        root.setPadding(new Insets(5));
        Color bgColor = Color.color(0.9, 1.0, 0.9, 0.75);
        root.setBackground(new Background(new BackgroundFill(
                bgColor,
                new CornerRadii(10),
                Insets.EMPTY)));
        root.setBorder(new Border(new BorderStroke(
                bgColor.darker().darker(),
                BorderStrokeStyle.SOLID,
                new CornerRadii(10),
                BorderStroke.MEDIUM)));
        root.setEffect(new Glow());
        return root;
    }

    private double mouseX, mouseY;
    private double stageX, stageY;

    private void mousePressed(MouseEvent event) {
        mouseX = event.getScreenX();
        mouseY = event.getScreenY();
        stageX = this.getX();
        stageY = this.getY();
    }

    private void mouseDragged(MouseEvent event) {
        double dx = event.getScreenX() - mouseX;
        double dy = event.getScreenY() - mouseY;
        this.setX(stageX + dx);
        this.setY(stageY + dy);
    }

    private void updateInProcessTasksThread() {
        Thread.startVirtualThread(() -> {
            while (this.isShowing() && primaryStage.isShowing()) {
                updateTaskView();
                Platform.runLater(this::sizeToScene);
                LockSupport.parkNanos(Duration.ofSeconds(5).toNanos());
            }
            Platform.runLater(this::close);
        });
    }

    private boolean updateInProcessTasks() {
        List<String> currentInProcessTasks = PrioritiesSection.prioritiesTaskList.stream()
                .filter(node -> node instanceof EditableTask t && t.taskCompleted.isIndeterminate())
                .map(task -> ((EditableTask) task).taskField.getText())
                .toList();
        if (inProcessTasks.equals(currentInProcessTasks)) {
            return false;
        } else {
            inProcessTasks = currentInProcessTasks;
            return true;
        }
    }

    private void updateTaskView() {
        if (!updateInProcessTasks()) return;
        Platform.runLater(() -> {
            inProcessTasksView.getChildren().clear();
            inProcessTasksView.getChildren().addAll(
                    inProcessTasks.stream()
                            .map(task -> new Label(" ➤ " + task))
                            .toList());
            sizeToScene();
        });
    }
}
