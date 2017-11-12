package conTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kivancmuslu.www.concurrent.BoolConditional;

public class CDTServer implements Runnable
{
    private @Nullable ServerSocket serverSocket_;
    private volatile int port_;

    private final BoolConditional portNumberAssignedCondition_;

    private final ExecutorService serverWorkers_;
    private volatile boolean alive_;

    static boolean DEBUG_SERVER = false;

    CDTServer(int noServerThreads)
    {
        alive_ = true;
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull ExecutorService workers = Executors.newFixedThreadPool(noServerThreads);
        serverWorkers_ = workers;
        port_ = -1;
        serverSocket_ = null;

        portNumberAssignedCondition_ = new BoolConditional(false);
    }

    public int getPort()
    {
        return port_;
    }

    @Override
    public void run()
    {
        try
        {
            ServerSocket serverSocket = new ServerSocket(0);
            serverSocket_ = serverSocket;
            port_ = serverSocket.getLocalPort();

            // Signal that the server assigned a port.
            portNumberAssignedCondition_.signalAll();
            CDTBase.getInstance().logInfo("Server logger accepting clients on port: " + port_);

            while (alive_)
            {
                try
                {
                    if (DEBUG_SERVER)
                        CDTBase.getInstance().logInfo("Waiting for a client to connect...");
                    // Socket is closed by the underlying thread when the connection is completed.
                    // Suppressed due to missing library annotations.
                    @SuppressWarnings({"resource", "null"}) @NonNull Socket clientSocket = serverSocket.accept();
                    CDTServerWorker serverWorker = new CDTServerWorker(clientSocket);
                    serverWorkers_.execute(serverWorker);
                }
                catch (IOException e)
                {
                    if (alive_)
                        CDTBase.getInstance().logWarning("Cannot accept client.", e);
                }
            }
        }
        catch (IOException e)
        {
            CDTBase.getInstance().logWarning("Cannot create CDT server.", e);
            e.printStackTrace();
        }
        finally
        {
            serverWorkers_.shutdown();
            try
            {
                boolean result = serverWorkers_.awaitTermination(1, TimeUnit.MINUTES);
                if (!result)
                    CDTBase.getInstance()
                           .logWarning("Cannot terminate CDT server workers normally.");
            }
            catch (InterruptedException e)
            {
                CDTBase.getInstance().logWarning("Cannot terminate CDT server workers.", e);
            }
        }
    }

    public void kill()
    {
        alive_ = false;
        try
        {
            ServerSocket serverSocket = serverSocket_;
            if (serverSocket != null)
                serverSocket.close();
        }
        catch (IOException e)
        {
            CDTBase.getInstance().logWarning("Cannot close CDT server socket.", e);
        }
    }

    private class CDTServerWorker implements Runnable
    {
        private final Socket socket_;
        private final BufferedReader reader_;

        CDTServerWorker(Socket socket) throws IOException
        {
            socket_ = socket;
            reader_ = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        @Override
        public void run()
        {
            try
            {
                readAllMessages();
            }
            catch (IOException e)
            {
                CDTBase.getInstance().logWarning(logPrefix() + "Cannot read incoming messages.", e);
            }
            finally
            {
                try
                {
                    reader_.close();
                }
                catch (IOException e)
                {
                    // We don't care if it fails at this moment, just log.
                    CDTBase.getInstance()
                           .logInfo(logPrefix() + "Cannot close the input stream of the socket.", e);
                }

                try
                {
                    socket_.close();
                }
                catch (IOException e)
                {
                    // We don't care if it fails at this moment, just log.
                    CDTBase.getInstance().logInfo(logPrefix() + "Cannot close the socket.", e);
                }
//                CDTBase.getInstance().logInfo(logPrefix() + "Completed successfully.");
            }
        }

        private void readAllMessages() throws IOException
        {
            if (DEBUG_SERVER)
                CDTBase.getInstance().logInfo("Client connected, reading messages...");
            String message = null;
            do
            {
                message = reader_.readLine();
                if (message != null)
                {
                    try
                    {
                        // Suppressed due to missing library annotations.
                        @SuppressWarnings({"null"}) @NonNull HashMap<String, Object> triggerData = new ObjectMapper().readValue(message,
                                                                                                                                HashMap.class);
                        CDTBase.getInstance().triggerActivated(triggerData);
                    }
                    catch (Throwable e)
                    {
                        CDTBase.getInstance()
                               .logWarning("Cannot parse trigger data: " + message, e);
                    }
                }
            } while (message != null);
//            CDTBase.getInstance().logInfo(logPrefix() + "Completed reading messages from client.");
        }

        private String logPrefix()
        {
            return "CDT server listener: ";
        }
    }

    public void waitUntilStarted() throws InterruptedException
    {
        portNumberAssignedCondition_.await();
    }
}
