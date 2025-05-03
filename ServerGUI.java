import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.*;
import java.util.*; // For HashMap, HashSet, etc.
import java.util.List; // Explicitly import List to resolve ambiguity

public class ServerGUI extends JFrame {
    private JTextArea chatLog;
    private DefaultListModel<String> statusModel;
    private JList<String> statusList;
    private ServerSocket serverSocket;

    public static Map<String, ClientHandler> clients = new HashMap<>();
    public static Set<String> blockedIPs = new HashSet<>();

    public ServerGUI() {
        setTitle("Chat Server");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        chatLog = new JTextArea();
        chatLog.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatLog);

        statusModel = new DefaultListModel<>();
        statusList = new JList<>(statusModel);
        JScrollPane statusScrollPane = new JScrollPane(statusList);

        JButton disableBtn = new JButton("Disable");
        JButton enableBtn = new JButton("Enable");
        disableBtn.addActionListener(e -> disableSelectedClients());
        enableBtn.addActionListener(e -> enableSelectedClients());
        enableBtn.addActionListener((ActionEvent e) -> enableSelectedClients());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(disableBtn);
        buttonPanel.add(enableBtn);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(statusScrollPane, BorderLayout.CENTER);
        rightPanel.add(buttonPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, rightPanel);
        splitPane.setDividerLocation(500);
        add(splitPane);

        setVisible(true);
        startServer();
    }

    public void startServer() {
        try {
            serverSocket = new ServerSocket(12345);
            appendChat("Server started on port 12345...");

            new Thread(() -> {
                while (true) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        ClientHandler handler = new ClientHandler(clientSocket, this);
                        handler.start();
                    } catch (IOException e) {
                        appendChat("Error accepting connection.");
                    }
                }
            }).start();
        } catch (IOException e) {
            appendChat("Server could not start.");
        }
    }

    public void appendChat(String message) {
        SwingUtilities.invokeLater(() -> chatLog.append(message + "\n"));
    }

    public void updateClientStatus() {
        SwingUtilities.invokeLater(() -> {
            statusModel.clear();
            statusModel.addElement("Client Status:");
            for (String name : UserManager.getUsers()) {
                ClientHandler client = clients.get(name);
                boolean isOnline = client != null && client.isConnected();
                String status = isOnline ? "Online" : "Offline";
                String ip = client != null ? client.getIP() : "-";
                String blocked = blockedIPs.contains(ip) ? " (Blocked)" : "";
                String coloredStatus = String.format("<html>%s : <font color='%s'>%s</font>%s</html>",
                        name, isOnline ? "green" : "red", status, blocked);
                statusModel.addElement(coloredStatus);
            }
        });

        statusList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setText(value.toString());
                return label;
            }
        });
    }

    private void disableSelectedClients() {
        List<String> selected = statusList.getSelectedValuesList();
        for (String entry : selected) {
            for (Map.Entry<String, ClientHandler> mapEntry : clients.entrySet()) {
                ClientHandler handler = mapEntry.getValue();
                if (entry.contains(mapEntry.getKey()) && handler != null) {
                    blockedIPs.add(handler.getIP());
                    appendChat("Blocked " + mapEntry.getKey() + " [" + handler.getIP() + "]");
                }
            }
        }
        updateClientStatus();
    }

    private void enableSelectedClients() {
        List<String> selected = statusList.getSelectedValuesList();
        for (String entry : selected) {
            for (Map.Entry<String, ClientHandler> mapEntry : clients.entrySet()) {
                ClientHandler handler = mapEntry.getValue();
                if (entry.contains(mapEntry.getKey()) && handler != null) {
                    blockedIPs.remove(handler.getIP());
                    appendChat("Unblocked " + mapEntry.getKey() + " [" + handler.getIP() + "]");
                }
            }
        }
        updateClientStatus();
    }

    public static boolean isBlocked(String ip) {
        return blockedIPs.contains(ip);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ServerGUI::new);
    }
}
