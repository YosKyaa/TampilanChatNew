import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.List;
import javax.swing.border.EmptyBorder;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public class ClientGUI extends JFrame {
    private JPanel chatPanel;
    private JTextField inputField;
    private JButton sendButton;
    private PrintWriter out;
    private String name;
    private BufferedReader in;

    public ClientGUI() {
        name = askUser();

        setTitle("Chat - " + name);
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel utama chat
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(chatPanel);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        // Panel input dan tombol
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();

        ImageIcon icon = new ImageIcon("send.png");
        Image scaled = icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        sendButton = new JButton(new ImageIcon(scaled));
        sendButton.setPreferredSize(new Dimension(50, 40));
        sendButton.setBorderPainted(false);
        sendButton.setContentAreaFilled(false);
        sendButton.setFocusPainted(false);

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        setVisible(true);
        connectToServer();
    }

    private void sendMessage() {
        String msg = inputField.getText().trim();
        if (!msg.isEmpty()) {
            addMessage("Me: " + msg, true);
            out.println(msg);
            inputField.setText("");
        }
    }
    private void addMessage(String msg, boolean isMe) {
        JPanel messageBubble = new JPanel();
        messageBubble.setLayout(new BorderLayout());
        messageBubble.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));

        // Add timestamp
        String timestamp = java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        JLabel label = new JLabel("<html>" + msg + "<br><small>" + timestamp + "</small></html>");

        if (isMe) {
            messageBubble.setBackground(new Color(204, 255, 204)); // Hijau muda
            label.setHorizontalAlignment(SwingConstants.RIGHT);
        } else {
            messageBubble.setBackground(Color.WHITE);
            label.setHorizontalAlignment(SwingConstants.LEFT);
        }

        messageBubble.add(label, BorderLayout.CENTER);

        // Adjust bubble size to fit text
        messageBubble.setMaximumSize(new Dimension(chatPanel.getWidth() - 20, label.getPreferredSize().height + 20));
        messageBubble.setAlignmentX(isMe ? Component.LEFT_ALIGNMENT : Component.RIGHT_ALIGNMENT);

        // Add spacing between messages
        chatPanel.add(Box.createVerticalStrut(10));
        chatPanel.add(messageBubble);
        chatPanel.revalidate();
        chatPanel.repaint();
    }

    private String askUser() {
        String[] options = { "Login", "Register" };
        int choice = JOptionPane.showOptionDialog(
                null, "Welcome!", "Choose Action",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);

        if (choice == 1) { // Register
            String newName;
            do {
                newName = JOptionPane.showInputDialog("Enter new username:");
            } while (newName == null || newName.trim().isEmpty() || UserManager.userExists(newName));
            UserManager.addUser(newName);
            return newName;
        } else { // Login
            List<String> users = UserManager.getUsers();
            if (users.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No users registered yet. Register first.");
                System.exit(0);
            }
            Object selected = JOptionPane.showInputDialog(null, "Select User:", "Login",
                    JOptionPane.PLAIN_MESSAGE, null, users.toArray(), users.get(0));
            return (String) selected;
        }
    }

    public void connectToServer() {
        try {
            Socket socket = new Socket("192.168.198.64", 12345); // Ganti IP untuk koneksi antar laptop
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(name); // kirim nama ke server

            new Thread(() -> {
                String line;
                try {
                    while ((line = in.readLine()) != null) {
                        String sender = line.split(":", 2)[0];
                        if (!sender.equals(name)) {
                            addMessage(line, false); // tampilkan jika bukan dari diri sendiri
                        }
                    }
                } catch (IOException e) {
                    addMessage("Connection lost.", false);
                }
            }).start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Unable to connect to server.");
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }
}
