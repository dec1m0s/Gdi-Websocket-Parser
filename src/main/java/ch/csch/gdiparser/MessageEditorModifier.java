package ch.csch.gdiparser;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.ui.Selection;
import burp.api.montoya.ui.contextmenu.WebSocketMessage;
import burp.api.montoya.ui.editor.EditorOptions;
import burp.api.montoya.ui.editor.RawEditor;
import burp.api.montoya.ui.editor.extension.EditorCreationContext;
import burp.api.montoya.ui.editor.extension.EditorMode;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedWebSocketMessageEditor;

import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Class to implement a tab in the Request view.
 * This is for the modifier view which parses and displays only the payload.
 */
public class MessageEditorModifier implements ExtensionProvidedWebSocketMessageEditor {
    final String tabName = "Gdi Network Frame (modify)";

    private final MontoyaApi api;
    private final RawEditor requestEditor;
    private ByteArray originalMessage;

    public MessageEditorModifier(MontoyaApi api, EditorCreationContext editorCreationContext) {
        this.api = api;

        // Set to read-only if this tab is open in a view that doesn't allow editing, such as the Proxy view.
        if (editorCreationContext.editorMode() == EditorMode.READ_ONLY)
        {
            requestEditor = api.userInterface().createRawEditor(EditorOptions.READ_ONLY, EditorOptions.WRAP_LINES);
        }
        else {
            requestEditor = api.userInterface().createRawEditor(EditorOptions.WRAP_LINES);
        }
    }

    /**
     * This method is called by Burp if the editor content was changed to apply the changes to the packet.
     * For example, the message was modified in the Repeater view.
     * It translates the content of the Editor view back into a packet.
     * @return message bytes to be sent
     */
    @Override
    public ByteArray getMessage() {
        if (requestEditor.isModified()) {
            // Get modified content
            ByteArray content = this.requestEditor.getContents();

            // Interpret as String
            String contentString = new String(content.getBytes(), StandardCharsets.UTF_8);

            // Convert string back into byte array
            byte[] payload = Utils.stringToBytes(contentString);

            // Compress bytes
            byte[] compressedPayload = Parser.compressPayload(payload);

            // Retrieve original header
            byte[] header = Parser.getHeaderBytes(originalMessage.getBytes());

            // Assemble packet
            ByteBuffer buffer = ByteBuffer.allocate(header.length + compressedPayload.length);
            buffer.put(header);
            buffer.put(compressedPayload);

            // Return
            return ByteArray.byteArray(buffer.array());
        } else {
            return originalMessage;
        }
    }

    /**
     * To display the tab, Burp calls this method to fill the editor field with custom content based on the packet.
     * This translates the packet into a form that can be modified without losing information.
     * @param webSocketMessage Input message
     */
    @Override
    public void setMessage(WebSocketMessage webSocketMessage) {
        // Save original message for later
        originalMessage = webSocketMessage.payload();

        // Get message bytes, extract payload, decompress and convert to readable form
        byte[] messageBytes = originalMessage.getBytes();
        byte[] payload = Parser.getPayloadBytes(messageBytes);
        byte[] payloadDecompressed = Parser.decompressPayload(payload);

        // Parse and write output editor field
        String payloadHex = Utils.bytesToString(payloadDecompressed);
        this.requestEditor.setContents(ByteArray.byteArray(payloadHex));
    }

    @Override
    public boolean isEnabledFor(WebSocketMessage webSocketMessage) {
        // Always enable for simplicity
        return true;
    }

    @Override
    public String caption() {
        return tabName;
    }

    @Override
    public Component uiComponent() {
        return requestEditor.uiComponent();
    }

    @Override
    public Selection selectedData() {
        return requestEditor.selection().isPresent() ? requestEditor.selection().get() : null;
    }

    @Override
    public boolean isModified() {
        // Tell Burp whether the message content was modified
        return requestEditor.isModified();
    }
}
