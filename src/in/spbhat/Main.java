/*
 * Copyright (c) 2020.
 * @author Sourabh P. Bhat ( https://spbhat.in/ )
 */

package in.spbhat;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setScene(new Scene(createContent(), 800, 800));
        stage.show();
    }

    private Parent createContent() {
        BorderPane root = new BorderPane();
        root.setTop(new PlannerTitlePane());

        Section projectsSection = new ProjectSection();
        Section peopleSection = new PeopleSection();
        Section prioritiesSection = new PrioritiesSection();
        VBox sections = new VBox(
                new Separator(),
                projectsSection,
                new Separator(),
                peopleSection,
                new Separator(),
                prioritiesSection);
        VBox.setVgrow(projectsSection, Priority.ALWAYS);
        VBox.setVgrow(peopleSection, Priority.ALWAYS);
        VBox.setVgrow(prioritiesSection, Priority.ALWAYS);

        sections.setAlignment(Pos.TOP_CENTER);
        root.setCenter(sections);

        return root;
    }
}
