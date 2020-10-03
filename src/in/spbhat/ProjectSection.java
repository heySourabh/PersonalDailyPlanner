/*
 * Copyright (c) 2020.
 * @author Sourabh P. Bhat ( https://spbhat.in/ )
 */

package in.spbhat;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class ProjectSection extends Section {
    final static Font projectTitleTextFont = Font.loadFont(Section.class
            .getResource("fonts/TheGirlNextDoor-Regular.ttf").toString(), 16);
    final static Font taskDetailTextFont = Font.loadFont(Section.class
            .getResource("fonts/Tillana-Regular.ttf").toString(), 16);

    public ProjectSection() {
        super("Projects", createContent());
    }

    private static Pane createContent() {
        Pane project1 = createProjectForm(1);
        Pane project2 = createProjectForm(2);
        Pane project3 = createProjectForm(3);
        HBox content = new HBox(project1, project2, project3);

        HBox.setHgrow(project1, Priority.ALWAYS);
        HBox.setHgrow(project2, Priority.ALWAYS);
        HBox.setHgrow(project3, Priority.ALWAYS);

        content.setSpacing(20);
        content.setAlignment(Pos.CENTER);

        return content;
    }

    private static Pane createProjectForm(int number) {
        Label titleLabel = new Label("#" + number + ": ");
        titleLabel.setFont(Section.labelFont);
        TextField titleField = new TextField();
        titleField.setPromptText("Project Name");
        titleField.setFont(projectTitleTextFont);
        titleField.setPadding(new Insets(2, 5, 2, 5));
        titleField.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        HBox projectTitle = new HBox(titleLabel, titleField);
        projectTitle.setAlignment(Pos.CENTER);
        HBox.setHgrow(titleField, Priority.ALWAYS);

        Label note = new Label("5 big things I must do \nto move this project forward:");
        note.setFont(Section.notesFont);
        note.setPadding(new Insets(5, 5, 5, 30));

        VBox tasks = new VBox();
        for (int num = 1; num <= 5; num++) {
            Label numLabel = new Label("" + num + ". ");
            numLabel.setFont(Section.labelFont);
            TextField taskDetail = new TextField();
            taskDetail.setPadding(new Insets(2, 5, 2, 5));
            taskDetail.setFont(taskDetailTextFont);
            taskDetail.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));

            HBox task = new HBox(numLabel, taskDetail);
            task.setAlignment(Pos.CENTER);
            tasks.getChildren().add(task);
            HBox.setHgrow(taskDetail, Priority.ALWAYS);
            VBox.setMargin(task, new Insets(5));
        }

        return new VBox(projectTitle, note, tasks);
    }
}
