package gui;

public interface AgentClientListener {
    void onException(String ex);
    void onMessage(String msg);
}
