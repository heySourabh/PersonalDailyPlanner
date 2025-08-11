/*
 * Copyright (c) 2024.
 * @author Sourabh P. Bhat ( https://spbhat.in/ )
 * Created on 16 Mar, 2024
 */

package in.spbhat;

import in.spbhat.icons.Icon;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class InfoWidget extends Stage {
    private final Stage primaryStage;
    private List<String> inProcessTasks = new ArrayList<>();
    VBox inProcessTasksView = new VBox();

    public InfoWidget(Stage primaryStage) {
        this.primaryStage = primaryStage;

        Pane root = createContent();
        Scene scene = new Scene(root, Color.TRANSPARENT);
        scene.setCursor(Cursor.OPEN_HAND);
        scene.setOnMousePressed(this::mousePressed);
        scene.setOnMouseReleased(this::mouseReleased);
        scene.setOnMouseDragged(this::mouseDragged);
        scene.setOnMouseClicked(this::mouseClicked);

        super.setScene(scene);
        super.setResizable(false);
        super.setTitle("Pomodoro Info Widget");
        super.setAlwaysOnTop(true);
        super.initStyle(StageStyle.TRANSPARENT);
        super.getIcons().add(Icon.graphic("info_widget_icon_64.png", 64).getImage());

        // position to the top-right corner of the primary screen
        new Thread(() -> {
            // wait for some time for the widget to show, so that its dimensions are populated
            do {
                System.out.println("Waiting for the info to show.");
                Planner.sleepFor(Duration.ofMillis(500));
            } while (!isShowing());
            // get primary screen
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double widgetWidth = root.getWidth();
            int horizontalPadding = 50;
            int verticalPadding = 50;
            moveWidgetToLocation(
                    screenBounds.getMaxX() - widgetWidth - horizontalPadding,
                    screenBounds.getMinY() + verticalPadding
            );
        }).start();
        updateInProcessTasksThread();
    }

    private void moveWidgetToLocation(double x, double y) {
        Platform.runLater(() -> {
            super.setX(x);
            super.setY(y);
        });
    }

    private Pane createContent() {
        Label timer = new Label();
        timer.setFont(Font.font(null, 20));
        timer.textProperty().bind(PomodoroSection.titleTextLabel.textProperty());
        VBox root = new VBox(timer, inProcessTasksView);
        root.setPadding(new Insets(5));
        Color bgColor = Color.color(0.9, 1.0, 0.9, 0.3);
        root.setBackground(new Background(new BackgroundFill(
                bgColor,
                new CornerRadii(10),
                Insets.EMPTY)));
        root.setBorder(new Border(new BorderStroke(
                bgColor.darker().darker(),
                BorderStrokeStyle.SOLID,
                new CornerRadii(10),
                BorderStroke.MEDIUM)));
        root.setEffect(new DropShadow(5, Color.WHITE));
        return root;
    }

    private double mouseX, mouseY;
    private double stageX, stageY;
    private boolean mouseDragged;

    private void mousePressed(MouseEvent event) {
        ((Scene) event.getSource()).setCursor(Cursor.CLOSED_HAND);
        mouseX = event.getScreenX();
        mouseY = event.getScreenY();
        stageX = this.getX();
        stageY = this.getY();
        mouseDragged = false;
    }

    private void mouseDragged(MouseEvent event) {
        double dx = event.getScreenX() - mouseX;
        double dy = event.getScreenY() - mouseY;
        this.setX(stageX + dx);
        this.setY(stageY + dy);
        mouseDragged = true;
    }

    private void mouseReleased(MouseEvent event) {
        ((Scene) event.getSource()).setCursor(Cursor.OPEN_HAND);
    }

    private void mouseClicked(MouseEvent event) {
        if (!mouseDragged) {
            Planner.movePrimaryStageToFront();
        }
    }

    private void updateInProcessTasksThread() {
        Thread.startVirtualThread(() -> {
            while (this.isShowing() && primaryStage.isShowing()) {
                updateTaskView();
                Platform.runLater(this::sizeToScene);
                Planner.sleepFor(Duration.ofSeconds(5));
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
                            .map(task -> new Label(" ➤ " + limitTo50Chars(task)))
                            .toList());
            sizeToScene();
        });
    }

    private String limitTo50Chars(String str) {
        int numChars = 50;
        if (str.length() <= numChars) {
            return str;
        } else {
            String append = "...";
            return str.substring(0, numChars - append.length()) + append;
        }
    }
}
