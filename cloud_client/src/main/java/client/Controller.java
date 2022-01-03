package client;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import server.model.*;


public class Controller implements Initializable {

    public ListView<String> clientFiles;
    public ListView<String> serverFiles;
    public TextField loginField;
    public PasswordField passwordField;
    public TextField loginFieldReg;
    public PasswordField passwordFieldReg;
    public TextField nickFieldReg;
    public VBox loginPanel;
    public VBox registrationPanel;
    public HBox cloudPanel;
    private Path baseDir;
    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;

    private void read() {
        try {
            while (true) {
                AbstractMessage msg = (AbstractMessage) is.readObject();
                switch (msg.getMessageType()) {
                    case ERROR:
                        ErrorMessage errorMessage = (ErrorMessage) msg;
                        Platform.runLater(() -> showError(errorMessage.getErrorMessage()));
                        break;
                    case AUTH_OK:
                        loginPanel.setVisible(false);
                        cloudPanel.setVisible(true);
                        break;
                    case FILE:
                        FileMessage fileMessage = (FileMessage) msg;
                        Files.write(
                                baseDir.resolve(fileMessage.getFileName()),
                                fileMessage.getBytes()
                        );
                        Platform.runLater(() -> fillClientView(getFileNames()));
                        break;
                    case FILES_LIST:
                        FilesList files = (FilesList) msg;
                        Platform.runLater(() -> fillServerView(files.getFiles()));
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR");
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    private void fillServerView(List<String> list) {
        serverFiles.getItems().clear();
        serverFiles.getItems().addAll(list);
    }

    private void fillClientView(List<String> list) {
        clientFiles.getItems().clear();
        clientFiles.getItems().addAll(list);
    }

    private List<FileInfo> getClientFiles() throws IOException {
        return Files.list(baseDir)
                .map(FileInfo::new)
                .collect(Collectors.toList());
    }

    private List<String> getFileNames() {
        try {
            return Files.list(baseDir)
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            baseDir = Paths.get(System.getProperty("user.home"));
            clientFiles.getItems().addAll(getFileNames());
            clientFiles.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    String file = clientFiles.getSelectionModel().getSelectedItem();
                    Path path = baseDir.resolve(file);
                    if (Files.isDirectory(path)) {
                        baseDir = path;
                        fillClientView(getFileNames());
                    }
                }
            });
            Socket socket = new Socket("localhost", 8189);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());
            Thread thread = new Thread(this::read);
            thread.setDaemon(true);
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void upload(ActionEvent actionEvent) throws IOException {
        String file = clientFiles.getSelectionModel().getSelectedItem();
        Path filePath = baseDir.resolve(file);
        os.writeObject(new FileMessage(filePath));
    }

    public void downLoad(ActionEvent actionEvent) throws IOException {
        String file = serverFiles.getSelectionModel().getSelectedItem();
        os.writeObject(new FileRequest(file));
    }

    public void registrationAuth(ActionEvent actionEvent) throws IOException {
        os.writeObject(new RegistrationAuth(loginFieldReg.getText(), nickFieldReg.getText(), passwordFieldReg.getText()));
    }

    public void sendAuth(ActionEvent actionEvent) throws IOException {
        os.writeObject(new AuthMessage(loginField.getText(), passwordField.getText()));
    }

    public void registration(ActionEvent actionEvent) throws IOException {
        loginPanel.setVisible(false);
        registrationPanel.setVisible(true);
    }

    public void goBackOnLoginPanel(ActionEvent actionEvent) {
        loginPanel.setVisible(true);
        registrationPanel.setVisible(false);
    }

    public void closeConnection(ActionEvent actionEvent) {
    }

    public void deleteOnClient(ActionEvent actionEvent) {
        String file = clientFiles.getSelectionModel().getSelectedItem();
        Path filePath = baseDir.resolve(file);
        Platform.runLater(() -> fillClientView(getFileNames()));
        try {
            Files.delete(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteFromServer(ActionEvent actionEvent) {
    }

    public void back(ActionEvent actionEvent) {
        if (baseDir.toString().length() != 3) baseDir = baseDir.getParent();
        Platform.runLater(() -> fillClientView(getFileNames()));
    }

    public void refresh(ActionEvent actionEvent) {
        Platform.runLater(() -> fillClientView(getFileNames()));
    }
}