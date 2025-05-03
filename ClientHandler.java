import java.io.*;
import java.net.*;

public class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String name;
    private ServerGUI serverGUI;

    public ClientHandler(Socket socket, ServerGUI serverGUI) {
        this.socket = socket;
        this.serverGUI = serverGUI;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Terima nama client
            name = in.readLine();

            // Tambah ke daftar client
            ServerGUI.clients.put(name, this);
            serverGUI.appendChat(name + " joined.");
            serverGUI.updateClientStatus();
            broadcast(name + " joined the chat.");

            // Loop baca pesan
            String message;
            while ((message = in.readLine()) != null) {
                String clientIP = getIP();
                if (ServerGUI.isBlocked(clientIP)) {
                    out.println("[Blocked] You are not allowed to send messages.");
                    continue;
                }
                serverGUI.appendChat(name + ": " + message);
                broadcast(name + ": " + message);
            }
        } catch (IOException e) {
            serverGUI.appendChat(name + " disconnected.");
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}

            // Remove dari daftar client
            ServerGUI.clients.remove(name);
            serverGUI.updateClientStatus();
            serverGUI.appendChat(name + " is now offline.");
            broadcast(name + " left the chat.");
        }
    }

    // Kirim pesan ke semua client
    private void broadcast(String message) {
        for (ClientHandler client : ServerGUI.clients.values()) {
            client.out.println(message);
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public String getIP() {
        return socket.getInetAddress().getHostAddress();
    }
}
