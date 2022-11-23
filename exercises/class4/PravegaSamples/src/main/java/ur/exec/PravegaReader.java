package ur.exec;

import io.pravega.client.ClientConfig;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.admin.ReaderGroupManager;
import io.pravega.client.stream.*;
import io.pravega.client.stream.impl.UTF8StringSerializer;
import org.apache.commons.cli.*;

import java.net.URI;
import java.util.UUID;

class Reader {
    private static final int READER_TIMEOUT_MS = 2000;

    public final String scope;
    public final String streamName;
    public final URI controllerURI;

    public Reader(String scope, String streamName, URI controllerURI) {
        this.scope = scope;
        this.streamName = streamName;
        this.controllerURI = controllerURI;
    }

    public void start(int interval) {
        try {
            // start to create reader group
            System.out.format("Start to create Pravega ReaderGroup for stream %s:%s through endpoint %s",
                    scope, streamName, controllerURI);

            final String readerGroup = UUID.randomUUID().toString().replace("-", "");
            final ReaderGroupConfig readerGroupConfig = ReaderGroupConfig.builder()
                    .stream(Stream.of(scope, streamName))
                    .build();
            try (ReaderGroupManager readerGroupManager = ReaderGroupManager.withScope(scope, controllerURI)) {
                readerGroupManager.createReaderGroup(readerGroup, readerGroupConfig);
            }

            // start to create reader
            System.out.format("Start to create Pravega reader for ReaderGroup %s", readerGroup);
            try (EventStreamClientFactory clientFactory = EventStreamClientFactory.withScope(scope,
                    ClientConfig.builder().controllerURI(controllerURI).build());
                 EventStreamReader<String> reader = clientFactory.createReader("reader",
                         readerGroup,
                         new UTF8StringSerializer(),
                         ReaderConfig.builder().build())) {

                System.out.format("Start to read all the events from %s/%s with dump interval %d second(s)",
                        scope, streamName, interval);
                EventRead<String> event = null;
                do {
                    try {
                        event = reader.readNextEvent(READER_TIMEOUT_MS);
                        if (event.getEvent() != null) {
                            System.out.format("Read event '%s'\n", event.getEvent());
                        }
                    } catch (ReinitializationRequiredException e) {
                        //There are certain circumstances where the reader needs to be reinitialized
                        e.printStackTrace();
                    }
                    Thread.sleep(interval * 1000 /* milliseconds -> seconds */);
                } while (event.getEvent() != null);
                System.out.format("No more events from %s/%s", scope, streamName);
            }
        } catch (Exception e) {
            System.out.format("Unable to read Pravega stream", e);
        }
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
        final boolean isHelp = cmd.hasOption("help");
        if (isHelp) {
            printHelp(0);
        }

        final URI controllerURI = URI.create(uriString);
        int interval = 0;
        if (intervalString != null && ! intervalString.isEmpty()) {
            interval = Integer.valueOf(intervalString);
        }

        Reader reader = new Reader(scope, streamName, controllerURI);
        reader.start(interval);
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
        return options;
    }

    private static CommandLine parseCommandLineArgs(Options options, String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        return cmd;
    }
}
