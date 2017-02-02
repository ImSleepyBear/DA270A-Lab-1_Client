/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package da270a.lab.pkg1.client;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author Mohini
 */
public class DA270ALab1Client {

    private static final int PORT = 8000;
    private static final String SERVER = "192.168.1.4";
    private Scanner scan;
    private String commandToSend = " ";
    private String commandToRetrieve = " ";
    FileOutputStream fin;
    byte[] myByte = new byte[1024 * 16];
    int byteRead;

    public static void main(String[] args) {

        String server = SERVER;
        int port = PORT;

        if (args.length >= 1) {
            server = args[0];
        } else if (args.length >= 2) {
            port = Integer.parseInt(args[1]);
        }

        new DA270ALab1Client(server, port);
    }

    public DA270ALab1Client(String server, int port) {

        DataInputStream inputFromServer;
        DataOutputStream outputToServer;
        BufferedReader br;

        try {
            Socket socket = new Socket(server, port);

            inputFromServer = new DataInputStream(socket.getInputStream());
            outputToServer = new DataOutputStream(socket.getOutputStream());
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            
            scan = new Scanner(System.in);
            System.out.println("Enter command: (pwd, printfiles, download)");

            while (scan.hasNextLine()) {
                commandToSend = scan.nextLine();

                outputToServer.writeBytes(commandToSend);
                outputToServer.flush();

                commandToRetrieve = inputFromServer.readUTF();

                System.out.println("Message from server:");
                System.out.println(commandToRetrieve);
                System.out.println("Enter command: (pwd, printfiles, download)");

//                fin = new FileOutputStream("s.txt");
//                BufferedOutputStream bos = new BufferedOutputStream(fin);
//
//                byteRead = inputFromServer.read(myByte, 0, myByte.length);
//
//                bos.write(myByte);
//                byteRead = inputFromServer.read(myByte);
//
//                System.out.println("file recieved");
            }
            

//            FileInputStream fin = new FileInputStream("s.pdf");
//            System.out.println("file recieved");
            fin.close();
            inputFromServer.close();
            outputToServer.close();
            socket.close();

        } catch (Exception e) {
            System.err.println(e);
        }
    }

}
