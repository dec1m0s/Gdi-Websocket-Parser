package ch.csch.gdiparser;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;

/**
 * This class is the entry point for Burp.
 * It calls the method initialize() to load the components.
 */
@SuppressWarnings("unused")
public class Main implements BurpExtension {
    @Override
    public void initialize(MontoyaApi api)
    {
        api.extension().setName("Gdi Websocket Parser");

        api.userInterface().registerWebSocketMessageEditorProvider(new MessageEditorProvider(api));
        api.userInterface().registerWebSocketMessageEditorProvider(new MessageEditorProviderModifier(api));

        Logging logging = api.logging();

        // write a message to our output stream
        logging.logToOutput("Gdi Websocket Parser started.");

    }
}