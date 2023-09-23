/*
 * Copyright (c) 2021.
 * @author Sourabh P. Bhat ( https://spbhat.in/ )
 */

package in.spbhat;

import in.spbhat.EditableTask.EditableTaskStatus;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.locks.LockSupport;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.time.temporal.ChronoUnit.MINUTES;

public class Planner extends Application {
    static final DateTimeFormatter dateFormatter = ofPattern("yyyy-MM-dd");
    static final LocalDateTime now = now();
    static final String todayDateString = now.format(dateFormatter);
    static Section pomodoroSection, projectsSection, peopleSection, prioritiesSection;
    static VBox sections;

    public static void main(String[] args) {
        launch(args);
    }

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        Scene scene = new Scene(createContent(), 900, 1000);
        stage.setScene(scene);
        stage.getIcons().addAll(
                new Image(Objects.requireNonNull(Planner.class
                        .getResource("icons/icon_64.png")).toString()),
                new Image(Objects.requireNonNull(Planner.class
                        .getResource("icons/icon_128.png")).toString()));
        stage.setTitle("Productivity Planner (by Sourabh Bhat)");
        stage.setOnCloseRequest(event -> save(sections, event));
        moveToFrontIntermittently();
        stage.show();
    }

    private void moveToFrontIntermittently() {
        Thread.startVirtualThread(() -> {
            while (true) {
                sleepFor(Duration.of(30, MINUTES));
                movePrimaryStageToFront();
            }
        });
    }

    public static void movePrimaryStageToFront() {
        Platform.runLater(() -> {
            primaryStage.setIconified(false);
            primaryStage.toFront();
        });
    }

    private Parent createContent() {
        BorderPane root = new BorderPane();
        Menu fileMenu = new Menu("_File");
        MenuItem saveMenuItem = new MenuItem("_Save");
        saveMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));

        MenuItem showLogMenuItem = new MenuItem("Show _Log");
        showLogMenuItem.setOnAction(event -> showLog());

        fileMenu.getItems().addAll(saveMenuItem, showLogMenuItem);

        Menu helpMenu = new Menu("_Help");
        MenuItem aboutMenuItem = new MenuItem("_About");
        aboutMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F1));
        aboutMenuItem.setOnAction(event -> showAboutInfo());

        helpMenu.getItems().add(aboutMenuItem);

        MenuBar menuBar = new MenuBar(fileMenu, helpMenu);
        menuBar.setUseSystemMenuBar(true);

        PlannerTitlePane titlePane = new PlannerTitlePane();
        HBox topPane = new HBox(menuBar, titlePane);
        HBox.setHgrow(titlePane, Priority.ALWAYS);
        topPane.setAlignment(Pos.CENTER);

        root.setTop(topPane);

        pomodoroSection = new PomodoroSection();
        projectsSection = new ProjectSection();
        peopleSection = new PeopleSection();
        prioritiesSection = new PrioritiesSection();
        sections = new VBox(
                new Separator(), pomodoroSection,
                new Separator(), projectsSection,
                new Separator(), peopleSection,
                new Separator(), prioritiesSection);
        VBox.setVgrow(projectsSection, Priority.ALWAYS);
        VBox.setVgrow(peopleSection, Priority.ALWAYS);
        VBox.setVgrow(prioritiesSection, Priority.ALWAYS);

        sections.setAlignment(Pos.TOP_CENTER);
        sections.setPadding(new Insets(0, 10, 10, 10));
        root.setCenter(sections);

        saveMenuItem.setOnAction(event -> save(sections, event));
        loadPlanIfAvailable();

        return root;
    }

    private void showAboutInfo() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("About Productivity Planner");
        String contentHTML = """
                <html>
                    This planner can help you to plan your day effectively and get work done.<br>
                    Some of the features:
                    <ul>
                        <li> Incorporates a Pomodoro timer to assist in taking timely breaks between work sessions.
                        <li> Tasks in the Priorities section can have three states.
                        <ol>
                            <li> Incomplete (White)
                            <li> In-process (Green), and
                            <li> Completed (Gray).
                        </ol>
                        <li> Time of 'In-process tasks' is recorded during pomodoro working sessions.
                        <li> Completed tasks are saved in log file for the day, which can be accessed from File menu
                             or by clicking on the title section.
                        <li> Completed tasks are automatically removed after a short time delay.
                        <li> Every task can have additional notes for planning,
                             which is also saved in the log file after task completion.
                    </ul>
                    
                    If you find any bugs, or have suggestions, please do not hesitate to report the same.
                    
                    <br>
                    Programmed by: <strong>Sourabh Bhat</strong><br>
                    Provide feedback at: <a style="text-decoration:none" href="#">heySourabh@gmail.com</a><br>
                    Website: <a style="text-decoration:none" href="#">https://spbhat.in</a><br>
                </html>
                """;
        WebView content = new WebView();
        content.getEngine().loadContent(contentHTML);
        content.setContextMenuEnabled(false);
        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setPrefWidth(800);
        alert.getDialogPane().setPrefHeight(500);
        alert.setResizable(true);
        alert.showAndWait();
    }

    public static void showLog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Log of completed tasks");
        alert.setHeaderText("Today's tasks saved after completion");
        String logText = "Log file not found. Probably no task is completed so far.";
        try {
            logText = Files.readString(new File("plans", Planner.todayDateString + ".log").toPath());
        } catch (IOException ignore) {
        }
        alert.getDialogPane().setPrefWidth(800);
        alert.setContentText(logText);
        alert.setResizable(true);
        alert.showAndWait();
    }

    private void save(Node node, Event event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        ButtonType[] buttons = {
                new ButtonType("Don't Save", ButtonBar.ButtonData.NO),
                ButtonType.CANCEL,
                new ButtonType("Save", ButtonBar.ButtonData.OK_DONE)
        };
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(buttons);
        alert.setHeaderText("Save image and data?");
        alert.setContentText("");
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType.getButtonData().equals(ButtonBar.ButtonData.OK_DONE)) {
                saveImageAndData(node);
            }
            if (buttonType.equals(ButtonType.CANCEL)) {
                event.consume();
            }
        });
    }

    private void saveImageAndData(Node node) {
        String imageFilename = "plans/" + todayDateString + ".png";
        String dataFileName = "plans/" + todayDateString + ".dat";
        // Saving image
        final WritableImage snapshot = node.snapshot(null, null);
        try {
            System.out.println(imageFilename);
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "PNG",
                    new File(imageFilename));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Save data
        File dataFile = new File(dataFileName);
        try (PrintWriter fileWriter = new PrintWriter(dataFile)) {
            System.out.println(dataFileName);
            // save Projects section
            fileWriter.println(ProjectSection.projectDataProperties.size());
            for (ProjectDataProperty projProp : ProjectSection.projectDataProperties) {
                fileWriter.println(projProp.projectName.get());
                fileWriter.println(projProp.projectTasks.size());
                for (SimpleStringProperty projectTask : projProp.projectTasks) {
                    fileWriter.println(projectTask.get());
                }
            }

            // save People section
            fileWriter.println(sanitizeStringFromPlanner(
                    PeopleSection.peopleToReachOutProperty.get()));
            fileWriter.println(sanitizeStringFromPlanner(
                    PeopleSection.peopleWaitingOnProperty.get()));

            // save Priorities section - tasks and status
            // last one is the add button
            int numPrioritiesTasks = PrioritiesSection.prioritiesTaskList.size() - 1;
            fileWriter.println(numPrioritiesTasks);
            for (int t = 0; t < numPrioritiesTasks; t++) {
                EditableTask task = (EditableTask) PrioritiesSection.prioritiesTaskList.get(t);
                fileWriter.println(task.taskCompleted.isSelected() ? EditableTaskStatus.COMPLETE
                        : task.taskCompleted.isIndeterminate() ? EditableTaskStatus.IN_PROCESS
                        : EditableTaskStatus.INCOMPLETE);
                fileWriter.println(task.taskField.getText());
                fileWriter.println(task.expectedDuration.get().toMinutes());
                fileWriter.println(task.actualDuration.get().toMinutes());
                fileWriter.println(sanitizeStringFromPlanner(task.notes));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPlanIfAvailable() {
        // Go back up to four days
        for (int day = 0; day < 4; day++) {
            String dateString = now.minusDays(day).format(dateFormatter);
            String dataFileName = "plans/" + dateString + ".dat";
            File dataFile = new File(dataFileName);
            if (dataFile.exists()) {
                loadPlan(dataFile);
                break;
            }
        }
    }

    private void loadPlan(File dataFile) {
        try (Scanner fileScanner = new Scanner(dataFile)) {
            // Project section
            int numProj = Integer.parseInt(fileScanner.nextLine());
            if (ProjectSection.projectDataProperties.size() != numProj) {
                throw new IllegalStateException("The number of projects in file doesn't match.");
            }
            for (ProjectDataProperty projProp : ProjectSection.projectDataProperties) {
                projProp.projectName.set(fileScanner.nextLine());
                int numProjTasks = Integer.parseInt(fileScanner.nextLine());
                if (projProp.projectTasks.size() != numProjTasks) {
                    throw new IllegalStateException("The number of tasks under project doesn't match");
                }
                for (SimpleStringProperty projectTask : projProp.projectTasks) {
                    projectTask.set(fileScanner.nextLine());
                }
            }

            // People section
            String peopleToReachOut = deSanitizeStringFromFile(fileScanner.nextLine());
            PeopleSection.peopleToReachOutProperty.set(peopleToReachOut);
            String peopleWaitingOn = deSanitizeStringFromFile(fileScanner.nextLine());
            PeopleSection.peopleWaitingOnProperty.set(peopleWaitingOn);

            // Priorities section
            int numPrioritiesTasks = Integer.parseInt(fileScanner.nextLine());
            for (int t = 0; t < numPrioritiesTasks; t++) {
                String status = fileScanner.nextLine();
                String taskDescription = fileScanner.nextLine();
                int expectedDurationMinutes = PrioritiesSection.defaultExpectedDurationMinutes;
                int actualDurationMinutes = PrioritiesSection.defaultActualDurationMinutes;
                try {
                    expectedDurationMinutes = fileScanner.nextInt();
                    fileScanner.nextLine();
                    actualDurationMinutes = Integer.parseInt(fileScanner.nextLine());
                } catch (Exception ignore) {
                    // ignore in case of parsing error
                    // thus enabling to read files from old version
                }
                String notes = deSanitizeStringFromFile(fileScanner.nextLine());
                PrioritiesSection.addEditableTask(
                        taskDescription, EditableTaskStatus.valueOf(status),
                        expectedDurationMinutes, actualDurationMinutes, notes);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void sleepFor(Duration duration) {
        LockSupport.parkNanos(duration.toNanos());
    }

    private static final String newlineReplacement = "{newline}";

    private String sanitizeStringFromPlanner(String input) {
        return input.replace("\n", newlineReplacement);
    }

    private String deSanitizeStringFromFile(String input) {
        return input.replace(newlineReplacement, "\n");
    }
}
