/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asm2_chatting;

/**
 *
 * @author thanhmq
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.logging.Logger;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;

public class ChatServer {

    //JDBC driver and database URL
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

    //URL to in given database
    private static final String DB_URL = "jdbc:mysql://localhost/CHATDATABASE";

    // Database credentials
    private static final String username = "root";
    private static final String password = "12345";
    private static Connection connect = null;
    private static Statement stm = null;
    //Variable for chatting information
    public static String userName, chatMessage, time, saveData;

    private static final int PORT = 9001;

    private static HashSet<String> names = new HashSet<String>();

    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    public static void main(String[] args) throws Exception {
        //Create database
        creatDatabase();
        //Create table
        creatTable();

        System.out.println("The chat server is running.");
        try (ServerSocket listener = new ServerSocket(PORT)) {
            while (true) {
                new Handler(listener.accept()).start();

            }
        } catch (Exception e) {
            Logger.getLogger(e.getMessage());
        }

    }

    private static class Handler extends Thread {

        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String input;

        public Handler(Socket socket) {
            this.socket = socket;

        }

        @Override
        public void run() {

            try {

                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                while (true) {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (names) {
                        if (!names.contains(name)) {
                            names.add(name);
                       
                            break;
                        }
                    }
                }

                out.println("NAMEACCEPTED");
                writers.add(out);

                // Accept messages from this client and broadcast them.
                // Ignore other clients that cannot be broadcasted to.
                while (true) {
                    input = in.readLine();
                    if (input == null) {
                        return;
                    }
                    writers.forEach((writer) -> {
                        writer.println("MESSAGE " + name + ": " + input);

                        //Get message from user
                        chatMessage = input;
                        //get username
                        userName = name;
                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                        time = sdf.format(System.currentTimeMillis());

                    });
                    

                        //Insert chatting infor into database
                        try {
                            saveData = "insert into chatinfor(username,chatmessage,time)"
                                    + "values('" + userName + "', '" + chatMessage + "','" + time + "')";
                            theQuery(saveData);
                        } catch (Exception ex) {
                            Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    
                    
                }

            } catch (IOException e) {
                System.out.println(e);
            } finally {
                // This client is going down!  Remove its name and its print
                // writer from the sets, and close its socket.
                if (name != null) {
                    names.remove(name);
                }
                if (out != null) {
                    writers.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }

        }

    }
    
    // Method to create database to store all chatting information

    public static void creatDatabase() throws Exception {

        try {

            Class.forName(JDBC_DRIVER);
            System.out.println("Connecting to database...");
            connect = DriverManager.getConnection("jdbc:mysql://localhost/", username, password);
            System.out.println("Creating database...");
            stm = connect.createStatement();
            String databaseName = "CREATE DATABASE CHATDATABASE ";

            stm.executeUpdate(databaseName);
            System.out.println("Database created successfully");

        } catch (Exception ex) {
            System.out.println(ex);
        } finally {
            //finally block used to close resoures
            try {
                if (stm != null) {
                    stm.close();
                }
            } catch (SQLException ex1) {
            }
            try {
                if (connect != null) {
                    connect.close();
                }
            } catch (Exception e) {
            }
        }
    }

    //Create table for database in Mysql
    public static void creatTable() throws Exception {
        String sql = "CREATE TABLE CHATINFOR "
                + "(id INTEGER NOT NULL AUTO_INCREMENT, "
                + " username VARCHAR(255) NOT NULL, "
                + " chatmessage TEXT NOT NULL, "
                + "time VARCHAR(255) NOT NULL, "
                + "PRIMARY KEY ( id ))";
        theQuery(sql);
        System.out.println("Table is created successfully");

    }

    //Create query
    public static void theQuery(String query) throws Exception {
        try {
            Class.forName(JDBC_DRIVER);
            System.out.println("Connecting to a selected database...");
            connect = DriverManager.getConnection(DB_URL, username, password);
            stm = connect.createStatement();
            stm.executeUpdate(query);

        } catch (Exception e) {
        } finally {
            //finally block used to close resoures
            try {
                if (stm != null) {
                    stm.close();
                }
            } catch (SQLException ex1) {
            }
            try {
                if (connect != null) {
                    connect.close();
                }
            } catch (Exception e) {
            }
        }

    }
}
