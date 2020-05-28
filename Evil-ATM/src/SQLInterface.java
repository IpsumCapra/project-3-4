import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;


public class SQLInterface {
    static private Connection con = null;
    static private DataOutputStream dOut = null;
    static private Socket rec = null;

    public static void main(String[] args) throws IOException, SQLException {
        ServerSocket socket = new ServerSocket(666); // Set up receive socket
        try {
            con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/bank?buseUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", "admin", "S7r0ngP455w0rd");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Could not initiate database connection. Halting.");
            System.exit(0);
        }

        while (true) {
            rec = socket.accept();
            DataInputStream dIn = new DataInputStream(rec.getInputStream());

            String messageType = dIn.readUTF();
            JSONObject input = new JSONObject(dIn.readUTF());

            String accountNumber = StringEscapeUtils.escapeJava(input.getJSONObject("body").getString("account"));
            String pin = StringEscapeUtils.escapeJava(input.getJSONObject("body").getString("pin"));

            if (!runQuery("SELECT accountNumber FROM bank.users WHERE accountNumber = TRIM(LEADING '0' FROM \"" + accountNumber + "\");").next()) {
                if (messageType.equals("balance")) {
                    generateBalanceResponseJson(404, 0, new String[]{}, input);
                } else if (messageType.equals("withdraw")) {
                    generateWithdrawResponseJson(404, input);
                }
            }

            if (runQuery("SELECT CASE WHEN userPin = \"" + pin + "\" AND blocked = 0 THEN 0 ELSE 1  END FROM bank.users WHERE accountNumber = TRIM(LEADING '0' FROM \"" + accountNumber + "\");").getInt(1) == 1) {
                runUpdateQuery("UPDATE bank.users SET attempts = CASE WHEN attempts < 2 THEN attempts + 1 ELSE 3 END WHERE accountNumber = TRIM(LEADING '0' FROM \"" + accountNumber + "\");");
                runUpdateQuery("UPDATE bank.users SET blocked = CASE WHEN attempts = 3 THEN 1 ELSE 0 END WHERE accountNumber = TRIM(LEADING '0' FROM \"" + accountNumber + "\");");

                int code;

                if (getAttempts(accountNumber) == 3) {
                    code = 403;
                } else {
                    code = 401;
                }

                if (messageType.equals("balance")) {
                    generateBalanceResponseJson(code, 0, new String[]{}, input);
                } else if (messageType.equals("withdraw")) {
                    generateWithdrawResponseJson(code, input);
                }
            } else {
                runUpdateQuery("UPDATE bank.users SET attempts = 0 WHERE accountNumber = TRIM(LEADING '0' FROM \"" + accountNumber + "\");");
            }


            switch (messageType) {
                case "balance": // Check data.
                    ResultSet response = runQuery("SELECT firstName, CASE WHEN lastNamePreposition IS NULL THEN \"\" ELSE lastNamePreposition END, lastName, userBalance FROM bank.users WHERE accountNumber = TRIM(LEADING '0' FROM \"" + accountNumber + "\");");

                    int code = 200;
                    String[] name = new String[3];

                    for (int i = 1; i < 4; i++) {
                        name[i - 1] = response.getString(i);
                    }

                    int balance = response.getInt(4);

                    generateBalanceResponseJson(code, balance, name, input);
                    break;
                case "withdraw": // Withdraw money.
                    int amount = input.getJSONObject("body").getInt("amount");

                    if (runQuery("SELECT userBalance FROM users.bank WHERE accountNumber = TRIM(LEADING '0' FROM \"" + accountNumber + "\");").getInt(1) - amount < 0) {
                        generateWithdrawResponseJson(402, input);
                    } else {
                        runUpdateQuery("UPDATE bank.users SET userBalance = userBalance - " + amount + " WHERE accountNumber = TRIM(LEADING '0' FROM \"" + accountNumber + "\");");
                        generateWithdrawResponseJson(200, input);
                    }
                    break;
            }
        }
    }

    static private ResultSet runQuery(String query) throws SQLException {
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        if (rs.next()) {
            return rs;
        } else {
            return null;
        }
    }

    static private void runUpdateQuery(String query) throws SQLException {
        Statement stmt = con.createStatement();
        stmt.executeUpdate(query);
    }

    static private void generateBalanceResponseJson(int code, int amount, String[] name, JSONObject input) {
        JSONObject json = new JSONObject();

        JSONObject body = new JSONObject();

        body.put("code", code);
        switch (code) {
            case 200:
                body.put("message", "Success");
                body.put("balance", amount);
                if (input.getJSONObject("header").getString("originBank").equals("EVIL")) {
                    body.put("firstName", name[0]);
                    body.put("lastNamePreposition", name[1]);
                    body.put("lastName", name[2]);
                }
                break;
            case 404:
                body.put("message", "Does not exist");
                break;
            case 403:
                body.put("message", "Card blocked");
                break;
            case 401:
                body.put("message", "Incorrect code");
                body.put("attempts", getAttempts(input.getJSONObject("body").getString("account")));
        }

        json.put("header", generateResponseHeader("balance", input));
        json.put("body", body);

        sendResponse(json);
    }

    static private void generateWithdrawResponseJson(int code, JSONObject input) {
        JSONObject json = new JSONObject();

        JSONObject body = new JSONObject();

        body.put("code", code);
        switch (code) {
            case 200:
                body.put("message", "Success");
                break;
            case 404:
                body.put("message", "Does not exist");
                break;
            case 403:
                body.put("message", "Card blocked");
                break;
            case 402:
                body.put("message", "Insufficient balance");
                break;
            case 401:
                body.put("message", "Incorrect code");
                body.put("attempts", getAttempts(input.getJSONObject("body").getString("account")));
        }

        json.put("header", generateResponseHeader("withdraw", input));
        json.put("body", body);

        sendResponse(json);
    }

    static private void sendResponse(JSONObject json) {
        //dOut.writeUTF(json.toString());
    }

    static private JSONObject generateResponseHeader(String type, JSONObject input) {
        JSONObject header = new JSONObject();
        header.put("originCountry", "US");
        header.put("originBank", "EVIL");
        header.put("receiveCountry", input.getJSONObject("header").getString("originCountry"));
        header.put("receiveBank", input.getJSONObject("header").getString("originBank"));
        header.put("action", type);
        return header;
    }

    private static int getAttempts(String accountNumber) {
        try {
            ResultSet response = runQuery("SELECT attempts FROM bank.users WHERE accountNumber = TRIM(LEADING '0' FROM \"" + accountNumber + "\");");
            return response.getInt(1);
        } catch (Exception e) {
            return -1;
        }
    }
}
