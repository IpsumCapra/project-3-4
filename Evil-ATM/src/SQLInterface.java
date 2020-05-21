import org.apache.commons.text.StringEscapeUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;


public class SQLInterface {
    static Connection con = null;

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
            Socket rec = socket.accept();
            DataInputStream dIn = new DataInputStream(rec.getInputStream());
            DataOutputStream dOut = new DataOutputStream(rec.getOutputStream());

            byte messageType = dIn.readByte();
            String accountNumber = StringEscapeUtils.escapeJava(dIn.readUTF());
            String pin = StringEscapeUtils.escapeJava(dIn.readUTF());

            switch (messageType) {
                case 1: // Check data.
                    if (runQuery("SELECT CASE WHEN userPin = \"" + pin + "\" AND blocked = 0 THEN 0 ELSE 1  END FROM bank.users WHERE accountNumber = TRIM(LEADING '0' FROM \"" + accountNumber + "\");").getInt(1) == 0) {
                        ResultSet response = runQuery("SELECT firstName, CASE WHEN lastNamePreposition IS NULL THEN \"\" ELSE lastNamePreposition END, lastName, userBalance FROM bank.users WHERE accountNumber = TRIM(LEADING '0' FROM \"" + accountNumber + "\");");
                        runUpdateQuery("UPDATE bank.users SET attempts = 0 WHERE accountNumber = TRIM(LEADING '0' FROM \"" + accountNumber + "\");");
                        dOut.writeByte(1);

                        for (int i = 1; i < 4; i++) {
                            dOut.writeUTF(response.getString(i));
                        }
                        dOut.writeInt(response.getInt(4));
                    } else {
                        runUpdateQuery("UPDATE bank.users SET attempts = CASE WHEN attempts < 2 THEN attempts + 1 ELSE 3 END WHERE accountNumber = TRIM(LEADING '0' FROM \"" + accountNumber + "\");");
                        runUpdateQuery("UPDATE bank.users SET blocked = CASE WHEN attempts = 3 THEN 1 ELSE 0 END WHERE accountNumber = TRIM(LEADING '0' FROM \"" + accountNumber + "\");");

                        ResultSet response = runQuery("SELECT CASE WHEN attempts = 3 OR blocked = 1 THEN -1 ELSE attempts END FROM bank.users WHERE accountNumber = TRIM(LEADING '0' FROM \"" + accountNumber + "\");");
                        dOut.writeByte(0);
                        dOut.writeInt(response.getInt(1));
                    }
                    break;
                case 2: // Withdraw money.
                    if (runQuery("SELECT CASE WHEN userPin = \"" + pin + "\" AND blocked = 0 THEN 0 ELSE 1  END FROM bank.users WHERE accountNumber = TRIM(LEADING '0' FROM \"" + accountNumber + "\");").getInt(1) == 0) {
                        int amount = dIn.readInt();
                        runUpdateQuery("UPDATE bank.users SET userBalance = userBalance - " + amount + " WHERE accountNumber = TRIM(LEADING '0' FROM \"" + accountNumber + "\");");
                        dOut.writeByte(1);
                    } else {
                        dOut.writeByte(0);
                    }
                    break;
            }
            rec.close();
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
}
