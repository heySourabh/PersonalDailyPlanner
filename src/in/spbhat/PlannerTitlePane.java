/*
 * Copyright (c) 2021.
 * @author Sourabh P. Bhat ( https://spbhat.in/ )
 */

package in.spbhat;

import javafx.geometry.Pos;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Objects;

public class PlannerTitlePane extends VBox {
    final Font titleFont;

    public PlannerTitlePane() {
        titleFont = Font.loadFont(Objects.requireNonNull(getClass()
                .getResource("fonts/Cinzel-SemiBold.ttf")).toString(), 24);
        Text title = new Text(String.format("Productivity Planner (%s)", Planner.todayDateString));
        title.setFont(titleFont);
        title.setEffect(new InnerShadow());
        title.setFill(Color.WHITE);
        title.setUnderline(true);

        getChildren().addAll(title);
        setAlignment(Pos.CENTER);
    }
}
