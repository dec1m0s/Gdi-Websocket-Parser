package ch.csch.gdiparser;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.HexFormat;

public class Utils {
    /*
    public static byte[] serializeObject(Object object) {
        byte[] outputBytes;

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ContextSensitiveObjectOutputStream(out);
            oos.writeObject(object);
            oos.close();

            outputBytes = out.toByteArray();
            return outputBytes;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    */


    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : in) {
            builder.append(String.format("%02x ", b));
        }
        return builder.toString();
    }

    public static String bytesToString(byte[] input) {
        if (input == null) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : input) {
            int chr = b & 0xFF;
            if (32 <= b && b < 127) stringBuilder.append((char) chr);
            else stringBuilder.append(String.format("\\x%02x", b));
        }
        return stringBuilder.toString();
    }

    public static byte[] stringToBytes(String input) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        int i = 0;
        while (i < input.length()) {
            char c = input.charAt(i);
            // Check if this is a hexadecimal character of the form \xFF
            if (c == '\\' && (i + 3) < input.length() && input.charAt(i + 1) == 'x') {
                String hex = input.substring(i + 2, i + 4);
                byte[] hexByte = HexFormat.of().parseHex(hex);
                stream.write(hexByte[0]);
                i += 4;
            }
            // If not, then append it as a character
            else {
                stream.write((byte) c);
                i++;
            }
        }
        return stream.toByteArray();
    }
}
