package lupos.endpoint.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * This main method of this class stops an extended server on the same computer by creating a socket and sending the stop signal.
 */
public class StopServer {

	public static void main(final String[] args) throws UnknownHostException, IOException {
		System.out.println("Stopping the LUPOSDATE Endpoint...");
		// Start Socket listener for receiving signal for stopping the server...
		final Socket socket = new Socket("localhost", Endpoint.portForStopping);
		final DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		out.writeUTF("Stop LUPOSDATE Endpoint");
		final DataInputStream in = new DataInputStream(socket.getInputStream());
        String responseLine;
        while ((responseLine = in.readUTF()) != null) {
            System.out.println("Server: " + responseLine);
            if (responseLine.compareTo("Server stopped") == 0) {
              break;
            }
        }
        out.close();
        in.close();
        socket.close();
	}

}
