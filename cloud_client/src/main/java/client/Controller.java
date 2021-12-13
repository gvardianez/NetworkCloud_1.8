package client;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

public class Controller implements Initializable {

    public ListView<String> serverFilesList;
    private FileInputStream fis;
    private FileOutputStream fos;
    private InputStream is;
    private OutputStream os;
    private DataInputStream dis;
    public TextField input;

    private void read() {
        try {
            while (true) {
                String message = dis.readUTF();
                Platform.runLater(() -> processMessage(message));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processMessage(String message) {
        System.out.println("proc");
        ObservableList<String> list = FXCollections.observableArrayList(message.split("/"));
        serverFilesList.setItems(list);
        MultipleSelectionModel<String> nameModel = serverFilesList.getSelectionModel();
        nameModel.setSelectionMode(SelectionMode.MULTIPLE);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Socket socket = new Socket("localhost", 8189);
            dis = new DataInputStream(socket.getInputStream());
            os = socket.getOutputStream();
            Thread thread = new Thread(this::read);
            thread.setDaemon(true);
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pressOnSendToCloudButton(ActionEvent actionEvent) {
        System.out.println("send");
        File file = new File(input.getText());
        byte[] bytes = new byte[8192];
        try {
            fis = new FileInputStream(file);
            int count;
            while ((count = fis.read(bytes)) > 0) {
                os.write(bytes, 0, count);
            }
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteFromClient(ActionEvent actionEvent) {
    }

    public void closeConnection(ActionEvent actionEvent) {
    }

    public void pressOnDownloadButton(ActionEvent actionEvent) {
    }

    public void deleteFromServer(ActionEvent actionEvent) {
    }
}