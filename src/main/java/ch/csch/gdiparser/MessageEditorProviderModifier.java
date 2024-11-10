package ch.csch.gdiparser;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.ui.editor.extension.EditorCreationContext;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedWebSocketMessageEditor;
import burp.api.montoya.ui.editor.extension.WebSocketMessageEditorProvider;

/**
 * Helper class to provide the Editor components to Burp.
 * This is for the modifier view which parses and displays only the payload.
 */
public class MessageEditorProviderModifier implements WebSocketMessageEditorProvider {
    private final MontoyaApi api;

    MessageEditorProviderModifier(MontoyaApi api) {
        this.api = api;
    }

    @Override
    public ExtensionProvidedWebSocketMessageEditor provideMessageEditor(EditorCreationContext editorCreationContext) {
        return new MessageEditorModifier(api, editorCreationContext);
    }
}
