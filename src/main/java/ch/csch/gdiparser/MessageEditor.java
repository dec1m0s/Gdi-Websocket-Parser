package ch.csch.gdiparser;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.ui.Selection;
import burp.api.montoya.ui.contextmenu.WebSocketMessage;
import burp.api.montoya.ui.editor.EditorOptions;
import burp.api.montoya.ui.editor.RawEditor;
import burp.api.montoya.ui.editor.extension.EditorCreationContext;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedWebSocketMessageEditor;

import java.awt.*;
import java.nio.ByteBuffer;

/**
 * Class to implement a tab in the Request view.
 * This is for the read-only view which parses and displays the entire packet.
 */
public class MessageEditor implements ExtensionProvidedWebSocketMessageEditor {
    final String tabName = "Gdi Network Frame";

    private final MontoyaApi api;
    private final RawEditor requestEditor;


    public MessageEditor(MontoyaApi api, EditorCreationContext editorCreationContext) {
        this.api = api;
        requestEditor = api.userInterface().createRawEditor(EditorOptions.READ_ONLY);
    }

    @Override
    public ByteArray getMessage() {
        // This will never return anything because the editor is set to read-only.
        return ByteArray.byteArray("test");
    }

    /**
     * To display the tab, Burp calls this method to fill the editor field with custom content based on the packet.
     * This translates the packet into a somewhat readable form.
     * @param webSocketMessage Input message
     */
    @Override
    public void setMessage(WebSocketMessage webSocketMessage) {
        // Get message payload
        ByteArray data = webSocketMessage.payload();

        // Parse bytes and write result to the Editor view
        String output = Parser.parse(data.getBytes());
        this.requestEditor.setContents(ByteArray.byteArray(output));
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
        return null;
    }


    @Override
    public boolean isModified() {
        // Tab is set to read-only, so it cannot be modified.
        return false;
    }
}
