package cc.whohow.vfs.watch;

import cc.whohow.vfs.FileObjectX;
import cc.whohow.vfs.version.FileVersion;
import cc.whohow.vfs.version.FileVersionProvider;
import org.apache.commons.vfs2.FileName;

import java.io.IOException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PollingFileWatchable<T> implements FileWatchable, Supplier<Map<FileName, FileVersion<T>>> {
    private final FileObjectX fileObject;
    private final FileVersionProvider<T> fileVersionProvider;

    public PollingFileWatchable(FileObjectX fileObject, FileVersionProvider<T> fileVersionProvider) {
        this.fileObject = fileObject;
        this.fileVersionProvider = fileVersionProvider;
    }

    public FileObjectX getFileObject() {
        return fileObject;
    }

    @Override
    public Map<FileName, FileVersion<T>> get() {
        try (Stream<FileVersion<T>> stream = fileVersionProvider.getVersions(fileObject)) {
            return stream.collect(Collectors.toMap(
                    f -> f.getFileObject().getName(), Function.identity(), (a, b) -> a, LinkedHashMap::new));
        }
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return fileObject.toString();
    }
}
