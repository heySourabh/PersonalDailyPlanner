/*
 * Copyright (c) 2020.
 * @author Sourabh P. Bhat ( https://spbhat.in/ )
 */

package in.spbhat;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class PeopleSection extends Section {
    public PeopleSection() {
        super("People", createContent());
    }

    private static Pane createContent() {
        return new HBox();
    }
}
