import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Receiver {
    public static void main(String[] args) throws IOException {
        ServerSocket socket = new ServerSocket(666); // Set up receive socket
        Socket rec = socket.accept();
        DataInputStream dIn = new DataInputStream(rec.getInputStream());

        boolean done = false;
        while(!done) {
            byte messageType = dIn.readByte();

            switch(messageType)
            {
                case 1: // Type A
                    System.out.println("Message A: " + dIn.readUTF());
                    break;
                case 2: // Type B
                    System.out.println("Message B: " + dIn.readUTF());
                    break;
                case 3: // Type C
                    System.out.println("Message C [1]: " + dIn.readUTF());
                    System.out.println("Message C [2]: " + dIn.readUTF());
                    break;
                default:
                    done = true;
            }
        }
        dIn.close();
    }
}
