import java.io.*;
import java.util.*;

public class UserManager {
    private static final String FILE_NAME = "users.txt";

    public static List<String> getUsers() {
        List<String> users = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = br.readLine()) != null) {
                users.add(line.trim());
            }
        } catch (IOException e) {
            // Ignore
        }
        return users;
    }

    public static void addUser(String name) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            bw.write(name);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean userExists(String name) {
        return getUsers().contains(name);
    }
}
