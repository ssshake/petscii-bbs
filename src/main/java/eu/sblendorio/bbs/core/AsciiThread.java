package eu.sblendorio.bbs.core;

import java.io.IOException;
import java.net.Socket;

public abstract class AsciiThread extends BbsThread {

    @Override
    public BbsInputOutput buildIO(Socket socket) throws IOException {
        return new AsciiInputOutput(socket);
    }

    @Override
    public int getScreenColumns() {
        return 40;
    }

    @Override
    public int getScreenRows() {
        return 24;
    }

}
