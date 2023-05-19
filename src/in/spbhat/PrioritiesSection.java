/*
 * Copyright (c) 2020.
 * @author Sourabh P. Bhat ( https://spbhat.in/ )
 */

package in.spbhat;

import in.spbhat.EditableTask.EditableTaskStatus;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class PrioritiesSection extends Section {
    public static ObservableList<Node> prioritiesTaskList;
    private static TilePane taskListPane;

    static final int defaultExpectedDurationMinutes = 30;
    static final int defaultActualDurationMinutes = 0;

    public PrioritiesSection() {
        super("Priorities", createContent());
    }

    private static Pane createContent() {
        Label headingLabel = new Label(
                "The main things I must complete today, no matter what.");
        headingLabel.setFont(Section.labelFont);
        Label noteLabel = new Label(
                "List of priorities and to-dos that must be accomplished today " +
                        "and DO these before getting trapped in other people's agendas.");
        noteLabel.setFont(Section.notesFont);
        noteLabel.setTextFill(Color.GRAY);

        taskListPane = new TilePane(Orientation.HORIZONTAL, 20, 2);
        prioritiesTaskList = taskListPane.getChildren();
        Button addBtn = new Button("Add", new ImageView(new Image(PrioritiesSection.class
                .getResource("icons/add.png").toString(),
                20, -1, true, true)));
        addBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        addBtn.setTooltip(new Tooltip("Add New Task"));
        addBtn.setOnAction(event ->
                addEditableTask("", EditableTaskStatus.INCOMPLETE, defaultExpectedDurationMinutes, defaultActualDurationMinutes)
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

    public static EditableTask addEditableTask(String description, EditableTaskStatus status, int expectedDurationMinutes, int actualDurationMinutes) {
        EditableTask newTask = new EditableTask(taskListPane, description, status, expectedDurationMinutes, actualDurationMinutes);
        prioritiesTaskList.add(taskListPane.getChildren().size() - 1, newTask);
        return newTask;
    }
}
