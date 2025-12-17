/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.editor;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Visual Robot Configuration Editor.
 *
 * <p>
 * Provides a drag-and-drop interface for building robot configurations:
 * <ul>
 *   <li>Component palette with sensors, actuators, processors</li>
 *   <li>Visual canvas for arrangement</li>
 *   <li>Property editor for component configuration</li>
 *   <li>Export to YAML/JSON configuration</li>
 * </ul>
 * </p>
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.6.0
 */
public class RobotEditor extends Application {

    private Pane canvas;
    private VBox propertyPanel;
    private TextArea codePreview;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("JRobotics Visual Editor");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-font-family: 'Segoe UI', Arial, sans-serif;");

        // Menu bar
        root.setTop(createMenuBar());

        // Component palette (left)
        root.setLeft(createPalette());

        // Main canvas (center)
        root.setCenter(createCanvas());

        // Property panel (right)
        root.setRight(createPropertyPanel());

        // Code preview (bottom)
        root.setBottom(createCodePreview());

        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        fileMenu.getItems().addAll(
                new MenuItem("New Configuration"),
                new MenuItem("Open..."),
                new MenuItem("Save"),
                new MenuItem("Save As..."),
                new SeparatorMenuItem(),
                new MenuItem("Export as YAML"),
                new MenuItem("Export as JSON"),
                new SeparatorMenuItem(),
                new MenuItem("Exit")
        );

        Menu editMenu = new Menu("Edit");
        editMenu.getItems().addAll(
                new MenuItem("Undo"),
                new MenuItem("Redo"),
                new SeparatorMenuItem(),
                new MenuItem("Cut"),
                new MenuItem("Copy"),
                new MenuItem("Paste"),
                new MenuItem("Delete")
        );

        Menu viewMenu = new Menu("View");
        viewMenu.getItems().addAll(
                new CheckMenuItem("Show Grid"),
                new CheckMenuItem("Show Connections"),
                new SeparatorMenuItem(),
                new MenuItem("Zoom In"),
                new MenuItem("Zoom Out"),
                new MenuItem("Fit to Window")
        );

        Menu helpMenu = new Menu("Help");
        helpMenu.getItems().addAll(
                new MenuItem("Documentation"),
                new MenuItem("Keyboard Shortcuts"),
                new SeparatorMenuItem(),
                new MenuItem("About JRobotics Editor")
        );

        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, helpMenu);
        return menuBar;
    }

    private VBox createPalette() {
        VBox palette = new VBox(10);
        palette.setPadding(new Insets(10));
        palette.setPrefWidth(200);
        palette.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Components");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Sensors section
        TitledPane sensors = new TitledPane("Sensors", createComponentList(
                "Lidar", "IMU", "Camera", "Ultrasonic", "GPS", "Encoder"
        ));
        sensors.setExpanded(true);

        // Actuators section
        TitledPane actuators = new TitledPane("Actuators", createComponentList(
                "DC Motor", "Servo", "Stepper", "LED", "Speaker"
        ));

        // Processors section
        TitledPane processors = new TitledPane("Processors", createComponentList(
                "Path Planner", "SLAM", "Object Detection", "Behavior Tree", "PID Controller"
        ));

        // Robot Types section
        TitledPane robotTypes = new TitledPane("Robot Types", createComponentList(
                "Differential Drive", "Ackermann", "Holonomic", "Humanoid", "Arm"
        ));

        Accordion accordion = new Accordion(sensors, actuators, processors, robotTypes);
        accordion.setExpandedPane(sensors);

        palette.getChildren().addAll(title, new Separator(), accordion);
        return palette;
    }

    private VBox createComponentList(String... names) {
        VBox list = new VBox(5);
        for (String name : names) {
            Button btn = new Button(name);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setStyle("-fx-background-color: white; -fx-border-color: #ccc;");
            btn.setOnAction(e -> addComponentToCanvas(name));
            list.getChildren().add(btn);
        }
        return list;
    }

    private StackPane createCanvas() {
        canvas = new Pane();
        canvas.setStyle("-fx-background-color: white;");

        // Grid background
        canvas.setStyle("-fx-background-color: linear-gradient(from 0px 0px to 20px 0px, repeat, #f0f0f0 1px, transparent 1px),"
                + "linear-gradient(from 0px 0px to 0px 20px, repeat, #f0f0f0 1px, transparent 1px),"
                + "white;");

        // Add sample robot shape
        addRobotBaseToCanvas();

        StackPane canvasContainer = new StackPane(canvas);
        canvasContainer.setStyle("-fx-border-color: #ccc;");
        return canvasContainer;
    }

    private void addRobotBaseToCanvas() {
        // Robot body
        Rectangle body = new Rectangle(200, 150);
        body.setFill(Color.LIGHTGRAY);
        body.setStroke(Color.DARKGRAY);
        body.setStrokeWidth(2);
        body.setArcWidth(20);
        body.setArcHeight(20);
        body.setLayoutX(300);
        body.setLayoutY(250);

        // Wheels
        Rectangle leftWheel = new Rectangle(20, 60);
        leftWheel.setFill(Color.DARKGRAY);
        leftWheel.setLayoutX(280);
        leftWheel.setLayoutY(295);

        Rectangle rightWheel = new Rectangle(20, 60);
        rightWheel.setFill(Color.DARKGRAY);
        rightWheel.setLayoutX(500);
        rightWheel.setLayoutY(295);

        // Direction indicator
        Circle indicator = new Circle(15);
        indicator.setFill(Color.LIGHTBLUE);
        indicator.setStroke(Color.BLUE);
        indicator.setLayoutX(400);
        indicator.setLayoutY(270);

        // Label
        Label robotLabel = new Label("DiffDrive Robot");
        robotLabel.setLayoutX(340);
        robotLabel.setLayoutY(310);
        robotLabel.setStyle("-fx-font-weight: bold;");

        canvas.getChildren().addAll(body, leftWheel, rightWheel, indicator, robotLabel);
    }

    private void addComponentToCanvas(String componentName) {
        // Create a draggable component
        VBox component = new VBox(5);
        component.setPadding(new Insets(10));
        component.setStyle("-fx-background-color: #e0e7ff; -fx-border-color: #4f46e5; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label nameLabel = new Label(componentName);
        nameLabel.setStyle("-fx-font-weight: bold;");

        component.getChildren().add(nameLabel);

        // Random position near robot
        double x = 300 + Math.random() * 300;
        double y = 100 + Math.random() * 100;
        component.setLayoutX(x);
        component.setLayoutY(y);

        // Make draggable
        final double[] offset = new double[2];
        component.setOnMousePressed(e -> {
            offset[0] = e.getSceneX() - component.getLayoutX();
            offset[1] = e.getSceneY() - component.getLayoutY();
            updatePropertyPanel(componentName);
        });
        component.setOnMouseDragged(e -> {
            component.setLayoutX(e.getSceneX() - offset[0]);
            component.setLayoutY(e.getSceneY() - offset[1]);
            updateCodePreview();
        });

        canvas.getChildren().add(component);
        updateCodePreview();
    }

    private VBox createPropertyPanel() {
        propertyPanel = new VBox(10);
        propertyPanel.setPadding(new Insets(10));
        propertyPanel.setPrefWidth(250);
        propertyPanel.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-width: 0 0 0 1;");

        Label title = new Label("Properties");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label hint = new Label("Select a component to\nedit its properties");
        hint.setStyle("-fx-text-fill: #888;");

        propertyPanel.getChildren().addAll(title, new Separator(), hint);
        return propertyPanel;
    }

    private void updatePropertyPanel(String componentName) {
        propertyPanel.getChildren().clear();

        Label title = new Label("Properties");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label componentLabel = new Label(componentName);
        componentLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        GridPane props = new GridPane();
        props.setHgap(10);
        props.setVgap(5);

        props.add(new Label("ID:"), 0, 0);
        props.add(new TextField(componentName.toLowerCase().replace(" ", "_")), 1, 0);

        props.add(new Label("Enabled:"), 0, 1);
        props.add(new CheckBox(), 1, 1);

        props.add(new Label("Update Rate:"), 0, 2);
        TextField rateField = new TextField("50");
        rateField.setPrefWidth(60);
        props.add(new HBox(5, rateField, new Label("Hz")), 1, 2);

        propertyPanel.getChildren().addAll(title, new Separator(), componentLabel, props);
    }

    private SplitPane createCodePreview() {
        codePreview = new TextArea();
        codePreview.setEditable(false);
        codePreview.setPrefHeight(150);
        codePreview.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px;");

        updateCodePreview();

        TitledPane preview = new TitledPane("Configuration Preview (YAML)", codePreview);
        preview.setCollapsible(false);

        SplitPane split = new SplitPane(preview);
        split.setOrientation(Orientation.VERTICAL);
        return split;
    }

    private void updateCodePreview() {
        StringBuilder yaml = new StringBuilder();
        yaml.append("# JRobotics Robot Configuration\n");
        yaml.append("# Generated by Visual Editor\n\n");
        yaml.append("robot:\n");
        yaml.append("  type: DifferentialDrive\n");
        yaml.append("  id: my-robot\n\n");
        yaml.append("  kinematics:\n");
        yaml.append("    wheelRadius: 0.05\n");
        yaml.append("    wheelBase: 0.20\n\n");
        yaml.append("  components:\n");

        int componentCount = 0;
        for (javafx.scene.Node node : canvas.getChildren()) {
            if (node instanceof VBox) {
                componentCount++;
                yaml.append("    - type: Sensor\n");
                yaml.append("      updateRate: 50\n");
            }
        }

        if (componentCount == 0) {
            yaml.append("    # Add components from the palette\n");
        }

        codePreview.setText(yaml.toString());
    }
}
