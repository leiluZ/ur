package ur.multithread.exec;

import io.pravega.client.ClientConfig;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.admin.ReaderGroupManager;
import io.pravega.client.stream.*;
import io.pravega.client.stream.impl.UTF8StringSerializer;
import org.apache.commons.cli.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.UUID;

class Reader implements Runnable {
    private static final int READER_TIMEOUT_MS = 1000000;

    public final String scope;
    public final String streamName;
    public final URI controllerURI;
    private String readerGroup;
    private String readerName;
    private int readInterval;

    public Reader(String scope, String streamName, String readerGroup, int readInterval, URI controllerURI) {
        this.scope = scope;
        this.streamName = streamName;
        this.controllerURI = controllerURI;
        this.readerGroup = readerGroup;
        this.readerName = UUID.randomUUID().toString().replace("-", "");
        this.readInterval = readInterval;
    }

    public void start() {
        try {
            try (EventStreamClientFactory clientFactory = EventStreamClientFactory.withScope(scope,
                    ClientConfig.builder().controllerURI(controllerURI).build());
                 EventStreamReader<String> reader = clientFactory.createReader(this.readerName,
                         readerGroup,
                         new UTF8StringSerializer(),
                         ReaderConfig.builder().build())) {

                System.out.format("Start to read all the events from %s/%s with dump interval %d second(s)\n",
                        scope, streamName, this.readInterval);
                EventRead<String> event = null;
                do {
                    try {
                        event = reader.readNextEvent(READER_TIMEOUT_MS);
                        if (event.getEvent() != null) {
                            System.out.format("%s Read event '%s'\n", Thread.currentThread(), event.getEvent());
                        }
                    } catch (ReinitializationRequiredException e) {
                        //There are certain circumstances where the reader needs to be reinitialized
                        e.printStackTrace();
                    }
                    Thread.sleep(this.readInterval * 1000 /* milliseconds -> seconds */);
                } while (event.getEvent() != null);
                System.out.format("%s No more events from %s/%s\n", Thread.currentThread(), scope, streamName);
            }
        } catch (Exception e) {
            System.out.format("Unable to read Pravega stream\n", e);
        }
    }

    @Override
    public void run() {
        start();
    }
}

public class PravegaReader {
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
        final String intervalString = cmd.getOptionValue("interval");
        final int threadNum = Integer.parseInt(cmd.getOptionValue("thread"));
        final boolean isHelp = cmd.hasOption("help");
        System.out.println("scope = " + scope);
        System.out.println("streamName = " + streamName);
        System.out.println("uriString = " + uriString);
        if (isHelp) {
            printHelp(0);
        }

        final URI controllerURI = URI.create(uriString);
        int interval = 0;
        if (intervalString != null && !intervalString.isEmpty()) {
            interval = Integer.valueOf(intervalString);
        }
        final String readerGroup = UUID.randomUUID().toString().replace("-", "");
        createReaderGroup(scope, streamName, readerGroup, controllerURI);

        System.out.format("start the readers\n");
        ArrayList<Thread> threads = new ArrayList<>(threadNum);
        for (int i = 0; i < threadNum; i++) {
            Thread thread = new Thread(new Reader(scope, streamName, readerGroup, interval, controllerURI));
            thread.start();
            threads.add(thread);
            System.out.format("readers %dth started\n", i + 1);
        }

        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            System.out.format("main thread be interrupted\n");
            e.printStackTrace();
            System.exit(4);
        } finally {
            releaseReaderGroup(scope, readerGroup, controllerURI);
        }

    }

    private static void createReaderGroup(String scope, String streamName, String readerGroup, URI controllerURI) {
        // start to create reader group
        System.out.format("Start to create Pravega ReaderGroup for stream %s:%s through endpoint %s",
                scope, streamName, controllerURI);

        final ReaderGroupConfig readerGroupConfig = ReaderGroupConfig.builder()
                .stream(Stream.of(scope, streamName))
                .build();
        try (ReaderGroupManager readerGroupManager = ReaderGroupManager.withScope(scope, controllerURI)) {
            readerGroupManager.createReaderGroup(readerGroup, readerGroupConfig);
        }

    }

    private static void releaseReaderGroup(String scope, String readerGroup, URI controllerURI) {
        System.out.format("release readerGroup: %s/%s\n", scope, readerGroup);
        try (ReaderGroupManager readerGroupManager = ReaderGroupManager.withScope(scope, controllerURI)) {
            readerGroupManager.deleteReaderGroup(readerGroup);
        }
    }

    private static void printHelp(int errCode) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("PravegaReader", getOptions());
        System.exit(errCode);
    }

    private static Options getOptions() {
        final Options options = new Options();
        options.addOption("s", "scope", true, "The scope name of the stream to read from.");
        options.addOption("n", "name", true, "The name of the stream to read from.");
        options.addOption("u", "uri", true, "The URI to the controller in the form tcp://host:port");
        options.addOption("i", "interval", true, "The interval(seconds) between each event");
        options.addOption("h", "help", false, "help");
        options.addOption("t", "thread", true, "the number of readers");
        return options;
    }

    private static CommandLine parseCommandLineArgs(Options options, String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        return cmd;
    }
}
