/*
 * Copyright (c) 2020.
 * @author Sourabh P. Bhat ( https://spbhat.in/ )
 */

package in.spbhat;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class Section extends VBox {
    final static Font sectionTitleFont = Font.loadFont(Section.class
            .getResource("fonts/Cinzel-Bold.ttf").toString(), 24);
    final static Font notesFont = Font.loadFont(Section.class
            .getResource("fonts/Montserrat-Regular.ttf").toString(), 12);
    final static Font labelFont = Font.loadFont(Section.class
            .getResource("fonts/Montserrat-SemiBold.ttf").toString(), 16);

    public Section(String title, Pane content) {
        Text titleText = new Text(title);
        titleText.setFont(sectionTitleFont);
        titleText.setEffect(new InnerShadow());
        titleText.setFill(Color.RED);

        getChildren().addAll(titleText, content);
        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(5));
        setSpacing(2);

        VBox.setVgrow(titleText, Priority.NEVER);
        VBox.setVgrow(content, Priority.ALWAYS);
    }
}
