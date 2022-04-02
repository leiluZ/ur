package ur.multithread.exec;

import io.grpc.internal.JsonParser;
import io.pravega.client.ClientConfig;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.stream.EventStreamWriter;
import io.pravega.client.stream.EventWriterConfig;
import io.pravega.client.stream.ScalingPolicy;
import io.pravega.client.stream.StreamConfiguration;
import io.pravega.client.stream.impl.UTF8StringSerializer;
import io.pravega.client.admin.StreamManager;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;


class Writer implements Runnable {
    private final String scope;
    private final String streamName;
    private final URI controllerURI;
    private final DataSource<String> dataSource;

    public Writer(String scope, String streamName, URI controllerURI, DataSource<String> dataSource) {
        this.scope = scope;
        this.streamName = streamName;
        this.controllerURI = controllerURI;
        this.dataSource = dataSource;
    }

    public void start() {
        System.out.format("Start to create Pravega writer for stream %s:%s through endpoint %s\n",
                scope, streamName, controllerURI);
        try (EventStreamClientFactory clientFactory = EventStreamClientFactory.withScope(scope,
                ClientConfig.builder().controllerURI(controllerURI).build());
             EventStreamWriter<String> writer = clientFactory.createEventWriter(streamName,
                     new UTF8StringSerializer(),
                     EventWriterConfig.builder().build())) {

            while (true) {
                // for each line in the data file, verify it's JSON
                String event = dataSource.next();
                if (event == null || event.isEmpty()) {
                    // end of data source
                    System.out.format("[%s] exit\n", Thread.currentThread());
                    break;
                }

                String routineKey = event.split(",")[0];

                writer.writeEvent(routineKey, event);
                // write to Pravega streams
                System.out.format("%s Writing message: '%s' to stream '%s / %s'%n", Thread.currentThread(), event, scope, streamName);
            }
        } catch (Exception e) {
            System.out.format("error happened when writing data\n");
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        start();
    }
}

public class PravegaWriter {
    public static void main(String[] args) {
        Options options = getOptions();
        CommandLine cmd = null;
        try {
            cmd = parseCommandLineArgs(options, args);
        } catch (ParseException e) {
            System.out.format("%s.%n", e.getMessage());
            printHelp(1);
        }

        final String scope = cmd.getOptionValue("scope") == null ? Constants.DEFAULT_SCOPE : cmd.getOptionValue("scope");
        final String streamName = cmd.getOptionValue("name") == null ? Constants.DEFAULT_STREAM_NAME : cmd.getOptionValue("name");
        final String uriString = cmd.getOptionValue("uri") == null ? Constants.DEFAULT_CONTROLLER_URI : cmd.getOptionValue("uri");
        final String dataFilename = cmd.getOptionValue("file");
        final int threadNum = Integer.parseInt(cmd.getOptionValue("thread"));
        final boolean isHelp = cmd.hasOption("help");
        if (isHelp) {
            printHelp(0);
        }

        final URI controllerURI = URI.create(uriString);
        if (dataFilename == null || dataFilename.isEmpty()
                || !Files.exists(Paths.get(dataFilename))) {
            System.out.format("data file: %s doesn't exist", dataFilename);
            System.exit(2);
        }
        createScopeAndStream(scope, streamName, controllerURI);
        FileDataSource fileDataSource = null;
        try {
            fileDataSource = new FileDataSource(dataFilename);
        } catch (Exception e) {
            System.out.format("failed to create data source for: %s\n", dataFilename);
            e.printStackTrace();
            System.exit(3);
        }

        System.out.format("start the writers\n");
        ArrayList<Thread> threads = new ArrayList<>(threadNum);
        for (int i = 0; i < threadNum; i++) {
            Thread thread = new Thread(new Writer(scope, streamName, controllerURI, fileDataSource));
            thread.start();
            threads.add(thread);
            System.out.format("writer %dth started\n", i + 1);
        }
        try {
            //wait for the stop of the all writers
            for (Thread thread : threads) {
                thread.join();
            }
            System.out.println("main thread exit");
        } catch (InterruptedException e) {
            System.out.println("main thread be interrupted");
            e.printStackTrace();
            System.exit(4);
        } finally {
            try {
                fileDataSource.close();
            } catch (IOException e) {
                System.out.println("failed to close the data source");
                e.printStackTrace();
            }
        }

    }

    private static void createScopeAndStream(String scope, String streamName, URI controllerURI) {
        // init Pravega writer
        StreamManager streamManager = StreamManager.create(controllerURI);
        /*final boolean scopeIsNew = */
        streamManager.createScope(scope);
        StreamConfiguration streamConfig = StreamConfiguration.builder()
                .scalingPolicy(ScalingPolicy.fixed(3/*hardcoded*/)).build();

        System.out.format("Start to create Pravega stream %s:%s through endpoint %s",
                scope, streamName, controllerURI);
        /*final boolean streamIsNew = */
        streamManager.createStream(scope, streamName, streamConfig);

        streamManager.close();
    }

    private static void printHelp(int errCode) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("PravegaWriter", getOptions());
        System.exit(errCode);
    }

    private static Options getOptions() {
        final Options options = new Options();
        options.addOption("s", "scope", true, "The scope name of the stream to read from.");
        options.addOption("n", "name", true, "The name of the stream to read from.");
        options.addOption("u", "uri", true, "The URI to the controller in the form tcp://host:port");
        options.addOption("f", "file", true, "The filename of the sample data");
        options.addOption("h", "help", false, "help");
        options.addOption("t", "thread", true, "thread number for writers");
        return options;
    }

    private static CommandLine parseCommandLineArgs(Options options, String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        return cmd;
    }
}
