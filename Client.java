import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
    //Creates the sockets.
    public static Socket clientSocket = null;

    //Creates all the readers, writers, and streams.
    public static PrintWriter pw;
    public static InputStreamReader in;
    public static BufferedReader bf;

    public static void main(String [] args) throws IOException {
        //Declares local variables.
        String userInput = "";
        Scanner inputScanner = new Scanner(System.in);

        //Provides instructions on how to use the client.
        System.out.println("The following commands are available for use:");
        System.out.println("connect");
        System.out.println("list");
        System.out.println("retr");
        System.out.println("stor");
        System.out.println("quit");


        do {
            //Reads user input.
            System.out.print("\nEnter one of the above commands: ");
            userInput = inputScanner.nextLine();
            userInput = userInput.toLowerCase();

            switch (userInput) {
                //Connects to the server.
                case "connect":
                    System.out.print("Enter hostname or IP address: ");
                    userInput = inputScanner.nextLine();
                    String hostname = userInput;

                    System.out.print("Enter port: ");
                    userInput = inputScanner.nextLine();
                    int port = Integer.parseInt(userInput);

                    connect(hostname, port);

                    break;
                case "list":
                    if (clientSocket == null) {
                        System.out.println("You must first connect to a server.");
                    } else {
                        list();
                    }

                    break;
                case "retr":
                    if (clientSocket == null) {
                        System.out.println("You must first connect to a server.");
                    } else {
                        System.out.print("Enter filename with file extension: ");
                        userInput = inputScanner.nextLine();

                        retr(userInput);
                    }

                    break;
                case "stor":
                    if (clientSocket == null) {
                        System.out.println("You must first connect to a server.");
                    } else {
                        System.out.print("Enter filename with file extension: ");
                        userInput = inputScanner.nextLine();

                        stor(userInput);
                    }

                    break;
                case "quit":
                    if (clientSocket == null) {
                        System.out.println("You must first connect to a server.");
                    } else {
                        quit();
                    }

                    break;
                default:
                    System.out.println("Invalid Command.\n");

                    break;
            }
        } while (!(userInput.toLowerCase().equals("quit")));
    }

    private static void connect(String host, int port) throws IOException {
        //Creates the client socket.
        System.out.println("Connecting to " + host + " at port " + port);
        clientSocket = new Socket(host, port);

        //Sets up the readers and writers.
        pw = new PrintWriter(clientSocket.getOutputStream());
    }

    private static void list() throws IOException {
        System.out.println("Listing available files:");
        pw.println("list");
        pw.flush();

        //Connects to the server's client socket.
        ServerSocket clientServerSocket = new ServerSocket(1);
        Socket serverAccept = clientServerSocket.accept();

        in = new InputStreamReader(serverAccept.getInputStream());
        bf = new BufferedReader(in);

        String listInput = "";
        do {
            listInput = bf.readLine();
            System.out.println(listInput);
        } while (!(listInput.equals("End of list.")));

        bf.close();
        in.close();
        serverAccept.close();
        clientServerSocket.close();
    }

    private static void retr(String filename) throws IOException {
        System.out.println("Attempting to retrieve the file " + filename);
        pw.println("retr");
        pw.flush();
        pw.println(filename);
        pw.flush();

        byte [] receiveFileBytes = new byte[6000000];

        //Connects to the server's client socket.
        ServerSocket clientServerSocket = new ServerSocket(1);
        Socket serverAccept = clientServerSocket.accept();

        InputStream is = serverAccept.getInputStream();
        FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir") + "/" + filename);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        int bytes = 0;

        while ((bytes = is.read(receiveFileBytes, 0, receiveFileBytes.length)) > -1) {
            bos.write(receiveFileBytes, 0, bytes);
            bos.flush();
        }

        is.close();
        bos.close();
        fos.close();
        serverAccept.close();
        clientServerSocket.close();

        System.out.println("File received.");
    }

    private  static void stor(String filename) throws IOException {
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

        boolean present = false;
        for (int i = 0; i < fileList.size(); i++) {
            if (fileList.get(i).equals(filename)) {
                present = true;
                break;
            }
        }

        if (present == false) {
            System.out.println("Could not find file.");
        } else {
            System.out.println("Attempting to store the file " + filename);
            pw.println("stor");
            pw.flush();
            pw.println(filename);
            pw.flush();

            Socket fileSendClient = new Socket("localhost", 1);

            File sendFile = new File(System.getProperty("user.dir") + "/" + filename);
            byte [] sendFileBytes = new byte[(int)sendFile.length()];

            FileInputStream fis = new FileInputStream(sendFile);
            BufferedInputStream bis = new BufferedInputStream(fis);

            bis.read(sendFileBytes, 0, sendFileBytes.length);

            OutputStream os = fileSendClient.getOutputStream();
            os.write(sendFileBytes, 0, sendFileBytes.length);
            os.flush();

            os.close();
            bis.close();
            fis.close();

            fileSendClient.close();
        }
    }

    private static void quit() throws IOException {
        //Closes the socket.
        System.out.println("Closing the sockets and exiting client.");
        pw.println("quit");
        pw.flush();
        clientSocket.close();
    }
}
