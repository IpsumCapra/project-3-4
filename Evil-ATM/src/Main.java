import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import javax.swing.*;
import org.json.JSONObject;
import org.apache.commons.text.StringEscapeUtils;

public class Main {
    DatabaseInterfacer database = new DatabaseInterfacer("145.24.222.190", 665);

    private static KeypadListener keypad;
    private static RFIDListener rListener;
    private static JButton[] numpadButtons;
    private static JButton[] withdrawButtons;
    private static JButton[] customPadButtons;

    private File file = new File("notes");

    static private final int[] NOTE_VALUES = new int[]{50, 20, 10, 5};
    static private int noteAmounts[] = new int[]{0, 0, 0, 0};
    static private JLabel[] noteAmountText = new JLabel[4];
    static private JLabel welcomeLabel = new JLabel();

    private JLabel cashLabel = new JLabel("amount");
    private JLabel saldoMessage = new JLabel("message");

    private JPanel cards; // a panel that uses CardLayout
    private JPasswordField passwordField = new JPasswordField(MAX_PIN_SIZE);
    private JTextField cashField = new JTextField(4);
    private JLabel result = new JLabel();
    private JLabel error = new JLabel();
    private JLabel transactionError = new JLabel();
    private JLabel internalNoteAmountLabel = new JLabel();
    private String cash = "";
    private String pin = "";
    private String firstName = "";
    private String lastNamePreposition = "";
    private String lastName = "";
    private String accountNumber = "";
    private JSONObject receivedData;

    private final static String WELCOME_SCREEN = "Welcome screen";
    private final static String LOGIN_SCREEN = "Login screen";
    private final static String MAIN_MENU = "Main menu";
    private final static String TRANSACTION_SCREEN = "transaction screen";
    private final static String RECEIPT_SCREEN = "receipt screen";
    private final static String SALDO_SCREEN = "saldo screen";
    private final static String NOTE_SELECT_SCREEN = "Note selection screen";
    private final static String CUSTOM_AMOUNT_SCREEN = "Custom amount selection screen";

    private final static String WELCOME_TEXT = "Welcome to the Evil corp ATM. Insert your card to continue.";
    private final static String TRANSACT_FINISH = "Thank you for choosing Evil Corp. Please remove your debit card, cash and receipt.";

    private final static String[] NUMPAD_CONTENT = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#" };
    private final static String[] WITHDRAW_OPTIONS = { "10", "20", "30", "40", "50", "60", "70", "CUS" };

    final static int MAX_PIN_SIZE = 6;

    ActionListener backToMainMenu = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            keypad.setKeypadBlock(true);
            transactionError.setText("");
            setCard(MAIN_MENU);
        }
    };

    Timer timer = new Timer(5000, new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent ae) {
            welcomeLabel.setText(WELCOME_TEXT);
        }
    });

    public void addComponentToPane(Container pane) {
        // Create the atm "screens".

        /* LOGIN SCREEN */
        JPanel loginScreen = new JPanel();

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
        welcomeLabel = new JLabel(WELCOME_TEXT);
        welcomeScreen.add(welcomeLabel);

        rListener = new RFIDListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                accountNumber = actionEvent.getActionCommand();
                setCard(LOGIN_SCREEN);
                keypad.setKeypadBlock(false);
                rListener.setRFIDBlock(true);
            }
        });
        rListener.start();

        /* temp login */
        //JButton testCont = new JButton("continue (TEST PURPOSES)");
        //testCont.addActionListener(new ActionListener() {
        //    public void actionPerformed(ActionEvent actionEvent) {
        //        setCard(LOGIN_SCREEN);
        //    }
        //});
        //welcomeScreen.add(testCont);

        /* TRANSACTION SCREEN */
        JPanel transactionScreen = new JPanel();

        transactionScreen.add(new JLabel("Transaction Menu"));

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
                    if (evt.getActionCommand().equals("CUS")) {
                        setCard(CUSTOM_AMOUNT_SCREEN);
                    } else {
                        cash = evt.getActionCommand();
                        setCard(NOTE_SELECT_SCREEN);
                        computeBills(Integer.parseInt(cash));
                    }
                }
            });
            withdrawOptions.add(withdrawButtons[i]);
        }
        transactionScreen.add(withdrawOptions);

        /* NOTE SELECTOR */
        JPanel noteSelectorScreen = new JPanel();

        noteSelectorScreen.add(transactionError);

        JButton noteAbortButton = new JButton("Abort");
        noteAbortButton.addActionListener(abortTransaction);
        noteSelectorScreen.add(noteAbortButton);

        JButton noteMain = new JButton("Back to main menu");
        noteMain.addActionListener(backToMainMenu);
        noteSelectorScreen.add(noteMain);

        JButton noteBack = new JButton("Amount selection");
        noteBack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                computeBills(0);
                transactionError.setText("");
                setCard(TRANSACTION_SCREEN);
            }
        });
        noteSelectorScreen.add(noteBack);

        JButton continueTransaction = new JButton("Perform transaction");
        continueTransaction.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    Scanner notes = new Scanner(file);

                    String[] noteArray = notes.nextLine().split(",");

                    notes.close();

                    for (int i = 0; i < noteAmounts.length; i++) {
                        if (Integer.parseInt(noteArray[i]) < noteAmounts[i]) {
                            transactionError.setText("Something went wrong. Please contact Evil Corp.");
                            return;
                        }
                    }
                } catch (Exception e) {
                    transactionError.setText("Something went wrong.");
                    return;
                }

                JSONObject response;
                try {
                    response = new JSONObject(database.requestTransaction(accountNumber, pin, cash));
                } catch (Exception e) {
                    transactionError.setText("Something went wrong. Please try again.");
                    return;
                }

                int code = response.getJSONObject("body").getInt("code");
                if (code == 200) {
                    try {
                        Scanner notes = new Scanner(file);

                        String[] noteArray = notes.nextLine().split(",");

                        notes.close();

                        for (int i = 0; i < noteAmounts.length; i++) {
                            noteArray[i] = String.valueOf(Integer.parseInt(noteArray[i]) - noteAmounts[i]);
                        }

                        internalNoteAmountLabel.setText("ATM note amounts: E50: " + noteArray[0] + " E20: " + noteArray[1] + " E10: " + noteArray[2] + " E5: " + noteArray[3]);

                        FileWriter writer = new FileWriter(file, false);

                        writer.write(noteArray[0] + "," + noteArray[1] + "," + noteArray[2] + "," + noteArray[3]);
                        writer.close();
                    } catch (Exception e) {

                    }
                    dispenseBills();
                    setCard(RECEIPT_SCREEN);
                } else {
                    String message = StringEscapeUtils.escapeJava(response.getJSONObject("body").getString("message"));
                    transactionError.setText("Error " + code + ": " + message);
                    return;
                }
            }
        });
        noteSelectorScreen.add(continueTransaction);

        JLabel noteTotal = new JLabel("Total: ");

        JPanel selector = new JPanel();

        selector.setLayout(new GridLayout(4, 4));

        selector.add(new JLabel("E50"));
        selector.add(new JLabel("E20"));
        selector.add(new JLabel("E10"));
        selector.add(new JLabel("E5"));

        for (int i = 0; i < 4; i++) {
            int column = i;
            JButton button = new JButton("+");
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    addNote(column);
                }
            });
            selector.add(button);
        }
        for (int i = 0; i < 4; i++) {
            noteAmountText[i] = new JLabel(String.valueOf(noteAmounts[i]));
            selector.add(noteAmountText[i]);
        }
        for (int i = 0; i < 4; i++) {
            int column = i;
            JButton button = new JButton("-");
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    subNote(column);
                }
            });
            selector.add(button);
        }
        noteSelectorScreen.add(noteTotal);
        noteSelectorScreen.add(selector);



        /* Numpad for withdrawal */
        JPanel customAmountScreen = new JPanel();

        JButton customAbortButton = new JButton("Abort");
        customAbortButton.addActionListener(abortTransaction);
        customAmountScreen.add(customAbortButton);

        JButton customMain = new JButton("Back to main menu");
        customMain.addActionListener(backToMainMenu);
        customAmountScreen.add(customMain);

        JButton customBack = new JButton("Amount selection");
        customBack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                cash = "";
                cashField.setText("");
                transactionError.setText("");
                setCard(TRANSACTION_SCREEN);
            }
        });
        customAmountScreen.add(customBack);

        JPanel customPad = new JPanel();

        cashField.setEditable(false);
        customAmountScreen.add(cashField);

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
        customAmountScreen.add(customPad);

        /* MAIN MENU */
        JPanel mainMenu = new JPanel();
        mainMenu.add(new JLabel("THIS IS THE MAIN MENU"));

        JButton mainMenuAbortButton = new JButton("Abort");
        mainMenuAbortButton.addActionListener(abortTransaction);
        mainMenu.add(mainMenuAbortButton);

        JButton saldoButton = new JButton("Check saldo");
        saldoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                saldoMessage.setText("Hello " + firstName + " " + lastNamePreposition + " " + lastName + ". This is your current saldo:");
                cashLabel.setText(cash);
                setCard(SALDO_SCREEN);
            }
        });

        JButton transactionButton = new JButton("New transaction");
        transactionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                keypad.setButtons(customPadButtons);
                keypad.setKeypadBlock(false);

                //clear transaction screen.
                customPadButtons[9].doClick();
                setCard(TRANSACTION_SCREEN);
            }
        });

        mainMenu.add(saldoButton);
        mainMenu.add(transactionButton);

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
                if (printReceipt(accountNumber.split("-")[2], String.valueOf(cash)))
                    abortTransaction.actionPerformed(actionEvent);
            }
        });
        printWindow.add(printButton);

        JButton endTransactionButton = new JButton("NO");
        endTransactionButton.addActionListener(abortTransaction);
        printWindow.add(endTransactionButton);

        printWindow.add(internalNoteAmountLabel);

        // Create the panel that contains the "screens".
        cards = new JPanel(new CardLayout());
        cards.add(welcomeScreen, WELCOME_SCREEN);
        cards.add(loginScreen, LOGIN_SCREEN);
        cards.add(mainMenu, MAIN_MENU);
        cards.add(transactionScreen, TRANSACTION_SCREEN);
        cards.add(printWindow, RECEIPT_SCREEN);
        cards.add(saldoWindow, SALDO_SCREEN);
        cards.add(customAmountScreen, CUSTOM_AMOUNT_SCREEN);
        cards.add(noteSelectorScreen, NOTE_SELECT_SCREEN);

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
        unblockRFID();
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
                error.setText("Something went wrong.");
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
        if (evt.getActionCommand().equalsIgnoreCase("#") && !(Integer.parseInt(cash) == 0)) {
            setCard(NOTE_SELECT_SCREEN);
            computeBills(Integer.parseInt(cash));
        } else if (evt.getActionCommand().equalsIgnoreCase("*")) {
            cash = "";
            cashField.setText(cash);
        } else if (cash.length() >= 4) {
            return;
        } else {
            if (!(cash.length() == 0 && evt.getActionCommand().equals("0"))) {
                cash += evt.getActionCommand();
                cashField.setText(cash);
            }
        }
    }

    public boolean printReceipt(String IBAN, String amount) {
        try {
            I2CBus bus = I2CFactory.getInstance(1);
            I2CDevice device = bus.getDevice(0x08);
            byte[] receiptData = ("*," + IBAN + "," + amount + ".").getBytes();
            device.write(receiptData, 0, receiptData.length);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean unblockRFID() {
        try {
            I2CBus bus = I2CFactory.getInstance(1);
            I2CDevice device = bus.getDevice(0x08);
            device.write((byte) '+');
            rListener.setRFIDBlock(false);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean dispenseBills() {
        try {
            I2CBus bus = I2CFactory.getInstance(1);
            I2CDevice device = bus.getDevice(0x08);
            byte[] billData = ("#" + noteAmounts[1] + ".").getBytes();
            device.write(billData, 0, billData.length);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    ActionListener abortTransaction = new ActionListener() {
        public void actionPerformed(ActionEvent actionEvent) {
            setCard(WELCOME_SCREEN);

            welcomeLabel.setText(TRANSACT_FINISH);

            error.setText("");
            transactionError.setText("");
            cashField.setText("");

            receivedData = null;
            accountNumber = "";
            firstName = "";
            lastNamePreposition = "";
            lastName = "";
            pin = "";
            passwordField.setText("");
            cash = "";
            keypad.setButtons(numpadButtons);
            keypad.setKeypadBlock(true);
            unblockRFID();

            timer.stop();
            timer.start();
        }
    };

    public static void computeBills(int amount) {
        noteAmounts = new int[]{0, 0, 0, 0};
        for (; amount > 49; amount -= 50) {
            noteAmounts[0]++;
        }
        for (; amount > 19; amount -= 20) {
            noteAmounts[1]++;
        }
        for (; amount > 9; amount -= 10) {
            noteAmounts[2]++;
        }
        for (; amount > 4; amount -= 5) {
            noteAmounts[3]++;
        }
        updateNoteText();
    }

    public static void addNote(int column) {
        int total = 0;
        int amount = NOTE_VALUES[column];
        for (int i = column + 1; i < 4; i++) {
            total += noteAmounts[i] * NOTE_VALUES[i];
        }
        System.out.println(total);
        if (total >= NOTE_VALUES[column]) {
            noteAmounts[column]++;
            for (int i = column + 1; i < 4; i++) {
                System.out.println(noteAmounts[i]);
                while (noteAmounts[i] > 0) {
                    if (amount - NOTE_VALUES[i] >= 0) {
                        noteAmounts[i]--;
                        amount -= NOTE_VALUES[i];
                    } else {
                        break;
                    }
                }
                System.out.println(i + ": " + amount);
            }
            System.out.println("");
            updateNoteText();
        }
    }

    public static void subNote(int column) {
        if (noteAmounts[column] > 0) {
            if (column == 0) {
                noteAmounts[0]--;
                noteAmounts[1] += 2;
                noteAmounts[2]++;
            } else if (column == 3) {
                addNote(2);
            } else {
                noteAmounts[column]--;
                noteAmounts[column + 1] += 2;
            }
            updateNoteText();
        }
    }

    public static void updateNoteText() {
        for (int i = 0; i < 4; i++) {
            noteAmountText[i].setText(String.valueOf(noteAmounts[i]));
        }
    }
}