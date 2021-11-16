package h2;

import org.datanucleus.util.NucleusLogger;
import org.h2.tools.Server;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class H2Server {

    private static H2Server instance = new H2Server();

    private NucleusLogger CONSOLE = NucleusLogger.getLoggerInstance("Console");
    private Server h2Server;
    private Server tcpServer;

    protected H2Server(){
        CompletableFuture.runAsync(() -> {
            try {
                h2Server = Server.createWebServer();
                if (!h2Server.isRunning(false)) {
                    h2Server.start();
                }
            } catch (SQLException throwables) {
                CONSOLE.error("Unable to start the H2 WebServer");
                CONSOLE.debug(throwables);
            }

            try {
                tcpServer = Server.createTcpServer();
                if (!tcpServer.isRunning(false)) {
                    tcpServer.start();
                }
            } catch (SQLException throwables) {
                CONSOLE.error("Unable to start the H2 TcpServer");
                CONSOLE.debug(throwables);
            }

            CONSOLE.info("H2 Web Status: " + h2Server.getStatus());
            CONSOLE.info("H2 Tcp Status: " + tcpServer.getStatus());
        });
    }

    static public H2Server getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        StringBuffer status = new StringBuffer();
        if( h2Server != null ){
            status.append("H2WebServer{" +
                    "h2Server=" + h2Server.getURL() +
                    " :: status=" + h2Server.getStatus() +
                    '}');
        }
        if( tcpServer != null ){
            status.append("H2TcpServer{" +
                    "tcpServer=" + tcpServer.getURL() +
                    " :: status=" + tcpServer.getStatus() +
                    '}');
        }

        return status.toString();
    }
}
