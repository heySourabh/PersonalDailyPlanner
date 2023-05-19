/*
 * Copyright (c) 2021.
 * @author Sourabh P. Bhat ( https://spbhat.in/ )
 */

package in.spbhat;

import in.spbhat.EditableTask.EditableTaskStatus;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.embed.swing.SwingFXUtils;
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
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.locks.LockSupport;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;

public class Planner extends Application {
    static final DateTimeFormatter dateFormatter = ofPattern("dd-MM-yyyy");
    static final LocalDateTime now = now();
    static final String todayDateString = now.format(dateFormatter);
    static Section projectsSection;
    static Section peopleSection;
    static Section prioritiesSection;
    static VBox sections;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(createContent(), 900, 1000);
        stage.setScene(scene);
        stage.getIcons().addAll(
                new Image(Objects.requireNonNull(Planner.class
                        .getResource("icons/icon_64.png")).toString()),
                new Image(Objects.requireNonNull(Planner.class
                        .getResource("icons/icon_128.png")).toString()));
        stage.setTitle("Productivity Planner (by Sourabh Bhat)");
        stage.setOnCloseRequest(event -> save(sections));
        moveToFrontIntermittently(stage);
        stage.show();
    }

    private final static int REMINDER_MINUTES = 30;

    private void moveToFrontIntermittently(Stage stage) {
        final Thread thread = new Thread(() -> {
            while (true) {
                long waitNanos = REMINDER_MINUTES * 60 * 1_000_000_000L;
                LockSupport.parkNanos(waitNanos);
                Platform.runLater(() -> {
                    stage.setIconified(false);
                    stage.toFront();
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private Parent createContent() {
        BorderPane root = new BorderPane();
        Menu fileMenu = new Menu("File");
        MenuItem saveMenuItem = new MenuItem("_Save");
        saveMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        fileMenu.getItems().add(saveMenuItem);
        MenuBar menuBar = new MenuBar(fileMenu);
        menuBar.setUseSystemMenuBar(true);

        PlannerTitlePane titlePane = new PlannerTitlePane();
        HBox topPane = new HBox(menuBar, titlePane);
        HBox.setHgrow(titlePane, Priority.ALWAYS);
        topPane.setAlignment(Pos.CENTER);

        root.setTop(topPane);

        projectsSection = new ProjectSection();
        peopleSection = new PeopleSection();
        prioritiesSection = new PrioritiesSection();
        sections = new VBox(
                projectsSection, new Separator(),
                peopleSection, new Separator(),
                prioritiesSection);
        VBox.setVgrow(projectsSection, Priority.ALWAYS);
        VBox.setVgrow(peopleSection, Priority.ALWAYS);
        VBox.setVgrow(prioritiesSection, Priority.ALWAYS);

        sections.setAlignment(Pos.TOP_CENTER);
        sections.setPadding(new Insets(0, 10, 10, 10));
        root.setCenter(sections);

        saveMenuItem.setOnAction(event -> save(sections));
        loadPlanIfAvailable();

        startDurationUpdateTimer();

        return root;
    }

    private void sleepFor(Duration duration) {
        LockSupport.parkNanos(duration.toNanos());
    }

    private void startDurationUpdateTimer() {
        Duration durationBetweenUpdates = Duration.ofSeconds(5);
        Thread thread = new Thread(() -> {
            while (true) {
                sleepFor(durationBetweenUpdates);
                for (Node node : PrioritiesSection.prioritiesTaskList) {
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

    private void save(Node node) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Save image and data?");
        alert.setContentText("");
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType.equals(ButtonType.OK)) {
                saveImageAndData(node);
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
                PrioritiesSection.addEditableTask(
                        taskDescription, EditableTaskStatus.valueOf(status),
                        expectedDurationMinutes, actualDurationMinutes);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static final String newlineReplacement = "{newline}";

    private String sanitizeStringFromPlanner(String input) {
        return input.replace("\n", newlineReplacement);
    }

    private String deSanitizeStringFromFile(String input) {
        return input.replace(newlineReplacement, "\n");
    }
}
