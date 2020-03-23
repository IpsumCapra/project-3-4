//rfid setup
#include <SPI.h>
#include <MFRC522.h>
#include <Keypad.h>

#define SS_PIN 10
#define RST_PIN 9
MFRC522 mfrc522(SS_PIN, RST_PIN); // Create MFRC522 instance.

//keypad setup
const byte ROWS = 4; //four rows
const byte COLS = 4; //four columns
//define the cymbols on the buttons of the keypads
char hexaKeys[ROWS][COLS] = {
	{'1', '2', '3', 'A'},
	{'4', '5', '6', 'B'},
	{'7', '8', '9', 'C'},
	{'*', '0', '#', 'D'}};
byte rowPins[ROWS] = {7, 6, 4, 5};   //connect to the row pinouts of the keypad
byte colPins[COLS] = {3, 2, A4, A5}; //connect to the column pinouts of the keypad

//initialize an instance of class NewKeypad
Keypad keypad = Keypad(makeKeymap(hexaKeys), rowPins, colPins, ROWS, COLS);

//leds
int redPin = A0;
int greenPin = A1;

//vars
String cards[3] = {"CA 4A F5 0B", "96 B6 69 32", "C6 93 B8 32"};
String pin[3] = {"1234", "0000", "1515"};
int pinAttempts[3] = {0, 0, 0};
int cardNr;
bool cardAccess = false;
int number = 0;
String code;

void blink ( int pin, int time, int repeats);

void setup()
{
	Serial.begin(9600); // Initiate a serial communication
	SPI.begin();		// Initiate  SPI bus
	mfrc522.PCD_Init(); // Initiate MFRC522
	Serial.println("Approximate your card to the reader...");
	Serial.println();
}
void loop()
{

	if (!cardAccess)
	{
		//-------------------------------------CARD READING--------------------------------------------//
		// Look for new cards
		if (!mfrc522.PICC_IsNewCardPresent())
		{
			return;
		} // Select one of the cards
		if (!mfrc522.PICC_ReadCardSerial())
		{
			return;
		}

		//Show UID on serial monitor
		Serial.print("UID tag :");
		String content = "";
		byte letter;

		for (byte i = 0; i < mfrc522.uid.size; i++)
		{
			Serial.print(mfrc522.uid.uidByte[i] < 0x10 ? " 0" : " ");
			Serial.print(mfrc522.uid.uidByte[i], HEX);
			content.concat(String(mfrc522.uid.uidByte[i] < 0x10 ? " 0" : " "));
			content.concat(String(mfrc522.uid.uidByte[i], HEX));
		}

		Serial.println();
		Serial.print("Message : ");
		content.toUpperCase();
		int len = sizeof(cards) / sizeof(cards[0]);
		for (int i = 0; i < len; i++)
		{
			if (content.substring(1) == cards[i]) // scan for card
			{
				if (pinAttempts[i] >= 3) {
					Serial.println("This card is blocked");
					return blink(redPin, 200, 3);
					}
				
				Serial.println("Authorized access");
				Serial.println();
				
				cardNr = i;
				cardAccess = true;
				return blink(greenPin, 200, 2);
			}
			
		}
		Serial.println("Card not found");
		Serial.println();
		blink(redPin, 500, 2);

	}

	//-------------------------------------KEYPAD LOGIN--------------------------------------------//.

	if (cardAccess)
	{
		
		char key;
		// correct ping
		if (number >= 4 && code == pin[cardNr])
		{
			Serial.println("Logged into ATM");
			pinAttempts[cardNr] = 0;
			number = 0;
			code = "";
			cardAccess = false;
			return blink(greenPin, 1000, 1);
		} // Incorrect ping
		else if (number >= 4)
		{
			Serial.println("Wrong pin attempt: ");
			pinAttempts[cardNr]++;
			Serial.println(pinAttempts[cardNr]);
			number = 0;
			code = "";
			cardAccess = false;
			return blink(redPin, 1000, 1);
		}

		key = keypad.getKey();
		if (key)
		{ // add key to total code
			code += key;
			number++;
			Serial.print("key: ");
			Serial.println(key);
			Serial.print("code: ");
			Serial.println(code);
			Serial.print("nr. : ");
			Serial.println(number);
		}
	}
}

void blink ( int pin, int time, int repeats) {
	for (int i = 0; i < repeats; i++) {
		analogWrite(pin, 255);
		delay(time);
		analogWrite(pin, 0);
		delay(time);
	}
	return;
}