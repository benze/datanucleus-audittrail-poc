package h2;

import org.datanucleus.util.NucleusLogger;
import org.h2.tools.Server;

import java.sql.SQLException;

public class H2Server {

    private NucleusLogger CONSOLE = NucleusLogger.getLoggerInstance("Console");

    private static H2Server instance;

    static {
        try {
            instance = new H2Server();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private Server webServer;
    private Server tcpServer;

    protected H2Server() throws SQLException {
        webServer = Server.createWebServer();
        tcpServer = Server.createTcpServer();
        CONSOLE.info("H2: " + webServer.getURL());
        CONSOLE.info("H2 Status: " + webServer.getStatus());
        if (!webServer.isRunning(false))
            webServer.start();
        if( !tcpServer.isRunning(false))
            tcpServer.start();
    }

    static public H2Server getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return "H2Server{" +
                "webServer=" + webServer.getStatus() +
                " :: tcpServer =" + tcpServer.getStatus() +
                '}';
    }
}
