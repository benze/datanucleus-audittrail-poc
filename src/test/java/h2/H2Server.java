package h2;

import org.datanucleus.util.NucleusLogger;
import org.h2.tools.Server;

import java.sql.SQLException;

public class H2Server {

    private NucleusLogger CONSOLE = NucleusLogger.getLoggerInstance("Console");

    private static H2Server instance;

    static {
        try {
            boolean enableH2 = System.getProperty("enableH2") != null;
            if( enableH2 )
                instance = new H2Server();
            else
                instance = null;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private Server h2Server;
    private Server tcpServer;

    protected H2Server() throws SQLException {
        h2Server = Server.createWebServer();
        tcpServer = Server.createTcpServer();
        CONSOLE.info("H2: " + h2Server.getURL());
        CONSOLE.info("H2 Status: " + h2Server.getStatus());
        if (!h2Server.isRunning(false))
            h2Server.start();
    }

    static public H2Server getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return "H2Server{" +
                "h2Server=" + h2Server.getURL() +
                " :: status=" + h2Server.getStatus() +
                '}';
    }
}
