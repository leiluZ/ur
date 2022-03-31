package ur.exec;

import io.grpc.internal.JsonParser;
import io.pravega.client.ClientConfig;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.stream.EventStreamWriter;
import io.pravega.client.stream.EventWriterConfig;
import io.pravega.client.stream.ScalingPolicy;
import io.pravega.client.stream.StreamConfiguration;
import io.pravega.client.stream.impl.UTF8StringSerializer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import io.pravega.client.admin.StreamManager;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

class Writer {
    private final String scope;
    private final String streamName;
    private final URI controllerURI;

    public Writer(String scope, String streamName, URI controllerURI) {
        this.scope = scope;
        this.streamName = streamName;
        this.controllerURI = controllerURI;
    }

    public void start(String dataFilename) {
        try {
            System.out.format("Start to read from file: %s", dataFilename);
            // init file reader
            try (BufferedReader reader = new BufferedReader(
                    new FileReader(dataFilename))) {
                // init Pravega writer
                StreamManager streamManager = StreamManager.create(controllerURI);
                /*final boolean scopeIsNew = */streamManager.createScope(scope);
                StreamConfiguration streamConfig = StreamConfiguration.builder()
                        .scalingPolicy(ScalingPolicy.fixed(3/*hardcoded*/)).build();

                System.out.format("Start to create Pravega stream %s:%s through endpoint %s",
                        scope, streamName, controllerURI);
                /*final boolean streamIsNew = */streamManager.createStream(scope, streamName, streamConfig);

                System.out.format("Start to create Pravega writer for stream %s:%s through endpoint %s",
                        scope, streamName, controllerURI);
                try (EventStreamClientFactory clientFactory = EventStreamClientFactory.withScope(scope,
                        ClientConfig.builder().controllerURI(controllerURI).build());
                     EventStreamWriter<String> writer = clientFactory.createEventWriter(streamName,
                             new UTF8StringSerializer(),
                             EventWriterConfig.builder().build())) {

                    System.out.format("Start to read for file %s", dataFilename);
                    while (true) {
                        // for each line in the data file, verify it's JSON
                        String event = reader.readLine();
                        if (event == null || event.isEmpty()) {
                            // end of file
                            break;
                        }
                        // JsonParser.parse(event);

                        // write to Pravega streams
                        System.out.format("Writing message: '%s' to stream '%s / %s'%n", event, scope, streamName);
                        writer.writeEvent(event);
                    }
                }
            }
        } catch (Exception e) {
            System.out.format("Unable to start data injection from file %s due to: %s" + dataFilename, e);
        }
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
        final boolean isHelp = cmd.hasOption("help");
        if (isHelp) {
            printHelp(0);
        }

        final URI controllerURI = URI.create(uriString);
        if (dataFilename == null || dataFilename.isEmpty()
                || ! Files.exists(Paths.get(dataFilename))) {
            System.out.format("data file: %s doesn't exist", dataFilename);
            System.exit(2);
        }

        Writer writer = new Writer(scope, streamName, controllerURI);
        writer.start(dataFilename);
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
        return options;
    }

    private static CommandLine parseCommandLineArgs(Options options, String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        return cmd;
    }
}
