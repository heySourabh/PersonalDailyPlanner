/*
 * Copyright (c) 2023.
 * @author Sourabh P. Bhat ( https://spbhat.in/ )
 */

package in.spbhat;

import in.spbhat.icons.Icon;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.Glow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.File;
import java.time.Duration;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.locks.LockSupport;

public class PomodoroSection extends Section {

    public enum PomodoroState {
        WORKING(Duration.ofMinutes(25), Color.GREENYELLOW, "Working"),
        SHORT_BREAK(Duration.ofMinutes(5), Color.DEEPSKYBLUE, "Short Break"),
        LONG_BREAK(Duration.ofMinutes(10), Color.DEEPPINK, "Long Break");

        private Duration duration;
        private final Color color;
        private final String str;

        PomodoroState(Duration duration, Color color, String str) {
            this.duration = duration;
            this.color = color;
            this.str = str;
        }

        @Override
        public String toString() {
            return this.str;
        }
    }

    private static int longBreakInterval = 4;
    private static final SimpleDoubleProperty soundLevel = new SimpleDoubleProperty(5);
    public static boolean pomodoroRunning = false;
    public static PomodoroState currentPomodoroState = null;

    public PomodoroSection() {
        super("Pomodoro | Not Started Yet: 00:00:00", createContent());
        startRunningPomodoroThread();
    }

    private static Button startStopBtn;
    private static HBox indicatorsContent;

    private static Pane createContent() {
        Indicator last = new Indicator(PomodoroState.LONG_BREAK);
        last.setActive(true);
        indicatorsContent = new HBox();
        indicatorsContent.setSpacing(10);
        indicatorsContent.setAlignment(Pos.CENTER);

        Region emptySpace = new Region();
        HBox.setHgrow(emptySpace, Priority.ALWAYS);

        startStopBtn = new Button("Start");
        stopPomodoro();
        startStopBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        startStopBtn.setTooltip(new Tooltip("Start / Stop Pomodoro"));
        startStopBtn.setOnAction(e -> {
            if (pomodoroRunning) {
                stopPomodoro();
            } else {
                startPomodoro();
            }
        });

        Button pomodoroSettings = new Button("Settings", Icon.graphic("sliders.png", 20));
        pomodoroSettings.setOnAction(e -> showPomodoroSettings());
        pomodoroSettings.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        pomodoroSettings.setTooltip(new Tooltip("Pomodoro Settings"));
        HBox controlsContent = new HBox(startStopBtn, pomodoroSettings);
        controlsContent.setSpacing(5);

        HBox pomodoroContent = new HBox(indicatorsContent, emptySpace, controlsContent);
        pomodoroContent.setPadding(new Insets(20));

        return pomodoroContent;
    }

    private static Indicator addPomodoroIndicator(PomodoroState pomodoroState) {
        Indicator indicator = new Indicator(pomodoroState);
        Platform.runLater(() -> indicatorsContent.getChildren().add(indicator));
        return indicator;
    }

    private static void stopPomodoro() {
        pomodoroRunning = false;
        stopBackgroundSound();
        startStopBtn.setGraphic(Icon.graphic("start.png", 20));
        System.out.println("Stopping pomodoro");
    }

    private static void startPomodoro() {
        pomodoroRunning = true;
        startBackgroundSound(currentPomodoroState);
        startStopBtn.setGraphic(Icon.graphic("stop.png", 20));
        System.out.println("Starting pomodoro");
    }

    private static boolean pomodoroThreadStarted = false;

    private void startRunningPomodoroThread() {
        if (pomodoroThreadStarted) return;
        pomodoroThreadStarted = true;
        Thread pomodoroThread = new Thread(() -> {
            for (int pomodoroNumber = 0; true; pomodoroNumber++) {
                for (int state = 0; state < 2; state++) {
                    PomodoroState pomodoroState;
                    if (state % 2 == 0) {
                        pomodoroState = PomodoroState.WORKING;
                    } else if ((pomodoroNumber + 1) % longBreakInterval == 0) {
                        pomodoroState = PomodoroState.LONG_BREAK;
                    } else {
                        pomodoroState = PomodoroState.SHORT_BREAK;
                    }
                    currentPomodoroState = pomodoroState;

                    Indicator indicator = addPomodoroIndicator(pomodoroState);
                    for (int seconds = 0; seconds < pomodoroState.duration.toSeconds(); seconds++) {
                        do {
                            indicator.setActive(pomodoroRunning);
                            sleepFor(Duration.ofSeconds(1));
                        } while (!pomodoroRunning);

                        if (seconds == 0) { // Show message about the Pomodoro state
                            pomodoroRunning = false;
                            stopBackgroundSound();
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Time for '%s'".formatted(pomodoroState));
                                alert.setHeaderText("Begin: " + pomodoroState);
                                alert.setContentText("Duration: " + format(pomodoroState.duration));
                                alert.showAndWait();
                                startBackgroundSound(pomodoroState);
                                pomodoroRunning = true;
                            });
                        }

                        Duration remainingSeconds = pomodoroState.duration.minusSeconds(seconds);
                        Platform.runLater(() -> super.titleText.setText("Pomodoro | %s: %s".formatted(pomodoroState, format(remainingSeconds))));
                    }
                    indicator.setActive(false);
                }
            }
        });

        pomodoroThread.setDaemon(true);
        pomodoroThread.start();
    }

    private static void sleepFor(Duration duration) {
        LockSupport.parkNanos(duration.toNanos());
    }

    private static String format(Duration duration) {
        int hr = duration.toHoursPart();
        int min = duration.toMinutesPart();
        int sec = duration.toSecondsPart();
        return (hr != 0 ? "%02d:".formatted(hr) : "") + "%02d:%02d".formatted(min, sec);
    }

    private static void showPomodoroSettings() {
        TextField workingDurationField = new TextField(String.valueOf(PomodoroState.WORKING.duration.toMinutes()));
        workingDurationField.setPrefColumnCount(3);
        HBox workingBox = new HBox(new Label("Working:                "), workingDurationField, new Label("Minutes"));
        workingBox.setSpacing(5);

        TextField shortBreakDurationField = new TextField(String.valueOf(PomodoroState.SHORT_BREAK.duration.toMinutes()));
        shortBreakDurationField.setPrefColumnCount(3);
        HBox shortBreakBox = new HBox(new Label("Short Break:          "), shortBreakDurationField, new Label("Minutes"));
        shortBreakBox.setSpacing(5);

        TextField longBreakDurationField = new TextField(String.valueOf(PomodoroState.LONG_BREAK.duration.toMinutes()));
        longBreakDurationField.setPrefColumnCount(3);
        HBox longBreakBox = new HBox(new Label("Long Break:           "), longBreakDurationField, new Label("Minutes"));
        longBreakBox.setSpacing(5);

        TextField longBreakIntervalField = new TextField(String.valueOf(longBreakInterval));
        longBreakIntervalField.setPrefColumnCount(3);
        HBox longBreakIntervalBox = new HBox(new Label("Long Break After:  "), longBreakIntervalField, new Label("Pomodoros"));
        longBreakIntervalBox.setSpacing(5);

        Slider soundLevelSlider = new Slider(0, 100, soundLevel.get());
        soundLevelSlider.setBlockIncrement(5);
        soundLevelSlider.setShowTickMarks(true);
        soundLevelSlider.setShowTickLabels(true);
        HBox volumeBox = new HBox(new Label("Sound Level: "), soundLevelSlider);
        volumeBox.setSpacing(20);

        VBox contentBox = new VBox(workingBox, shortBreakBox, longBreakBox, longBreakIntervalBox, volumeBox);
        contentBox.setSpacing(16);
        contentBox.setPadding(new Insets(10, 100, 10, 100));

        DialogPane dialogPane = new DialogPane();
        dialogPane.setGraphic(Icon.graphic("sliders.png", 64));
        dialogPane.setHeaderText("Pomodoro Settings");
        dialogPane.setContent(contentBox);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setDialogPane(dialogPane);
        alert.setTitle("Pomodoro Settings");
        alert.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                int workingMinutes = Integer.parseInt(workingDurationField.getText());
                PomodoroState.WORKING.duration = Duration.ofMinutes(workingMinutes);

                int shortBreakMinutes = Integer.parseInt(shortBreakDurationField.getText());
                PomodoroState.SHORT_BREAK.duration = Duration.ofMinutes(shortBreakMinutes);

                int longBreakMinutes = Integer.parseInt(longBreakDurationField.getText());
                PomodoroState.LONG_BREAK.duration = Duration.ofMinutes(longBreakMinutes);

                longBreakInterval = Integer.parseInt(longBreakIntervalField.getText());
                soundLevel.set(soundLevelSlider.getValue());
            }
        });
    }

    private static MediaPlayer bgSoundPlayer;

    private static void startBackgroundSound(PomodoroState pomodoroState) {
        if (bgSoundPlayer != null) {
            bgSoundPlayer.dispose();
        }
        chooseBackgroundSoundFile(pomodoroState).ifPresent(file -> {
            String source = file.toURI().toString();
            bgSoundPlayer = new MediaPlayer(new Media(source));
            bgSoundPlayer.volumeProperty().bind(soundLevel.divide(100.0));
            bgSoundPlayer.setOnEndOfMedia(() -> startBackgroundSound(pomodoroState));
            bgSoundPlayer.play();
        });
    }

    private static void stopBackgroundSound() {
        if (bgSoundPlayer != null) {
            bgSoundPlayer.stop();
        }
    }

    private static Optional<File> chooseBackgroundSoundFile(PomodoroState pomodoroState) {
        File baseSoundPath = switch (pomodoroState) {
            case WORKING -> new File("sounds/working/");
            case SHORT_BREAK, LONG_BREAK -> new File("sounds/break/");
        };
        File[] soundFiles = baseSoundPath.listFiles(file -> file.getPath().endsWith(".mp3"));

        if (soundFiles == null || soundFiles.length == 0) {
            System.out.println("Unable to locate mp3 files in the folder: " + baseSoundPath.getAbsolutePath());
            return Optional.empty();
        }
        File chosenFile = soundFiles[new Random().nextInt(soundFiles.length)];
        System.out.println("Sound file:" + chosenFile.getAbsoluteFile());
        return Optional.of(chosenFile);
    }

    public static class Indicator extends Circle {
        public final PomodoroState pomodoroState;
        private final AnimationTimer timer;
        private boolean isActive = false;

        Indicator(PomodoroState state) {
            this.pomodoroState = state;
            int radius = switch (state) {
                case WORKING -> 16;
                case SHORT_BREAK -> 5;
                case LONG_BREAK -> 8;
            };
            setRadius(radius);
            setFill(state.color);
            Glow effect = new Glow();
            effect.setInput(new InnerShadow());
            setEffect(effect);
            Tooltip tooltip = new Tooltip(state.toString());
            setOnMouseEntered(e -> tooltip.show(this, e.getScreenX() + 20, e.getScreenY() + 16));
            setOnMouseExited(e -> tooltip.hide());
            timer = new AnimationTimer() {
                double time = 0.0;

                @Override
                public void handle(long now) {
                    time += 1.0 / 30;
                    double f = Math.sin(time);
                    setOpacity(f * f);
                }
            };
        }

        void setActive(boolean isActive) {
            if (this.isActive == isActive) {
                return;
            }
            this.isActive = isActive;
            if (isActive) {
                timer.start();
            } else {
                timer.stop();
                setOpacity(1.0);
            }
        }
    }
}
