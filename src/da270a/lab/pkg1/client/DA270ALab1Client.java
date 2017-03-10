/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package da270a.lab.pkg1.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 *
 * @author Mohini
 */
public class DA270ALab1Client {

    private static final int PORT = 8000;
    private static final String SERVER = "localhost"; 
    
    public static void main(String[] args) throws InterruptedException {

        String server = SERVER;
        int port = PORT;

        if (args.length >= 1) {
            server = args[0];
        } else if (args.length >= 2) {
            port = Integer.parseInt(args[1]);
        }

        new DA270ALab1Client(server, port);
    }

    public DA270ALab1Client(String server, int port) throws InterruptedException {
        try {
            boolean reconnect = false;
            boolean connected = false;

            // create data input/output streams
            //we will asume at the first that these values are working server and port
            String prevWorkingServer = server;
            int prevWrokingPort = port;

            CommandReader R = new CommandReader();
            R.start();

//            CONNECT_TO_SERVER:
            do {
                System.out.println("Connecting to: " + server + ":" + port);

                reconnect = false;
                Socket socket = null;

                try {
                    socket = new Socket(server, port);
                    socket.setSoTimeout(10000); //10 sec max for read line wait time

                    //at this point the server and port seems working so we keep them for reconnect
                    prevWorkingServer = server;
                    prevWrokingPort = port;

                    connected = socket.isConnected();

                } catch (ConnectException | UnknownHostException e) {
                    System.out.println(e);
                    if (prevWorkingServer != null) {
                        server = prevWorkingServer;
                    }
                    if (prevWrokingPort != 0) {
                        port = prevWrokingPort;
                    }
                    connected = false;
                }

                // create Scanner object to read command
                Scanner sc = new Scanner(System.in);
                InputStream is = null;
                InputStreamReader isr;
                //STOP BR BufferedReader br = null;
                DataInputStream dis = null;

                OutputStream os;
                DataOutputStream out = null;
                PrintStream ps = null;

                if (connected) {
                    is = socket.getInputStream();
                    isr = new InputStreamReader(is);
                    //STOP BR br = new BufferedReader(isr);
                    dis = new DataInputStream(is);

                    os = socket.getOutputStream();
                    out = new DataOutputStream(os);
                    ps = new PrintStream(out);
                }

//                COMMANDS:
                while (true) {
                    if (connected) {
                        System.out.print(server + ":" + port + ">");
                    } else {
                        System.out.print(">");
                    }

                    String cmd;
                    long lastResponse = System.currentTimeMillis(); // it starts to count from the moment we are waiting command
                    while (!R.hasCommand) {
                        Thread.sleep(100);
                    }
                    cmd = R.getCommand();

                    //check internal command first
                    String res;
                    if (cmd.trim().isEmpty()) { //read from server if there is any msg
                        if (connected) {

                            do {
                                res = dis.readLine();
                                lastResponse = System.currentTimeMillis();
                                System.out.println(res);
                            } while (!res.isEmpty());
                        }
                    } else if (cmd.startsWith("exit")) {
                        reconnect = false;
                        break;
                    } else if (cmd.startsWith("connect")) {
                        String[] arg = cmd.substring(7).trim().split(" ");
                        if (arg.length > 0) {
                            if (!arg[0].isEmpty()) {
                                server = arg[0];
                            }
                        }
                        if (arg.length > 1) {
                            try {
                                port = Integer.parseInt(arg[1]);
                            } catch (NumberFormatException e) {
                                System.out.println("Port number is invalid, default or old port number will be used instead");
                                port = prevWrokingPort;
                            } catch (Exception e) {
                                System.out.println("test");
                            }
                        }
                        reconnect = true;
                        break;// COMMANDS;
                    } else if (connected) {
                        // send command to server
                        ps.println(cmd);
                        ps.flush();
                        out.flush();
                        Thread.sleep(50); //wait unitll server send reply
//                        GET:
                        if (cmd.startsWith("get ")) {

                            res = dis.readLine();
                            lastResponse = System.currentTimeMillis();
                            System.out.println(res);
//                            COPYING:
                            if (res.startsWith("COPYING ")) {
                                long fileSize = Long.parseLong(res.substring(8));

                                String newFileName = "." + File.separator + cmd.substring(4);
                                String suffex = "";
                                int i = 1;
                                while (new File(newFileName + suffex).exists()) {
                                    suffex = String.valueOf(i);
                                    i++;
                                }
                                newFileName = newFileName + suffex;
                                File f = new File(newFileName);

                                FileOutputStream fos = new FileOutputStream(f);

                                long timeOut = System.currentTimeMillis();

                                byte[] b;
                                final int defBufferSize = 8192;
                                if (fileSize < defBufferSize) {
                                    b = new byte[(int) fileSize];
                                } else {
                                    b = new byte[defBufferSize]; //max of buffer
                                }

                                int r;
                                long remainFileSize = fileSize;
                                int x = b.length;
                              // while (remainFileSize > 0 && dis.available() > 0 && (r = dis.read(b, 0, x)) > 0) {
                                
                                while (remainFileSize > 0 && (r = dis.read(b, 0, x)) > 0) {
                                    lastResponse = System.currentTimeMillis();
                                    fos.write(b, 0, r);
                                    remainFileSize -= r;
                                    x = remainFileSize < b.length ? (int) remainFileSize : b.length;
                                    Thread.sleep(10);
                                }

                                fos.close();
                                b = null;

                            } else { // if the response is not equal to COPYING then there must a String reply
                                do {
                                    res = dis.readLine();
                                    lastResponse = System.currentTimeMillis();
                                    System.out.println(res);
                                } while (!res.isEmpty());
                            } 
                        } else { //if not get

                            do {
                                res = dis.readLine();
                                lastResponse = System.currentTimeMillis();
                                System.out.println(res);
                            } while (!res.isEmpty());
                        }
                    } else { //not internal command and it's not connacted
                        System.out.println("Command not sent, you are not connected!!");
                    }

                }

                System.out.println("Client is closing...");
                if (connected) {
                    ps.close();
                    dis.close();
                    out.close();
                    socket.close();
                    connected = false;
                }
            } while (reconnect);
            System.out.println("Client Shutdown.");

        } catch (IOException e) {
            System.err.println(e);
        }
        
    }

}
