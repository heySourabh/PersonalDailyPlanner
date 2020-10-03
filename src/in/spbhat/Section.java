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
    final Font sectionTitleFont;

    public Section(String title, Pane content) {
        sectionTitleFont = Font.loadFont(getClass()
                .getResource("fonts/Cinzel-Bold.ttf").toString(), 32);

        Text titleText = new Text(title);
        titleText.setFont(sectionTitleFont);
        titleText.setEffect(new InnerShadow());
        titleText.setFill(Color.WHITE);

        getChildren().addAll(titleText, content);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(15));

        VBox.setVgrow(content, Priority.ALWAYS);
    }
}
