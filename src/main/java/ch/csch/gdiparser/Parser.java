package ch.csch.gdiparser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Parser {

    private static final int headerLength = 15;

    public static String parse(byte[] messageBytes) {
        // Assemble into output
        String output = "";

        output += parseHeaderBytes(messageBytes);
        output += "\n\n";

        byte[] payloadBytes = getPayloadBytes(messageBytes);
        output += parsePayloadBytes(payloadBytes);
        output += "\n\n";

        return output;
    }

    /**
     * Extract and print all information contained in the message header
     *
     * @param messageBytes Header bytes
     * @return Printable representation
     */
    public static String parseHeaderBytes(byte[] messageBytes) {
        // Convert byte array to bytebuffer for easier access
        ByteBuffer byteBuffer = ByteBuffer.wrap(messageBytes);

        // 1 byte:		PacketCode (bitmask: start, start|end, middle, middle|end)
        byte packetCode = byteBuffer.get();
        List<String> packetFlags = getPacketFlags(packetCode);

        // 4 bytes:		object id (int)
        int objectId = byteBuffer.getInt();

        // 2 bytes:		auth token length (not used) => actually, it's just one byte. documentation is wrong^^
        byte authTokenLength = byteBuffer.get();

        // <64 kb:		auth token (not used)
        byte[] authToken = new byte[authTokenLength];
        byteBuffer.get(authToken);

        // 1 byte:		hmac length (currently only 0 or 32)
        byte hmacLength = byteBuffer.get();

        // 8 bytes:		utc time (long – when was this packed marshalled)
        long utcTime = byteBuffer.getLong();

        // 0/32 bytes:	hmac digest (not used in client / server, only server / server)
        byte[] hmacDigest = new byte[hmacLength];
        byteBuffer.get(hmacDigest);

        // ~128kb:		frame payload#
        // byte[] payload = new byte[byteBuffer.remaining()];
        // byteBuffer.get(payload);

        // Assemble into output
        String output = "";

        output += "HEADERS\n";
        output += "-------\n";
        output += "Packet Code:   " + packetCode + " (" + String.join(", ", packetFlags) + ")\n";
        output += "Object ID:     " + objectId + "\n";
        output += "Token Length:  " + authTokenLength + "\n";
        output += "Auth Token:    " + Arrays.toString(authToken) + "\n";
        output += "HMAC Length:   " + hmacLength + "\n";
        output += "UTC Timestamp: " + utcTime + "\n";
        output += "HMAC Digest:   " + Arrays.toString(hmacDigest) + "\n";

        return output;
    }

    /**
     * Print the payload in raw and decompressed form
     *
     * @param payload Payload bytes
     * @return Printable representation
     */
    public static String parsePayloadBytes(byte[] payload) {
        // Assemble into output
        StringBuilder output = new StringBuilder();

        output.append("PAYLOAD (RAW)\n");
        output.append("-------------\n");
        output.append(new String(payload, StandardCharsets.UTF_8)).append("\n");
        // output += Utils.bytesToHex(payload) + "\n";
        output.append("\n\n");

        output.append("PAYLOAD (DECOMPRESSED)\n");
        output.append("----------------------\n");
        byte[] decompressedPayload = decompressPayload(payload);
        String decompressedPayloadString = (new String(decompressedPayload, StandardCharsets.UTF_8));
        output.append(decompressedPayloadString).append("\n");
        // output.append(Utils.bytesToHex(decompressedPayload) + "\n");
        output.append("\n\n");

        output.append("PAYLOAD (EDITABLE)\n");
        output.append("------------------\n");
        String readablePayload = Utils.bytesToString(decompressedPayload);
        output.append(readablePayload).append("\n");

        return output.toString();
    }

    /**
     * Extract only the bytes which form the message header
     *
     * @param messageBytes Entire message
     * @return Header
     */
    public static byte[] getHeaderBytes(byte[] messageBytes) {
        byte[] headerBytes = new byte[headerLength];
        System.arraycopy(messageBytes, 0, headerBytes, 0, headerLength);
        return headerBytes;
    }

    /**
     * Extract only the bytes which form the message payload
     *
     * @param messageBytes Entire message
     * @return Payload
     */
    public static byte[] getPayloadBytes(byte[] messageBytes) {
        byte[] payloadBytes = new byte[messageBytes.length - headerLength];
        System.arraycopy(messageBytes, headerLength, payloadBytes, 0, messageBytes.length - headerLength);
        return payloadBytes;
    }


    private static List<String> getPacketFlags(byte packetCode) {
        final byte PACKET_START = 1;
        final byte PACKET_MIDDLE = 1 << 1;
        final byte PACKET_END = 1 << 2;
        final byte PACKET_CANCEL = (byte) (1 << 7);

        List<String> packetFlags = new ArrayList<>();

        if ((packetCode & PACKET_START) != 0) {
            packetFlags.add("PACKET_START");
        }
        if ((packetCode & PACKET_MIDDLE) != 0) {
            packetFlags.add("PACKET_MIDDLE");
        }
        if ((packetCode & PACKET_END) != 0) {
            packetFlags.add("PACKET_END");
        }
        if ((packetCode & PACKET_CANCEL) != 0) {
            packetFlags.add("PACKET_CANCEL");
        }

        return packetFlags;
    }

    public static byte[] decompressPayload(byte[] payload) {
        // thanks https://stackoverflow.com/questions/12531579/uncompress-a-gzip-string-in-java
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(payload);
            GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int res = 0;
            byte[] buf = new byte[32];
            while (res >= 0) {
                res = gzipInputStream.read(buf, 0, buf.length);
                if (res > 0) {
                    byteArrayOutputStream.write(buf, 0, res);
                }
            }
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error!".getBytes();
        }
    }

    public static byte[] compressPayload(byte[] payload) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            // Use compression level 3
            CompressingOutputStream gzipOutputStream = new CompressingOutputStream(byteArrayOutputStream, 3);
            gzipOutputStream.write(payload);
            gzipOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error!".getBytes();
        }
    }


    /**
     * Wrapper to use another compression level
     */
    public static final class CompressingOutputStream extends GZIPOutputStream {

        private CompressingOutputStream(OutputStream out, int compression) throws IOException {
            super(out, true);
            def.setLevel(compression);
        }

        static OutputStream of(OutputStream out, int compression) throws IOException {
            if (compression > 0) {
                return new CompressingOutputStream(out, compression);
            }
            return out;
        }
    }


}
