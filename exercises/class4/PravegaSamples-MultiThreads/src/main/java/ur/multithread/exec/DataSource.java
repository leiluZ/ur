package ur.multithread.exec;

import java.io.Closeable;
import java.io.IOException;

public interface DataSource<T> extends Closeable {
    T next() throws Exception;
}
