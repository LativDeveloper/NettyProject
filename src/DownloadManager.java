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

    private Client source; //file from here
    private Client target; //file to here
    private String path;
    private String filename;

    DownloadManager(Client source, Client target, String path) {
        this.source = source;
        this.target = target;
        this.path = path;

        path = path.replace('\\', '/');
        String[] params = path.split("/");
        this.filename = params[params.length - 1];
    }

    @Override
    public void run() {
        try {
            int port = findFreePort();
            serverSocket = new ServerSocket(port);
            source.sendStartUploadFile(path, port, target.getName());

            sourceSocket = serverSocket.accept(); //wait source
            inputStream = sourceSocket.getInputStream();
            fileOutputStream = new FileOutputStream(Config.DOWNLOAD_PATH + filename);
            byte[] bytes = new byte[8*1024];
            int len;
            while ((len = inputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, len); //receive file from source
            }

            target.sendStartDownloadFile(filename, port, source.getName());

            targetSocket = serverSocket.accept(); //wait target
            outputStream = targetSocket.getOutputStream();
            fileInputStream = new FileInputStream(Config.DOWNLOAD_PATH + filename);
            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len); //transfer file to target
            }

            target.sendFinishLoadFile(filename, "success", source.getName());
            source.sendFinishLoadFile(filename, "success", target.getName());
            dispose();
        } catch (IOException e) {
            e.printStackTrace();
            target.sendFinishLoadFile(filename, "error", source.getName());
            source.sendFinishLoadFile(filename, "success", target.getName());
            dispose();
        }
    }

    private void dispose() {
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
