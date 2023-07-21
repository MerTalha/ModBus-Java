import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        byte b = 0x65; //hexadecimal address of the address you want to receive data from
        int a = receiveData(b);
        System.out.println(a);
    }

    public static int receiveData(byte adress){
        int value = 0;
        byte[] data = {
                0x00, 0x01, // transaction identifier
                0x00, 0x00, // Protocol Identifier (modbus)
                0x00, 0x06, // PDU length
                0x11,       // Address (17 decimals)
                0x03,       // read register command
                0x00, adress, // from the entered register
                0x00, 0x01  // Only 1 register (2 bytes)
        };

        byte[] rData = new byte[12];
        try (Socket socket = new Socket("192.168.1.5", 502)) {
            if (socket.isConnected()) {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(data);

                InputStream inputStream = socket.getInputStream();
                int bytesRead = inputStream.read(rData);

                if (bytesRead >= 9) {
                    int pduLength = rData[8] & 0xFF; // Read the PDU length field
                    int expectedDataLength = pduLength - 2; // Subtract 2 bytes of process identifier from PDU length

                    if (bytesRead >= expectedDataLength + 9) { // Check if the incoming data is greater than or equal to the expected data size
                        byte[] incomingValue = new byte[]{rData[10], rData[9]}; // Order changed
                        value = ((incomingValue[1] & 0xFF) << 8) | (incomingValue[0] & 0xFF);
                        System.out.println("Incoming Value: " + value);
                    } else {
                        System.out.println("Missing data received.");
                    }
                } else {
                    System.out.println("Data could not be read or missing data was received.");
                }
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return value;
    }
}
