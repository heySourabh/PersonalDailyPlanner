/*
 * Copyright (c) 2020-2024.
 * @author Sourabh P. Bhat ( https://spbhat.in/ )
 */

package in.spbhat;

import in.spbhat.icons.Icon;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.util.Duration;

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
    boolean sectionCollapsed = false;

    public Section(String title, Pane content, boolean collapsable) {
        titleText = new Label(title);
        titleText.setFont(sectionTitleFont);
        HBox titleBox = new HBox(titleText);
        titleBox.setSpacing(10);
        if (collapsable) {
            insertCollapsableButton(titleBox, content);
        }

        getChildren().addAll(titleBox, content);
        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(5));
        setSpacing(2);

        VBox.setVgrow(titleText, Priority.NEVER);
        VBox.setVgrow(content, Priority.ALWAYS);
    }

    private void insertCollapsableButton(HBox titleBox, Pane content) {
        int btnSize = 20;
        Button collapseBtn = new Button("", Icon.graphic("collapsable.png", btnSize));
        collapseBtn.setShape(new Circle(btnSize * 0.5));
        collapseBtn.setMinSize(btnSize * 1.3, btnSize * 1.3);
        collapseBtn.setMaxSize(btnSize * 1.3, btnSize * 1.3);
        collapseBtn.setTooltip(new Tooltip("Hide / Show Section"));
        collapseBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        titleBox.getChildren().addFirst(collapseBtn);
        collapseBtn.setOnAction(e -> {
            sectionCollapsed = !sectionCollapsed;
            if (sectionCollapsed) {
                Transition transition = playCollapseTransition(content, collapseBtn);
                transition.setOnFinished(event -> getChildren().remove(content));
            } else {
                if (getChildren().getLast() != content) {
                    getChildren().add(content);
                    playExpandTransition(content, collapseBtn);
                }
            }
        });
    }

    private static void playExpandTransition(Pane content, Button collapseBtn) {
        Duration transitionDuration = Duration.seconds(0.25);
        Transition transition = createTransition(content, collapseBtn, transitionDuration,
                0, 1, 1, 0);
        transition.play();
    }

    private static Transition playCollapseTransition(Pane content, Button collapseBtn) {
        Duration transitionDuration = Duration.seconds(0.2);
        Transition transition = createTransition(content, collapseBtn, transitionDuration,
                -content.getLayoutBounds().getHeight() / 2,
                0, 0, -90);
        transition.play();
        return transition;
    }

    private static Transition createTransition(Pane content, Button collapseBtn, Duration duration,
                                               double translateTo, double scaleTo,
                                               double fadeTo, double rotateTo) {
        TranslateTransition translateTransition = new TranslateTransition(duration, content);
        translateTransition.setToY(translateTo);
        ScaleTransition scaleTransition = new ScaleTransition(duration, content);
        scaleTransition.setToY(scaleTo);
        FadeTransition fadeTransition = new FadeTransition(duration, content);
        fadeTransition.setToValue(fadeTo);
        RotateTransition rotateTransition = new RotateTransition(duration, collapseBtn.getGraphic());
        rotateTransition.setToAngle(rotateTo);
        return new ParallelTransition(translateTransition, scaleTransition, fadeTransition, rotateTransition);
    }
}
