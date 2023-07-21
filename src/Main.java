import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Main {
    public static void main(String[] args) {
        byte[] data = {
                0x00, 0x01, // işlem tanımlayıcı
                0x00, 0x00, // Protokol Tanımlaycı (modbus)
                0x00, 0x06, // PDU uzunluğu
                0x11,       // Adres (17 decimal)
                0x03,       // Register oku komutu
                0x00, 0x64, // 0 ıncı registerdan itibaren
                0x00, 0x01  // Sadece 1 register (2 byte)
        };

        byte[] rData = new byte[12]; // C#'daki uzunluğa uygun olarak artırıldı
        try (Socket socket = new Socket("192.168.1.5", 502)) {
            if (socket.isConnected()) {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(data);

                InputStream inputStream = socket.getInputStream();
                int bytesRead = inputStream.read(rData);

                if (bytesRead >= 9) {
                    int pduLength = rData[8] & 0xFF; // PDU uzunluğu alanını okuyun
                    int expectedDataLength = pduLength - 2; // PDU uzunluğundan 2 byte'lık işlem tanımlayıcıyı çıkarın

                    if (bytesRead >= expectedDataLength + 9) { // Gelen veri, beklenen veri boyutuna eşit veya daha büyük mü kontrol edin
                        byte[] gelenDeger = new byte[]{rData[10], rData[9]}; // Sırası değiştirildi
                        //short deger = (short) (ByteBuffer.wrap(gelenDeger).getShort() & 0xFFFF);
                        int deger = ((gelenDeger[1] & 0xFF) << 8) | (gelenDeger[0] & 0xFF);
                        System.out.println("Gelen Değer: " + deger);
                    } else {
                        System.out.println("Eksik veri alındı.");
                    }
                } else {
                    System.out.println("Veri okunamadı veya eksik veri alındı.");
                }
            }
        } catch (IOException e) {
            System.out.println("Hata: " + e.getMessage());
        }
    }
}
