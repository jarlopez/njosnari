package gui.controllers;

import agent.*;
import agent.tasks.IAgentTask;
import agent.tasks.TaskFactory;
import gui.AgentClientListener;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.ResourceBundle;

public class AgentController implements Initializable, AgentClientListener {

    @FXML public ChoiceBox agentTypeChoiceBox;
    @FXML public ChoiceBox agentOptionChoiceBox;
    @FXML public TextField portTextField;
    @FXML public TextArea logArea;
    @FXML public Button startButton;

    private BaseAgent agent;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        ObservableList agentTypes = FXCollections.observableArrayList(Arrays.asList(AgentType.values()));
        ObservableList taskTypes = FXCollections.observableArrayList(Arrays.asList(IAgentTask.Type.values()));
        agentTypeChoiceBox.setItems(agentTypes);
        agentTypeChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(AgentType.TaskedAgent)) {
                agentOptionChoiceBox.setDisable(false);
                agentOptionChoiceBox.setItems(taskTypes);
                agentOptionChoiceBox.setValue(IAgentTask.Type.GatherFootprintTask);
            } else {
                agentOptionChoiceBox.setDisable(true);
            }
        });
        agentTypeChoiceBox.setValue(AgentType.TaskedAgent);
    }

    public void createAgent(ActionEvent actionEvent) {
        AgentType agentType = (AgentType) agentTypeChoiceBox.getValue();
        IAgentTask.Type taskType = (IAgentTask.Type) agentOptionChoiceBox.getValue();
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
            int port = Integer.parseInt(portTextField.getText());
            agent = AgentFactory.createAgent(agentType, address, port);
            appendToLog("Created " + agentType + " agent.");
            if (agentType.equals(AgentType.TaskedAgent)) {
                IAgentTask task = TaskFactory.createTask(taskType);
                ((TaskedAgent) agent).setTask(task);
                appendToLog("Assigned " + taskType + " to agent.");
            }
            startButton.setDisable(false);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            appendToLog("UnknownHostException when accessing localhost address.");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            appendToLog("Could not instantiate the agent or task.");
        }
    }

    public void appendToLog(String msg) {
        logArea.appendText(msg + "\n");
    }
    public void clearLog() {
        logArea.clear();
    }

    public void startClientServices(ActionEvent actionEvent) {
        int port = Integer.parseInt(portTextField.getText());
        AgentClient client = new AgentClient(port);
        client.setListener(this);
        client.setAgent(agent);
        try {
            client.run();
        } catch (IOException e) {
            e.printStackTrace();
            onException(e.getMessage());

        }
    }

    @Override
    public void onException(String ex) {
        Platform.runLater(() -> {
            appendToLog("EXCEPTION:\n\t" + ex);
        });
    }

    @Override
    public void onMessage(String msg) {
        Platform.runLater(() -> {
            appendToLog(msg);
        });
    }
}
