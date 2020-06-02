import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;
import org.apache.commons.text.StringEscapeUtils;


public class Main {
    DatabaseInterfacer database = new DatabaseInterfacer("145.24.222.190", 665);

    private static KeypadListener keypad;
    private static JButton[] numpadButtons;
    private static JButton[] withdrawButtons;
    private static JButton[] customPadButtons;

    static JLabel debug;

    private JLabel cashLabel = new JLabel("amount");
    private JLabel saldoMessage = new JLabel("message");

    private JPanel cards; // a panel that uses CardLayout
    private JPasswordField passwordField = new JPasswordField(MAX_PIN_SIZE);
    private JTextField cashField = new JTextField(4);
    private JLabel result = new JLabel();
    private JLabel error = new JLabel();
    private String cash = "";
    private String pin = "";
    private String IBAN = "";
    private String firstName = "";
    private String lastNamePreposition = "";
    private String lastName = "";
    private String accountNumber = "";
    private int amountPinned = 0;
    private JSONObject receivedData;

    private final static String WELCOME_SCREEN = "Welcome screen";
    private final static String LOGIN_SCREEN = "Login screen";
    private final static String MAIN_MENU = "Main menu";
    private final static String TEST_WINDOW = "test window";
    private final static String TRANSACTION_SCREEN = "transaction screen";
    private final static String RECEIPT_SCREEN = "receipt screen";
    private final static String SALDO_SCREEN = "saldo screen";

    private final static String[] NUMPAD_CONTENT = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#" };
    private final static String[] WITHDRAW_OPTIONS = { "10", "20", "30", "40", "50", "60", "70" };

    final static int MAX_PIN_SIZE = 6;

    ActionListener backToMainMenu = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            setCard(MAIN_MENU);
        }
    };

    ActionListener abortTransaction = new ActionListener() {
        public void actionPerformed(ActionEvent actionEvent) {
            setCard(WELCOME_SCREEN);
        }
    };

    ActionListener login = new ActionListener() {
        public void actionPerformed(ActionEvent actionEvent) {
            setCard(LOGIN_SCREEN);
        }
    };

    public void addComponentToPane(Container pane) {
        // Create the atm "screens".

        /* LOGIN SCREEN */
        JPanel loginScreen = new JPanel();
        debug = new JLabel("TEST");
        loginScreen.add(debug);

        passwordField.setEditable(false);
        loginScreen.add(passwordField);

        JButton loginAbortButton = new JButton("Abort");
        loginAbortButton.addActionListener(abortTransaction);
        loginScreen.add(loginAbortButton);

        /* Numpad for login screen */
        JPanel numpad = new JPanel();
        numpad.setLayout(new GridLayout(4, 3, 1, 1));
        numpadButtons = new JButton[NUMPAD_CONTENT.length];
        for (int i = 0; i < NUMPAD_CONTENT.length; i++) {
            numpadButtons[i] = new JButton(NUMPAD_CONTENT[i]);
            numpadButtons[i].setPreferredSize(new java.awt.Dimension(80, 50));

            numpadButtons[i].addActionListener(new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    loginNumpadButtonActionPerformed(evt);
                }
            });
            numpad.add(numpadButtons[i]);
        }
        loginScreen.add(numpad);
        loginScreen.add(error);
        
        /* WELCOME SCREEN */
        JPanel welcomeScreen = new JPanel();
        welcomeScreen.setLayout(new GridBagLayout());
        welcomeScreen.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel welcomeLabel = new JLabel("Welcome to the Evil corp ATM. Insert your card to continue.");
        welcomeScreen.add(welcomeLabel);

        RFIDListener rListener = new RFIDListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                IBAN = actionEvent.getActionCommand().split("-")[2];
                accountNumber = actionEvent.getActionCommand();
                setCard(LOGIN_SCREEN);
            }
        });
        rListener.start();

        /* temp login */
        JButton testCont = new JButton("continue (TEST PURPOSES)");
        testCont.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                setCard(LOGIN_SCREEN);
            }
        });
        welcomeScreen.add(testCont);

        /* TRANSACTION SCREEN */
        JPanel transactionScreen = new JPanel();
        transactionScreen.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        transactionScreen.add(new JLabel("Transaction Menu"));

        cashField.setEditable(false);
        transactionScreen.add(cashField);

        transactionScreen.add(result);

        JButton transactionAbortButton = new JButton("Abort");
        transactionAbortButton.addActionListener(abortTransaction);
        transactionScreen.add(transactionAbortButton);

        JButton transactionMain = new JButton("back to main menu");
        transactionMain.addActionListener(backToMainMenu);
        transactionScreen.add(transactionMain);

        /* hotkeys for withdrawal */
        JPanel withdrawOptions = new JPanel();
        withdrawOptions.setLayout(new GridLayout(2, 4, 1, 1));
        withdrawButtons = new JButton[WITHDRAW_OPTIONS.length];
        for (int i = 0; i < WITHDRAW_OPTIONS.length; i++) {
            withdrawButtons[i] = new JButton(WITHDRAW_OPTIONS[i]);
            withdrawButtons[i].setPreferredSize(new java.awt.Dimension(80, 50));

            withdrawButtons[i].addActionListener(new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    System.out.println(evt.getActionCommand().toString());
                    switch (evt.getActionCommand().toString()) {
                        case "10":
                            result.setText("You receive 1 E10 bill");
                            break;
                        case "20":
                            result.setText("You receive 1 E20 bill");
                            break;
                        case "30":
                            result.setText("You receive 1 E10 bill and 1 E20 bill");
                            break;
                        case "40":
                            result.setText("You receive 2 E20 bills");
                            break;
                        case "50":
                            result.setText("You receive 1 E50 bill");
                            break;
                        case "60":
                            result.setText("You receive 3 E20 bills");
                            break;
                        case "70":
                            result.setText("You receive 1 E50 bill and 1 E20 bill");
                            break;
                        default:
                            result.setText("Something went wrong");
                            break;
                    }
                }
            });
            withdrawOptions.add(withdrawButtons[i]);
        }
        transactionScreen.add(withdrawOptions);

        /* Numpad for withdrawal */
        JPanel customPad = new JPanel();
        customPad.setLayout(new GridLayout(4, 3, 1, 1));
        customPadButtons = new JButton[NUMPAD_CONTENT.length];
        for (int i = 0; i < NUMPAD_CONTENT.length; i++) {
            customPadButtons[i] = new JButton(NUMPAD_CONTENT[i]);
            customPadButtons[i].setPreferredSize(new java.awt.Dimension(80, 50));

            customPadButtons[i].addActionListener(new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    customWithdrawalButtonActionPerformed(evt);
                }
            });
            customPad.add(customPadButtons[i]);
        }
        transactionScreen.add(customPad);

        /* MAIN MENU */
        JPanel mainMenu = new JPanel();
        mainMenu.add(new JLabel("THIS IS THE MAIN MENU"));

        JButton mainMenuAbortButton = new JButton("Abort");
        mainMenuAbortButton.addActionListener(abortTransaction);
        mainMenu.add(mainMenuAbortButton);

        JButton mainLogin = new JButton("To login");
        mainLogin.addActionListener(login);
        mainMenu.add(mainLogin);

        /*
        JButton testButton = new JButton("to test window");
        testButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                setCard(TEST_WINDOW);
            }
        });
        */

        JButton saldoButton = new JButton("Check saldo");
        saldoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                saldoMessage.setText("Hello " + firstName + " " + lastNamePreposition + " " + lastName + ". This is your current saldo:");
                cashLabel.setText(cash);
                setCard(SALDO_SCREEN);
            }
        });

        //mainMenu.add(testButton);
        mainMenu.add(saldoButton);

        /* SALDO WINDOW */
        JPanel saldoWindow = new JPanel();
        saldoWindow.add(saldoMessage);
        saldoWindow.add(cashLabel);

        JButton saldoAbort = new JButton("Abort");
        saldoAbort.addActionListener(abortTransaction);
        saldoWindow.add(saldoAbort);

        JButton saldoMain = new JButton("back to main menu");
        saldoMain.addActionListener(backToMainMenu);
        saldoWindow.add(saldoMain);


        /* PRINT WINDOW */
        JPanel printWindow = new JPanel();
        printWindow.add(new JLabel("Would you like a physical receipt?"));
        JButton printButton = new JButton("YES");
        printButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (lastNamePreposition.equals(""))
                    printReceipt(accountNumber, firstName + " " + lastName, String.valueOf(amountPinned));
                else
                    printReceipt(accountNumber, firstName + " " + lastNamePreposition + " " + lastName,
                            String.valueOf(amountPinned));
            }
        });
        printWindow.add(printButton);

        JButton endTransactionButton = new JButton("NO");
        printWindow.add(endTransactionButton);

        /* TEST WINDOW */
        JPanel testWindow = new JPanel();
        testWindow.add(new JLabel("TEST WINDOW"));

        JButton testAbort = new JButton("Abort");
        testAbort.addActionListener(abortTransaction);
        testWindow.add(testAbort);

        JButton testMain = new JButton("back to main menu");
        testMain.addActionListener(backToMainMenu);
        testWindow.add(testMain);

        // Create the panel that contains the "screens".
        cards = new JPanel(new CardLayout());
        cards.add(welcomeScreen, WELCOME_SCREEN);
        cards.add(loginScreen, LOGIN_SCREEN);
        cards.add(mainMenu, MAIN_MENU);
        cards.add(testWindow, TEST_WINDOW);
        cards.add(transactionScreen, TRANSACTION_SCREEN);
        cards.add(printWindow, RECEIPT_SCREEN);
        cards.add(saldoWindow, SALDO_SCREEN);

        pane.add(cards, BorderLayout.CENTER);
    }

    private static void createAndShowGUI() {
        // Create and set up the window
        JFrame frame = new JFrame("EVIL ATM GUI");

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        GraphicsEnvironment graphics = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = graphics.getDefaultScreenDevice();

        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setResizable(false);
        frame.setUndecorated(true);

        // Create and set up the content pane.
        Main demo = new Main();

        demo.addComponentToPane(frame.getContentPane());
        demo.setCard(WELCOME_SCREEN);

        keypad = new KeypadListener(numpadButtons);
        keypad.start();
        // Display the window.
        device.setFullScreenWindow(frame);
    }

    public static void main(String[] args) {
        // Schedule a job for the event dispatch thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });

    }

    public void setCard(String cardId) {
        CardLayout cl = (CardLayout) cards.getLayout();
        cl.show(cards, cardId);
    }

    public void loginNumpadButtonActionPerformed(ActionEvent evt) {
        if (evt.getActionCommand().toString().equalsIgnoreCase("#")) {
            try {
                receivedData = new JSONObject(database.requestBalance(accountNumber, pin));
            } catch (Exception e) {
                error.setText(accountNumber + " " + pin);
                return;
            }

            int code = receivedData.getJSONObject("body").getInt("code");
            if (code == 200) {
                if (accountNumber.split("-")[1].equals("EVIL")) {
                    firstName = receivedData.getJSONObject("body").getString("firstName");
                    lastNamePreposition = receivedData.getJSONObject("body").getString("lastNamePreposition");
                    lastName = receivedData.getJSONObject("body").getString("lastName");
                } else {
                    firstName = "user";
                }
                cash = String.valueOf(receivedData.getJSONObject("body").getInt("balance"));
                setCard(MAIN_MENU);
            } else {
                String message = StringEscapeUtils.escapeJava(receivedData.getJSONObject("body").getString("message"));
                error.setText("Error: " + message);
                return;
            }

        } else if (evt.getActionCommand().toString().equalsIgnoreCase("*")) {
            pin = "";
            passwordField.setText(pin);
        } else {
            pin += evt.getActionCommand().toString();
            passwordField.setText(pin);
        }
    }

    public void customWithdrawalButtonActionPerformed(ActionEvent evt) {
        if (evt.getActionCommand().toString().equalsIgnoreCase("#")) {
            int finalCash = Integer.parseInt(cash);
            result.setText(calculateBills(finalCash));
            cash = "";
            cashField.setText(cash);

        } else if (evt.getActionCommand().toString().equalsIgnoreCase("*")) {
            cash = "";
            cashField.setText(cash);
        } else if (cash.length() >= 4) {
            return;
        } else {
            cash += evt.getActionCommand().toString();
            cashField.setText(cash);
        }
    }

    public String calculateBills(int total) {
        int e50 = 0;
        int e20 = 0;
        int e10 = 0;
        int e5 = 0;

        if (!((total / 5) % 1 == 0)) {
            return "Please input a number divisible by 5";
        }

        while (total / 50 >= 1) {
            e50++;
            total -= 50;
        }
        while (total / 20 >= 1) {
            e20++;
            total -= 20;
        }
        while (total / 10 >= 1) {
            e10++;
            total -= 10;
        }
        while (total / 5 >= 1) {
            e5++;
            total -= 5;
        }
        String bills = "You get " + e50 + " E50 bills, " + e20 + " E20 bills, " + e10 + " E10 bills, " + e5
                + " E5 bills";
        return bills;
    }

    public boolean printReceipt(String IBAN, String name, String amount) {
        try {
            I2CBus bus = I2CFactory.getInstance(1);
            I2CDevice device = bus.getDevice(0x08);
            byte[] receiptData = (IBAN + "," + name + "," + amount + ",").getBytes();
            for (int i = 0; i < receiptData.length; i++) {
                device.write(receiptData[i]);
            }
            
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}