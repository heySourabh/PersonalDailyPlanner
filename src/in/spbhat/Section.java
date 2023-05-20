/*
 * Copyright (c) 2020.
 * @author Sourabh P. Bhat ( https://spbhat.in/ )
 */

package in.spbhat;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class Section extends VBox {
    final static Font sectionTitleFont = Font.loadFont(Section.class
            .getResource("fonts/Cinzel-Bold.ttf").toString(), 24);
    final static Font notesFont = Font.loadFont(Section.class
            .getResource("fonts/Montserrat-Regular.ttf").toString(), 12);
    final static Font labelFont = Font.loadFont(Section.class
            .getResource("fonts/Montserrat-SemiBold.ttf").toString(), 16);
    final static Font writeAreaFont = Font.loadFont(Section.class
            .getResource("fonts/Tillana-Regular.ttf").toString(), 16);

    Label titleText;

    public Section(String title, Pane content) {
        titleText = new Label(title);
        titleText.setFont(sectionTitleFont);
        HBox titleBox = new HBox(titleText);

        getChildren().addAll(titleBox, content);
        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(5));
        setSpacing(2);

        VBox.setVgrow(titleText, Priority.NEVER);
        VBox.setVgrow(content, Priority.ALWAYS);
    }
}
