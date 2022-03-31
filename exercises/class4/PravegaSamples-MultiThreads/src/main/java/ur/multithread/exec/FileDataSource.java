package ur.multithread.exec;

import java.io.*;

public class FileDataSource implements DataSource<String> {
    private String filePath;
    private BufferedReader reader;

    public FileDataSource(String filePath) throws Exception {
        this.filePath = filePath;
        reader = new BufferedReader(new FileReader(filePath));
    }

    @Override
    public String next() throws Exception {
        return reader.readLine();
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
