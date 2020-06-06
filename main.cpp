//rfid setup
#include <SPI.h>
#include <MFRC522.h>
#include <Keypad.h>
#include <Wire.h>
#include "Adafruit_Thermal.h"
#include "elogo.h"

#define TX_PIN 6 // Arduino transmit  YELLOW WIRE  labeled RX on printer
#define RX_PIN 5 // Arduino receive   GREEN WIRE   labeled TX on printer

Adafruit_Thermal printer(&Serial);

#define SS_PIN 10
#define RST_PIN 9
MFRC522 mfrc522(SS_PIN, RST_PIN); // Create MFRC522 instance.

//keypad setup
const byte ROWS = 4; //four rows
const byte COLS = 4; //four columns
//define the symbols on the buttons of the keypads
char hexaKeys[ROWS][COLS] = {
        {'1', '2', '3', 'A'},
        {'4', '5', '6', 'B'},
        {'7', '8', '9', 'C'},
        {'*', '0', '#', 'D'}};
byte rowPins[ROWS] = {7, 6, 4, 5};   //connect to the row pinouts of the keypad
byte colPins[COLS] = {3, 2, A2, A3}; //connect to the column pinouts of the keypad

//initialize an instance of class NewKeypad
Keypad keypad = Keypad(makeKeymap(hexaKeys), rowPins, colPins, ROWS, COLS);

//vars
byte sendArray[17];
char inKey;
bool blocked = false;

String IBAN = "";
String withdrawal = "";
String printAmount = "";
String e20 = "";

bool printTask = false;
bool resetTask = false;
bool dispenseTask = false;

void dispenseBills(String e20);
void resetVars();
void requestEvent();
void receiveEvent(int i);
void printReceipt(String IBAN, String withdrawal);

void setup()
{
    // NOTE: SOME PRINTERS NEED 9600 BAUD instead of 19200, check test page.
    //Serial.begin(19200); // Initialize printer Serial
    printer.begin(); // Init printer (same regardless of serial type)

    Serial.begin(9600); // Initiate a serial communication
    SPI.begin();        // Initiate  SPI bus
    mfrc522.PCD_Init(); // Initiate MFRC522
    //Serial.println("Approximate your card to the reader...");
    //Serial.println();

    Wire.begin(0x08);             // Initialize I2C communications as Slave
    Wire.onRequest(requestEvent); // Function to run when data requested from master
    Wire.onReceive(receiveEvent); // Function to run when data received from master
    //printReceipt("00000001", "Sam Cornelisse", "200");
}

void loop()
{
    if (printTask)
    {
        printReceipt(IBAN, withdrawal);
        printTask = false;
    }
    if (resetTask)
    {
        resetVars();
        resetTask = false;
    }
    if (dispenseTask)
    {
        dispenseBills(e20);
        dispenseTask = false;
    }

    inKey = keypad.getKey();
    if (inKey)
    {
        sendArray[16] = inKey;
        //Serial.println(inKey);
        digitalWrite(8, HIGH);
        delay(1);
        digitalWrite(8, LOW);
    }

    if (!blocked)
    {
        // Look for new cards
        if (!mfrc522.PICC_IsNewCardPresent())
        {
            return;
        } // Select one of the cards
        if (!mfrc522.PICC_ReadCardSerial())
        {
            return;
        }

        // Read Card

        // Prepare key - all keys are set to FFFFFFFFFFFFh at chip delivery from the factory.
        MFRC522::MIFARE_Key key;
        for (byte i = 0; i < 6; i++)
            key.keyByte[i] = 0xFF;

        //some variables we need
        byte block;
        byte len;
        MFRC522::StatusCode status;

        //Serial.println(F("**Card Detected:**"));

        //-------------------------------------------

        //mfrc522.PICC_DumpDetailsToSerial(&(mfrc522.uid)); //dump some details about the card

        //Serial.print(F("IBAN: "));

        len = 18;

        //---------------------------------------- Read IBAN

        byte buffer2[18];
        block = 1;

        status = mfrc522.PCD_Authenticate(MFRC522::PICC_CMD_MF_AUTH_KEY_A, 1, &key, &(mfrc522.uid)); //line 834
        if (status != MFRC522::STATUS_OK)
        {
            //Serial.print(F("Authentication failed: "));
            //Serial.println(mfrc522.GetStatusCodeName(status));
            return;
        }

        status = mfrc522.MIFARE_Read(block, buffer2, &len);
        if (status != MFRC522::STATUS_OK)
        {
            //Serial.print(F("Reading failed: "));
            //Serial.println(mfrc522.GetStatusCodeName(status));
            return;
        }

        //PRINT IBAN
        for (uint8_t i = 0; i < 16; i++)
        {
            //Serial.write(buffer2[i]);
            sendArray[i] = buffer2[i];
        }

        //----------------------------------------

        //Serial.println(F("\n**End Reading**\n"));

        mfrc522.PICC_HaltA();
        mfrc522.PCD_StopCrypto1();

        blocked = true;
        digitalWrite(8, HIGH);
        delay(1);
        digitalWrite(8, LOW);
    }
}

void receiveEvent(int i)
{
    int receivePhase = 0;
    char wireReceive = Wire.read();
    char taskType = wireReceive;

    switch (taskType)
    {
        // reset vars
        case '+':
            resetTask = true;
            break;
        // print receipt
        case '*':
            while (0 < Wire.available()) {
                wireReceive = Wire.read();
                if (wireReceive == ',') {
                    receivePhase++;
                }
                else {
                    switch (receivePhase) {
                        case 1:
                            IBAN += wireReceive;
                            break;
                        case 2:
                            if (wireReceive == '.') {
                                printTask = true;
                                break;
                            }
                            withdrawal += wireReceive;
                            break;
                    }
                }
            }
            break;
        case '#':
            while (0 < Wire.available()) {
                wireReceive = Wire.read();
                if (wireReceive == '.') {
                    //dispenseTask = true;
                    //break;
                }
            }
    }
}

void requestEvent()
{
    digitalWrite(13, HIGH);
    //String info = card + inKey;
    //Serial.println("Wire data: ");
    //Serial.println(info);
    for (int i = 0; i < 17; i++)
    {
        Wire.write(sendArray[i]);
        //Serial.write(sendArray[i]);
    }
}

void printReceipt(String IBAN, String withdrawal)
{
    printer.wake();       // MUST wake() before printing again, even if reset
    printer.setDefault(); // Restore printer to defaults

    printer.justify('C');
    printer.setSize('L');
    printer.println(F("EVIL CORP. BANK"));
    printer.setSize('S');
    printer.println("Transaction receipt");

    printer.feed();
    printer.justify('L');
    printer.print(F("IBAN: "));
    printer.boldOn();
    printer.println(IBAN);
    printer.boldOff();
    printer.feed();

    printer.justify('L');
    printer.println(F("Withdrawal Amount:"));
    printer.justify('R');
    printer.boldOn();
    printer.print(F("E"));
    printer.print(withdrawal);
    printer.println(".00");
    printer.boldOff();

    printer.justify('C');
    printer.setSize('M');
    printer.feed();
    printer.println(F("Thank you for choosing Evil Corp"));
    printer.printBitmap(64, 64, elogo);
    printer.feed(4);

    printer.sleep(); // Tell printer to sleep
}

void dispenseBills(String e20)
{
    for (int i = e20.toInt(); i > 0; i--)
    {
        digitalWrite(A0, HIGH);
        delay(2000);
    }
    digitalWrite(A0, LOW);
}

void resetVars()
{
    blocked = false;
    for (int i = 0; i < 17; i++)
    {
        sendArray[i] = ' ';
    }
    inKey = ' ';
    IBAN = "";
    withdrawal = "";
    printAmount = "";
    e20 = "";
    delay(5000);
}

/*	FOR PROGRAMMING
// Prepare key - all keys are set to FFFFFFFFFFFFh at chip delivery from the factory.
MFRC522::MIFARE_Key key;
for (byte i = 0; i < 6; i++)
	key.keyByte[i] = 0xFF;
Serial.print(F("Card UID:")); //Dump UID
for (byte i = 0; i < mfrc522.uid.size; i++)
{
	Serial.print(mfrc522.uid.uidByte[i] < 0x10 ? " 0" : " ");
	Serial.print(mfrc522.uid.uidByte[i], HEX);
}
Serial.print(F(" PICC type: ")); // Dump PICC type
MFRC522::PICC_Type piccType = mfrc522.PICC_GetType(mfrc522.uid.sak);
Serial.println(mfrc522.PICC_GetTypeName(piccType));
byte buffer[34];
byte block;
MFRC522::StatusCode status;
byte len;
Serial.setTimeout(20000L); // wait until 20 seconds for input from serial
// Ask personal data: Family name
Serial.println(F("Type IBAN, ending with #"));
len = Serial.readBytesUntil('#', (char *)buffer, 30); // read family name from serial
for (byte i = len; i < 30; i++)
	buffer[i] = ' '; // pad with spaces
block = 1;
//Serial.println(F("Authenticating using key A..."));
status = mfrc522.PCD_Authenticate(MFRC522::PICC_CMD_MF_AUTH_KEY_A, block, &key, &(mfrc522.uid));
if (status != MFRC522::STATUS_OK)
{
	Serial.print(F("PCD_Authenticate() failed: "));
	Serial.println(mfrc522.GetStatusCodeName(status));
	return;
}
else
	Serial.println(F("PCD_Authenticate() success: "));
// Write block
status = mfrc522.MIFARE_Write(block, buffer, 16);
if (status != MFRC522::STATUS_OK)
{
	Serial.print(F("MIFARE_Write() failed: "));
	Serial.println(mfrc522.GetStatusCodeName(status));
	return;
}
else
	Serial.println(F("MIFARE_Write() success: "));
block = 2;
//Serial.println(F("Authenticating using key A..."));
status = mfrc522.PCD_Authenticate(MFRC522::PICC_CMD_MF_AUTH_KEY_A, block, &key, &(mfrc522.uid));
if (status != MFRC522::STATUS_OK)
{
	Serial.print(F("PCD_Authenticate() failed: "));
	Serial.println(mfrc522.GetStatusCodeName(status));
	return;
}
Serial.println(" ");
mfrc522.PICC_HaltA();	   // Halt PICC
mfrc522.PCD_StopCrypto1(); // Stop encryption on PCD
*/