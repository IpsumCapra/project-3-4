import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONObject;

public class DatabaseInterfacer {
    private Socket socket;
    private DataInputStream dIn;
    private DataOutputStream dOut;
    private String ip;
    private int port;

    private static final String[] errorMsg = { "404" };

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

    public String[] getInformation(String accountNumber, String pin, String requestType) {
        if (!connect()) {
            return errorMsg;
        }
        try {

            String[] accountInfo = accountNumber.split("-");

            if (requestType == "balance" || requestType == "withdraw") {
                dOut.writeUTF(requestType);
                generateRequestJson(requestType, accountInfo, pin);

                JSONObject input = new JSONObject(dIn.readUTF());
                String[] code = StringEscapeUtils.escapeJava(input.getJSONObject("body").getString("code"));

                if (code[0] == "200") {
                    return input.toString();
                } else {
                    return code;
                }

            } else {
                return errorMsg;
            }
        } catch (Exception e) {
            return errorMsg;
        }

        return errorMsg;
    }

    static private void generateRequestJson(String requestType, String[] accountInfo, String pin) {
        JSONObject json = new JSONObject();

        JSONObject body = new JSONObject();

        body.put("account", accountInfo[2]);
        body.put("pin", pin);

        json.put("header", generateResponseHeader(requestType, accountInfo));
        json.put("body", body);

        sendResponse(json);
    }

    static private JSONObject generateResponseHeader(String requestType, String[] accountInfo) {
        JSONObject header = new JSONObject();
        header.put("originCountry", "US");
        header.put("originBank", "EVIL");
        header.put("receiveCountry", accountInfo[0]);
        header.put("receiveBank", accountInfo[1]);
        header.put("action", requestType);
        return header;
    }

    static private void sendResponse(JSONObject json) {
        dOut.writeUTF(json.toString());
    }
}
