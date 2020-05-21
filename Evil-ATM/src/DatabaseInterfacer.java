import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class DatabaseInterfacer {
    private Socket socket;
    private DataInputStream dIn;
    private DataOutputStream dOut;
    private String ip;
    private int port;

    private static final String[] errorMsg = {"404"};

    public DatabaseInterfacer(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    private boolean connect() {
        try {
            socket = new Socket(ip, port);
            dIn = new DataInputStream(socket.getInputStream());
            dOut = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public String[] getInformation(String accountNumber, String pin) {
        if (!connect()) {
            return errorMsg;
        }
        try {
            dOut.writeByte(1);
            dOut.writeUTF(accountNumber);
            dOut.writeUTF(pin);
        } catch (Exception e) {
            return errorMsg;
        }

        try {
            switch(dIn.readByte()) {
                case 1:
                    return new String[] {
                        dIn.readUTF(), //Voornaam
                        dIn.readUTF(), //Tussenvoegsel als het er niet is is het ""
                        dIn.readUTF(), //Achternaam
                        String.valueOf(dIn.readInt()) //Balans
                    };
                case 0:
                    return new String[] {String.valueOf(dIn.readInt())};
            }
        } catch (Exception e) {
            return errorMsg;
        }
        return errorMsg;
    }
}
