/*
 * Copyright (c) 2020.
 * @author Sourabh P. Bhat ( https://spbhat.in/ )
 */

package in.spbhat;

import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.List;

public class ProjectDataProperty {
    public static final int NUM_PROJECT_TASKS = 5;

    public final SimpleStringProperty projectName;
    public final List<SimpleStringProperty> projectTasks;

    public ProjectDataProperty() {
        projectName = new SimpleStringProperty();
        projectTasks = new ArrayList<>(NUM_PROJECT_TASKS);
        for (int i = 0; i < NUM_PROJECT_TASKS; i++) {
            projectTasks.add(new SimpleStringProperty());
        }
    }
}
