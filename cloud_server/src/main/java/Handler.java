import java.io.*;
import java.net.Socket;

public class Handler implements Runnable {

    private FileInputStream fis;
    private FileOutputStream fos;
    private InputStream is;
    private OutputStream os;
    private DataOutputStream dos;
    private File path;

    public Handler(Socket socket) throws IOException {
        is = socket.getInputStream();
        dos = new DataOutputStream(socket.getOutputStream());
        path = new File("D:\\");
        sendList(path);
        System.out.println("Client accepted...");
    }

    @Override
    public void run() {
        try {
            File file = new File("D:\\new");
            file.createNewFile();
            byte[] bytes = new byte[8192];
            int count;
            while ((count = is.read(bytes)) > 0) {
                fos = new FileOutputStream(file);
                fos.write(bytes, 0, count);
                sendList(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendList(File file) {
        String[] files = file.list();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < files.length; i++) {
            sb.append(files[i]).append("/");
        }
        try {
            dos.writeUTF(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}