import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class Sender {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("145.24.222.190", 666); // Create and connect the socket
        DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
        DataInputStream dIn = new DataInputStream(socket.getInputStream());
        // Send first message
        dOut.writeByte(Integer.parseInt(args[0]));
        dOut.writeUTF(args[1]);
        dOut.writeUTF(args[2]);
        if (args[0].equals("2")) {
            System.out.println("Sending thru balance...");
            dOut.writeInt(Integer.parseInt(args[3]));
        }
        dOut.flush(); // Send off the data
        byte messageType = dIn.readByte();
        if (args[0].equals("1")) {
            switch (messageType) {
                case 1:
                    System.out.println(dIn.readUTF());
                    System.out.println(dIn.readUTF());
                    System.out.println(dIn.readUTF());
                    System.out.println(dIn.readInt());
                    break;
                case 0:
                    System.out.println(dIn.readInt());
                    break;
            }
        } else {
            System.out.println(messageType);
        }
    }
}

