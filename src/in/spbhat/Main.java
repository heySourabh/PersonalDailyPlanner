/*
 * Copyright (c) 2020.
 * @author Sourabh P. Bhat ( https://spbhat.in/ )
 */

package in.spbhat;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;

public class Main extends Application {
    static final String dateString = now().format(ofPattern("dd-MM-yyyy"));

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(createContent(), 800, 800);
        stage.setScene(scene);
        stage.show();
    }

    private Parent createContent() {
        BorderPane root = new BorderPane();
        Menu fileMenu = new Menu("File");
        MenuItem saveMenuItem = new MenuItem("_Save");
        saveMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        fileMenu.getItems().add(saveMenuItem);
        MenuBar menuBar = new MenuBar(fileMenu);
        menuBar.setUseSystemMenuBar(true);

        VBox topPane = new VBox(menuBar);

        root.setTop(topPane);

        Section projectsSection = new ProjectSection();
        Section peopleSection = new PeopleSection();
        Section prioritiesSection = new PrioritiesSection();
        VBox sections = new VBox(
                new PlannerTitlePane(), new Separator(),
                projectsSection, new Separator(),
                peopleSection, new Separator(),
                prioritiesSection);
        VBox.setVgrow(projectsSection, Priority.ALWAYS);
        VBox.setVgrow(peopleSection, Priority.ALWAYS);
        VBox.setVgrow(prioritiesSection, Priority.ALWAYS);

        sections.setAlignment(Pos.TOP_CENTER);
        sections.setPadding(new Insets(0, 10, 10, 10));
        root.setCenter(sections);

        saveMenuItem.setOnAction(event -> saveSnapshot(sections));

        return root;
    }

    private void saveSnapshot(Node node) {
        final WritableImage snapshot = node.snapshot(null, null);
        try {
            String fileName = dateString + ".png";
            System.out.println(fileName);
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "PNG",
                    new File(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
