import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONObject;

public class DatabaseInterfacer {
    private Socket socket;
    private DataInputStream dIn;
    private static DataOutputStream dOut;
    private String ip;
    private int port;

    private static final String errorMsg = "404";

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

    public String requestBalance(String accountNumber, String pin) {
        if (!connect()) {
            return errorMsg;
        }
        try {

            String[] accountInfo = accountNumber.split("-");

            dOut.writeUTF("balance");
            generateBalanceRequestJson(accountInfo, pin);

            JSONObject input = new JSONObject(dIn.readUTF());
            int code = input.getJSONObject("body").getInt("code");

            if (code == 200) {
                return input.toString();
            } else {
                return String.valueOf(code);
            }

        } catch (Exception e) {
            return errorMsg;
        }
    }

    public String requestTransaction(String accountNumber, String pin, String withdrawAmount) {
        if (!connect()) {
            return errorMsg;
        }
        try {

            String[] accountInfo = accountNumber.split("-");

            dOut.writeUTF("withdraw");
            generateTransactionRequestJson(withdrawAmount, accountInfo, pin);

            JSONObject input = new JSONObject(dIn.readUTF());
            int code = input.getJSONObject("body").getInt("code");

            if (code == 200) {
                return input.toString();
            } else {
                return String.valueOf(code);
            }

        } catch (Exception e) {
            return errorMsg;
        }
    }

    static private void generateTransactionRequestJson(String withdrawAmount, String[] accountInfo, String pin) {
        JSONObject json = new JSONObject();

        JSONObject body = new JSONObject();

        body.put("account", accountInfo[2]);
        body.put("pin", pin);
        body.put("amount", withdrawAmount);

        json.put("header", generateRequestHeader("withdraw", accountInfo));
        json.put("body", body);

        sendRequest(json);
    }

    static private void generateBalanceRequestJson(String[] accountInfo, String pin) {
        JSONObject json = new JSONObject();

        JSONObject body = new JSONObject();

        body.put("account", accountInfo[2]);
        body.put("pin", pin);

        json.put("header", generateRequestHeader("balance", accountInfo));
        json.put("body", body);

        sendRequest(json);
    }

    static private JSONObject generateRequestHeader(String requestType, String[] accountInfo) {
        JSONObject header = new JSONObject();
        header.put("originCountry", "US");
        header.put("originBank", "EVIL");
        header.put("receiveCountry", accountInfo[0]);
        header.put("receiveBank", accountInfo[1]);
        header.put("action", requestType);
        return header;
    }

    static private void sendRequest(JSONObject json) {
        try {
            dOut.writeUTF(json.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
