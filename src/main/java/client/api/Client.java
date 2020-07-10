package client.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import server.server.Response;
import server.server.Server;

import java.io.*;
import java.net.Socket;

public class Client {
    private static int PORT = 29805;
    private static Client client = null;
    private final static String HOME = "127.0.0.1";
    private Socket mySocket;
    private DataOutputStream outStream;
    private DataInputStream inStream;
    private String authToken;
    private Gson gson;

    public static Client getClient() {
        try {
            if(client == null)
                client = new Client();
            return client;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Client() throws IOException {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public  <T, E, C extends Response> Response<T> postAndGet(Command<E> command, Class<C> responseType, Class<T> responseDataType){
        try {
            makeConnection();
            command.setAuthToken(authToken);
            System.out.println(command.getMessage());
            System.out.println("Type : " + command.getType());
            String commandStr = gson.toJson(command/*,  new TypeToken<Command<E>>(){}.getType()*/);
            System.out.println(commandStr);
            outStream.writeUTF(commandStr);
            outStream.flush();
            String responseStr = inStream.readUTF();
            Response<T> response = gson.fromJson(responseStr,  TypeToken.getParameterized(responseType, responseDataType).getType());
            closeConnection();
            return response;
        } catch (IOException e) {
            System.err.println("SHIT ERROR IN POST AND GET");
            e.printStackTrace();
        }
        return null;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    private void closeConnection() throws IOException {
        inStream.close();
        outStream.close();
        mySocket.close();
    }

    private void makeConnection() throws IOException {
        mySocket = new Socket(HOME, PORT);
        inStream = new DataInputStream(new BufferedInputStream(mySocket.getInputStream()));
        outStream = new DataOutputStream(new BufferedOutputStream(mySocket.getOutputStream()));
    }

}