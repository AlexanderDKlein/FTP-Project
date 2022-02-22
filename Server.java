import java.lang.annotation.Target;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Server extends Thread {
    private static Socket clientSocket;
    private String serverMessage = null;

    public Server(Socket clientSocket, String serverMessage) {
        this.clientSocket = clientSocket;
        this.serverMessage = serverMessage;
    }

    public static void main(String [] args) throws IOException, InterruptedException {
        //Creates the server socket.
        ServerSocket serverSocket = new ServerSocket(4676);
        //Accepts the client socket.

        while (true) {
            clientSocket = serverSocket.accept();
            new Thread(
                    new Server(clientSocket, "Multithreaded Server")
            ).start();
        }
    }

    @Override
    public void run() {
        Socket serverClientSocket;

        //Creates all the readers, writers, and streams.
        InputStreamReader in = null;
        try {
            in = new InputStreamReader(clientSocket.getInputStream());

            BufferedReader bf = new BufferedReader(in);
            OutputStream os;
            FileInputStream fis;
            BufferedInputStream bis;

            String clientMessage;

            do {
                //Creates the list of files.
                ArrayList<String> fileList = new ArrayList<>();

                //Populates the list of files.
                File dir = new File(System.getProperty("user.dir"));
                File[] directoryListing = dir.listFiles();
                if (directoryListing != null) {
                    for (File child : directoryListing) {
                        fileList.add(child.getName());
                    }
                }

                clientMessage = "";

                //Reads client input.
                clientMessage = bf.readLine();

                //Determines which command was sent.
                switch (clientMessage) {
                    //Lists items in the arraylist.
                    case "list":
                        //Creates the server's client socket.
                        serverClientSocket = new Socket("localhost", 1);
                        System.out.println("Sending list contents to client.");

                        PrintWriter pw = new PrintWriter(serverClientSocket.getOutputStream());

                        for (int i = 0; i < fileList.size(); i++) {
                            pw.println(fileList.get(i));
                            pw.flush();
                        }
                        pw.println("");
                        pw.flush();
                        pw.println("End of list.");
                        pw.flush();

                        serverClientSocket.close();

                        break;
                    //Sends file to client.
                    case "retr":
                        System.out.println("Sending file to client.");
                        clientMessage = bf.readLine();
                        boolean present = false;
                        for (int i = 0; i < fileList.size(); i++) {
                            if (fileList.get(i).equals(clientMessage)) {
                                present = true;
                                break;
                            }
                        }
                        if (present == false) {
                            System.out.println("Could not find file.");
                        } else {
                            System.out.println("Sending file " + clientMessage);

                            //Creates the server's client socket.
                            serverClientSocket = new Socket("localhost", 1);

                            File sendFile = new File(System.getProperty("user.dir") + "/" + clientMessage);
                            byte[] sendFileBytes = new byte[(int) sendFile.length()];

                            fis = new FileInputStream(sendFile);
                            bis = new BufferedInputStream(fis);

                            bis.read(sendFileBytes, 0, sendFileBytes.length);

                            os = serverClientSocket.getOutputStream();
                            os.write(sendFileBytes, 0, sendFileBytes.length);
                            os.flush();

                            os.close();
                            bis.close();
                            fis.close();

                            serverClientSocket.close();
                        }
                        break;
                    //Receives file from client.
                    case "stor":
                        System.out.println("Storing client file to server.");
                        clientMessage = bf.readLine();
                        System.out.println("Retrieving file " + clientMessage);

                        //Creates the server's client socket.
                        ServerSocket fileReceiveServer = new ServerSocket(1);
                        Socket fileAccept = fileReceiveServer.accept();

                        byte[] receiveFileBytes = new byte[6000000];

                        InputStream is = fileAccept.getInputStream();
                        FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir") + "/" + clientMessage);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                        int bytes = 0;

                        while ((bytes = is.read(receiveFileBytes, 0, receiveFileBytes.length)) > -1) {
                            bos.write(receiveFileBytes, 0, bytes);
                            bos.flush();
                        }

                        System.out.println("File received.");

                        is.close();
                        bos.close();
                        fos.close();
                        fileAccept.close();
                        fileReceiveServer.close();

                        break;
                    case "quit":
                        System.out.println("Client sent quit command.");
                        break;
                    default:
                        System.out.println("Message not recognized.");
                        break;
                }

            } while (!(clientMessage.equals("quit")));

            //Closes the sockets
            System.out.println("Server shutting down.");
            clientSocket.close();
            currentThread().stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
