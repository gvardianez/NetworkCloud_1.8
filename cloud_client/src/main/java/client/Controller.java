package client;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

public class Controller implements Initializable {

    private static final int BUFFER_SIZE = 8192; // 8Kb

    private Path baseDir;
    public ListView<String> clientFiles;
    public ListView<String> serverFiles;
    private DataInputStream is;
    private DataOutputStream os;
    private byte[] buffer;

    public Controller() {
        this.buffer = new byte[BUFFER_SIZE];
    }

    private void read() {
        try {
            while (true) {
                String command = is.readUTF();
                if (command.equals("#list#")) {
                    int fileCount = is.readInt();
                    Platform.runLater(() -> serverFiles.getItems().clear());
                    for (int i = 0; i < fileCount; i++) {
                        String name = is.readUTF();
                        Platform.runLater(() -> serverFiles.getItems().add(name));
                    }
                }
                if (command.equals("#reciveFile#")) {
                    String fileName = is.readUTF();
//                    long size = is.readLong();
                    try (FileOutputStream fos = new FileOutputStream(
                            baseDir.resolve(fileName).toFile())) {
                        int count;
                        while ((count = is.read(buffer)) > 0) {
                            fos.write(buffer, 0, count);
                        }
                    }
                    clientFiles.getItems().addAll(getFileNames());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            baseDir = Paths.get(System.getProperty("user.home"));
            clientFiles.getItems().addAll(getFileNames());
            Socket socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
            Thread thread = new Thread(this::read);
            thread.setDaemon(true);
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<FileInfo> getClientFiles() throws IOException {
        return Files.list(baseDir)
                .map(FileInfo::new)
                .collect(Collectors.toList());
    }

    private List<String> getFileNames() throws IOException {
        return Files.list(baseDir)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
    }


    public void pressOnSendToCloudButton(ActionEvent actionEvent) {
        String file = clientFiles.getSelectionModel().getSelectedItem();
        Path filePath = baseDir.resolve(file);
        try {
            os.writeUTF("#upload#");
            os.writeUTF(file);
            os.writeLong(Files.size(filePath));
            os.write(Files.readAllBytes(filePath));
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteFromClient(ActionEvent actionEvent) {
    }

    public void closeConnection(ActionEvent actionEvent) {
    }

    public void pressOnDownloadButton(ActionEvent actionEvent) {
        String file = serverFiles.getSelectionModel().getSelectedItem();
        try {
            os.writeUTF("#download#");
            os.writeUTF(file);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void deleteFromServer(ActionEvent actionEvent) {
    }
}