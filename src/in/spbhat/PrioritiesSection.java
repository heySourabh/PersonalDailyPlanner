/*
 * Copyright (c) 2020.
 * @author Sourabh P. Bhat ( https://spbhat.in/ )
 */

package in.spbhat;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class PrioritiesSection extends Section {
    public PrioritiesSection() {
        super("Priorities", createContent());
    }

    private static Pane createContent() {
        return new HBox();
    }
}
