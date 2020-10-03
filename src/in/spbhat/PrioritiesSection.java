/*
 * Copyright (c) 2020.
 * @author Sourabh P. Bhat ( https://spbhat.in/ )
 */

package in.spbhat;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class PrioritiesSection extends Section {
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

        TextArea writeAreaLeft = new TextArea();
        writeAreaLeft.setFont(Section.writeAreaFont);
        writeAreaLeft.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        HBox.setHgrow(writeAreaLeft, Priority.ALWAYS);
        VBox.setVgrow(writeAreaLeft, Priority.ALWAYS);

        TextArea writeAreaRight = new TextArea();
        writeAreaRight.setFont(Section.writeAreaFont);
        writeAreaRight.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        HBox.setHgrow(writeAreaRight, Priority.ALWAYS);
        VBox.setVgrow(writeAreaRight, Priority.ALWAYS);

        HBox writeArea = new HBox(writeAreaLeft, writeAreaRight);
        writeArea.setSpacing(10);

        VBox content = new VBox(headingLabel, noteLabel, writeArea);
        content.setAlignment(Pos.CENTER);

        return content;
    }
}
