/*
 * Copyright (c) 2020-2024.
 * @author Sourabh P. Bhat ( https://spbhat.in/ )
 */

package in.spbhat;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class PeopleSection extends Section {
    public final static SimpleStringProperty peopleToReachOutProperty = new SimpleStringProperty();
    public final static SimpleStringProperty peopleWaitingOnProperty = new SimpleStringProperty();

    public PeopleSection() {
        super("People", createContent(), true);
    }

    private static Pane createContent() {
        Pane peopleToReachOut = createPeopleForm(
                "People I need to reach out to today.",
                "List of people I have to reach out to today, no matter what:",
                peopleToReachOutProperty);
        Pane peopleWaitingOn = createPeopleForm(
                "People I'm waiting on.",
                "List of people who I need something from to move forward:",
                peopleWaitingOnProperty);
        HBox peopleContent = new HBox(peopleToReachOut, peopleWaitingOn);
        peopleContent.setSpacing(10);

        HBox.setHgrow(peopleToReachOut, Priority.ALWAYS);
        HBox.setHgrow(peopleWaitingOn, Priority.ALWAYS);

        return peopleContent;
    }

    private static Pane createPeopleForm(String heading, String note, SimpleStringProperty property) {
        Label headingLabel = new Label(heading);
        headingLabel.setFont(Section.labelFont);

        Label noteLabel = new Label(note);
        noteLabel.setFont(Section.notesFont);
        noteLabel.setTextFill(Color.GRAY);

        TextArea writeArea = new TextArea();
        property.bindBidirectional(writeArea.textProperty());
        writeArea.setFont(Section.writeAreaFont);
        writeArea.setWrapText(true);
        writeArea.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        writeArea.setPrefRowCount(6);
        VBox.setVgrow(writeArea, Priority.ALWAYS);

        VBox peopleForm = new VBox(headingLabel, noteLabel, writeArea);
        peopleForm.setAlignment(Pos.CENTER);
        return peopleForm;
    }
}
