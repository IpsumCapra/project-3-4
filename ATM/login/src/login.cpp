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
byte rowPins[ROWS] = {7, 6, 4, 5};	 //connect to the row pinouts of the keypad
byte colPins[COLS] = {3, 2, A4, A5}; //connect to the column pinouts of the keypad

//initialize an instance of class NewKeypad
Keypad keypad = Keypad(makeKeymap(hexaKeys), rowPins, colPins, ROWS, COLS);

//leds
int redPin = A0;
int greenPin = A1;

//vars
String cards[3] = {"CA 4A F5 0B", "96 B6 69 32", "C6 93 B8 32"};
int cardNr;
bool cardAccess = false;
String pin[3] = {"1234", "0000", "1515"};
int number = 0;
String code;
char key = 'x';

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

	key = keypad.getKey();
	if (key)
	{
		Serial.println(key);

		if (key == '#')
		{
			// Prepare key - all keys are set to FFFFFFFFFFFFh at chip delivery from the factory.
			MFRC522::MIFARE_Key key;
			for (byte i = 0; i < 6; i++)
				key.keyByte[i] = 0xFF;

			// Reset the loop if no new card present on the sensor/reader. This saves the entire process when idle.
			if (!mfrc522.PICC_IsNewCardPresent())
			{
				return;
			}

			// Select one of the cards
			if (!mfrc522.PICC_ReadCardSerial())
			{
				return;
			}

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
			Serial.println(F("Type Family name, ending with #"));
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

			// Write block
			status = mfrc522.MIFARE_Write(block, &buffer[16], 16);
			if (status != MFRC522::STATUS_OK)
			{
				Serial.print(F("MIFARE_Write() failed: "));
				Serial.println(mfrc522.GetStatusCodeName(status));
				return;
			}
			else
				Serial.println(F("MIFARE_Write() success: "));

			// Ask personal data: First name
			Serial.println(F("Type First name, ending with #"));
			len = Serial.readBytesUntil('#', (char *)buffer, 20); // read first name from serial
			for (byte i = len; i < 20; i++)
				buffer[i] = ' '; // pad with spaces

			block = 4;
			//Serial.println(F("Authenticating using key A..."));
			status = mfrc522.PCD_Authenticate(MFRC522::PICC_CMD_MF_AUTH_KEY_A, block, &key, &(mfrc522.uid));
			if (status != MFRC522::STATUS_OK)
			{
				Serial.print(F("PCD_Authenticate() failed: "));
				Serial.println(mfrc522.GetStatusCodeName(status));
				return;
			}

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

			block = 5;
			//Serial.println(F("Authenticating using key A..."));
			status = mfrc522.PCD_Authenticate(MFRC522::PICC_CMD_MF_AUTH_KEY_A, block, &key, &(mfrc522.uid));
			if (status != MFRC522::STATUS_OK)
			{
				Serial.print(F("PCD_Authenticate() failed: "));
				Serial.println(mfrc522.GetStatusCodeName(status));
				return;
			}

			// Write block
			status = mfrc522.MIFARE_Write(block, &buffer[16], 16);
			if (status != MFRC522::STATUS_OK)
			{
				Serial.print(F("MIFARE_Write() failed: "));
				Serial.println(mfrc522.GetStatusCodeName(status));
				return;
			}
			else
				Serial.println(F("MIFARE_Write() success: "));

			Serial.println(" ");
			mfrc522.PICC_HaltA();	   // Halt PICC
			mfrc522.PCD_StopCrypto1(); // Stop encryption on PCD
		}

		else if ((key == '*'))
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
						Serial.println("Authorized access");
						Serial.println();
						analogWrite(greenPin, 255);
						delay(300);
						analogWrite(greenPin, 0);
						delay(300);
						analogWrite(greenPin, 255);
						delay(300);
						analogWrite(greenPin, 0);
						cardNr = i;
						cardAccess = true;
						break;
					}
				}
			}

			//-------------------------------------KEYPAD LOGIN--------------------------------------------//.

			if (cardAccess)
			{
				char key2;
				// correct ping
				if (number >= 4 && code == pin[cardNr])
				{
					Serial.println("Logged into ATM");
					number = 0;
					code = "";
					cardAccess = false;
					analogWrite(greenPin, 255);
					delay(3000);
					analogWrite(greenPin, 0);
					return;
				} // Incorrect ping
				else if (number >= 4)
				{
					Serial.println("Wrong pin");
					number = 0;
					code = "";
					cardAccess = false;
					analogWrite(redPin, 255);
					delay(3000);
					analogWrite(redPin, 0);
					return;
				}

				key2 = keypad.getKey();
				if (key2)
				{ // add key to total code
					code += key2;
					number++;
					Serial.print("key: ");
					Serial.println(key2);
					Serial.print("code: ");
					Serial.println(code);
					Serial.print("nr. : ");
					Serial.println(number);
				}
			}
		}

		else if (key == '0')
		{
			// Prepare key - all keys are set to FFFFFFFFFFFFh at chip delivery from the factory.
			MFRC522::MIFARE_Key key;
			for (byte i = 0; i < 6; i++)
				key.keyByte[i] = 0xFF;

			//some variables we need
			byte block;
			byte len;
			MFRC522::StatusCode status;

			//-------------------------------------------

			// Reset the loop if no new card present on the sensor/reader. This saves the entire process when idle.
			if (!mfrc522.PICC_IsNewCardPresent())
			{
				return;
			}

			// Select one of the cards
			if (!mfrc522.PICC_ReadCardSerial())
			{
				return;
			}

			Serial.println(F("**Card Detected:**"));

			//-------------------------------------------

			mfrc522.PICC_DumpDetailsToSerial(&(mfrc522.uid)); //dump some details about the card

			//mfrc522.PICC_DumpToSerial(&(mfrc522.uid));      //uncomment this to see all blocks in hex

			//-------------------------------------------

			Serial.print(F("Name: "));

			byte buffer1[18];

			block = 4;
			len = 18;

			//------------------------------------------- GET FIRST NAME
			status = mfrc522.PCD_Authenticate(MFRC522::PICC_CMD_MF_AUTH_KEY_A, 4, &key, &(mfrc522.uid)); //line 834 of MFRC522.cpp file
			if (status != MFRC522::STATUS_OK)
			{
				Serial.print(F("Authentication failed: "));
				Serial.println(mfrc522.GetStatusCodeName(status));
				return;
			}

			status = mfrc522.MIFARE_Read(block, buffer1, &len);
			if (status != MFRC522::STATUS_OK)
			{
				Serial.print(F("Reading failed: "));
				Serial.println(mfrc522.GetStatusCodeName(status));
				return;
			}

			//PRINT FIRST NAME
			for (uint8_t i = 0; i < 16; i++)
			{
				if (buffer1[i] != 32)
				{
					Serial.write(buffer1[i]);
				}
			}
			Serial.print(" ");

			//---------------------------------------- GET LAST NAME

			byte buffer2[18];
			block = 1;

			status = mfrc522.PCD_Authenticate(MFRC522::PICC_CMD_MF_AUTH_KEY_A, 1, &key, &(mfrc522.uid)); //line 834
			if (status != MFRC522::STATUS_OK)
			{
				Serial.print(F("Authentication failed: "));
				Serial.println(mfrc522.GetStatusCodeName(status));
				return;
			}

			status = mfrc522.MIFARE_Read(block, buffer2, &len);
			if (status != MFRC522::STATUS_OK)
			{
				Serial.print(F("Reading failed: "));
				Serial.println(mfrc522.GetStatusCodeName(status));
				return;
			}

			//PRINT LAST NAME
			for (uint8_t i = 0; i < 16; i++)
			{
				Serial.write(buffer2[i]);
			}

			//----------------------------------------

			Serial.println(F("\n**End Reading**\n"));

			delay(1000); //change value if you want to read cards faster

			mfrc522.PICC_HaltA();
			mfrc522.PCD_StopCrypto1();
		}
	}
}