/*
 * Copyright (c) 2020.
 * @author Sourabh P. Bhat ( https://spbhat.in/ )
 */

package in.spbhat;

import in.spbhat.EditableTask.EditableTaskStatus;
import in.spbhat.icons.Icon;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.locks.LockSupport;

public class PrioritiesSection extends Section {
    public static ObservableList<Node> prioritiesTaskList;
    private static TilePane taskListPane;

    static final int defaultExpectedDurationMinutes = 30;
    static final int defaultActualDurationMinutes = 0;

    File taskCompletionLogFile = new File("plans", Planner.todayDateString + ".log");

    public PrioritiesSection() {
        super("Priorities", createContent());
        startTaskDurationUpdateTimer();
        startCompletedTaskRemovalThread();
    }

    private static Pane createContent() {
        Label headingLabel = new Label(
                "Things I must complete to move my projects forward.");
        headingLabel.setFont(Section.labelFont);
        Label noteLabel = new Label(
                "List of priorities and to-dos that must be accomplished " +
                        "and DO these before getting trapped in other people's agendas.");
        noteLabel.setFont(Section.notesFont);
        noteLabel.setTextFill(Color.GRAY);

        taskListPane = new TilePane(Orientation.HORIZONTAL, 20, 2);
        prioritiesTaskList = taskListPane.getChildren();
        Button addBtn = new Button("Add", Icon.graphic("add.png", 20));
        addBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        addBtn.setTooltip(new Tooltip("Add New Task"));
        addBtn.setOnAction(event ->
                addEditableTask("", EditableTaskStatus.INCOMPLETE,
                        defaultExpectedDurationMinutes, defaultActualDurationMinutes,
                        "")
                        .requestFocus());
        prioritiesTaskList.add(addBtn);
        taskListPane.setTileAlignment(Pos.TOP_LEFT);

        ScrollPane scrollPane = new ScrollPane(taskListPane);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(400);

        HBox tasksArea = new HBox(scrollPane);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        tasksArea.setSpacing(20);

        VBox content = new VBox(headingLabel, noteLabel, tasksArea);
        content.setAlignment(Pos.CENTER);
        VBox.setVgrow(tasksArea, Priority.ALWAYS);

        return content;
    }


    private void sleepFor(Duration duration) {
        LockSupport.parkNanos(duration.toNanos());
    }

    private void startTaskDurationUpdateTimer() {
        Duration durationBetweenUpdates = Duration.ofSeconds(5);
        Thread thread = new Thread(() -> {
            while (true) {
                do {
                    sleepFor(durationBetweenUpdates);
                } while (!PomodoroSection.pomodoroRunning || PomodoroSection.currentPomodoroState != PomodoroSection.PomodoroState.WORKING);

                for (Node node : prioritiesTaskList) {
                    if (node instanceof EditableTask task && task.taskCompleted.isIndeterminate()) {
                        SimpleObjectProperty<Duration> actualDuration = task.actualDuration;
                        actualDuration.set(actualDuration.get().plus(durationBetweenUpdates));
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void startCompletedTaskRemovalThread() {
        final var tasksScheduledForRemoval = new ArrayList<EditableTask>();
        Thread thread = new Thread(() -> {
            while ((true)) {
                // Remove current tasks scheduled for removal and write task to log file
                for (EditableTask task : tasksScheduledForRemoval) {
                    if (task.taskCompleted.isSelected()) {
                        System.out.println("Removing task: " + task.taskField.getText());
                        Platform.runLater(() -> prioritiesTaskList.remove(task));

                        // write task to log file
                        Duration taskDuration = task.actualDuration.get();
                        String logMessage = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                                + " : Completed '" + task.taskField.getText() + "'"
                                + " in "
                                + "%02dh:%02dm:%02ds".formatted(
                                taskDuration.toHoursPart(),
                                taskDuration.toMinutesPart(),
                                taskDuration.toSecondsPart())
                                + "\n  Notes:\n" + task.notes.indent(4);
                        System.out.println("Logging:\n  " + logMessage);
                        try (var taskCompletionLogWriter = new FileWriter(taskCompletionLogFile, true);
                             var logWriter = new PrintWriter(taskCompletionLogWriter)) {
                            logWriter.println(logMessage);
                        } catch (Exception ignore) {
                        }
                    }
                }
                tasksScheduledForRemoval.clear();
                sleepFor(Duration.ofSeconds(1)); // Wait for the UI to remove the task

                // Schedule completed tasks for removal during next cycle
                for (Node node : prioritiesTaskList) {
                    if (node instanceof EditableTask task) {
                        if (task.taskCompleted.isSelected()) {
                            System.out.println("Scheduled for removal: " + task.taskField.getText());
                            tasksScheduledForRemoval.add(task);
                        }
                    }
                }

                sleepFor(Duration.ofMinutes(1));
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    public static EditableTask addEditableTask(String description, EditableTaskStatus status,
                                               int expectedDurationMinutes, int actualDurationMinutes,
                                               String notes) {
        EditableTask newTask = new EditableTask(taskListPane, description, status, expectedDurationMinutes, actualDurationMinutes, notes);
        prioritiesTaskList.add(taskListPane.getChildren().size() - 1, newTask);
        return newTask;
    }
}
