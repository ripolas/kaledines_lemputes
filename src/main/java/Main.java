import com.fazecast.jSerialComm.SerialPort;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import static spark.Spark.*;

public class Main {
    public static enum Mode {
        KAS_ANTRA
    }
    public static ArrayList<Color> leds = new ArrayList<>();
    public static final int LED_COUNT = 118;
    public static SerialPort serialPort;
    public static final String arduinoPort = "/dev/ttyACM0"; //"COM6" for windows.
    public static OutputStream arduinoOutputStream;
    public static void main(String[] args) throws IOException {
        staticFileLocation("public");
        port(8443);
        String keystorePath = "/home/ripolas/Downloads/keystore.jks";
        String filePath = "/home/ripolas/Downloads/password.txt";
        String keystorePassword = "";
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            keystorePassword = br.readLine();
        } catch (IOException ignored) {
        }
        secure(keystorePath, keystorePassword, null, null);
        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        });
        Mode mode = Mode.KAS_ANTRA;
        for(int i = 0;i<LED_COUNT;i++){
            leds.add(new Color(255, 255, 0));
        }
        // setup for the arduino sending thing
        System.out.println("Available Ports:");
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort port : ports) {
            System.out.println(port.getSystemPortName() + " - " + port.getDescriptivePortName());
        }
        serialPort = SerialPort.getCommPort(arduinoPort);
        serialPort.setComPortParameters(1000000, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
        arduinoOutputStream = serialPort.getOutputStream();
        if (serialPort.openPort()) {
            while (true) {
                update_LEDS();
            }
        }else{
            System.out.println("failed to open port");
        }
    }
    public static void update_LEDS() throws IOException {
        for(int i = 0;i<leds.size();i++){
            float[] rgb_vals = leds.get(i).getRGBComponents(null);
            float[] hsb_vals = Color.RGBtoHSB((int) rgb_vals[0], (int) rgb_vals[1], (int) rgb_vals[2],null);
            arduinoOutputStream.write(i);
            arduinoOutputStream.write((int) hsb_vals[0]);
            arduinoOutputStream.write((int) hsb_vals[1]);
            arduinoOutputStream.write((int) hsb_vals[2]);
        }
        arduinoOutputStream.write(243);
        arduinoOutputStream.write(255);
        arduinoOutputStream.write(255);
        arduinoOutputStream.write(255);
    }
}