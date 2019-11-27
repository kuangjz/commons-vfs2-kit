package cc.whohow.vfs.provider.s3;

import cc.whohow.vfs.FileObjectX;
import cc.whohow.vfs.FileOperationX;
import cc.whohow.vfs.VirtualFileSystem;
import cc.whohow.vfs.io.AppendableConsumer;
import cc.whohow.vfs.io.IO;
import cc.whohow.vfs.operations.Copy;
import cc.whohow.vfs.version.FileVersion;
import cc.whohow.vfs.version.FileVersionView;
import cc.whohow.vfs.version.FileVersionViewWriter;
import cc.whohow.vfs.watch.FileDiffEntry;
import cc.whohow.vfs.watch.FileDiffIterator;
import cc.whohow.vfs.watch.FileDiffStatistics;
import org.apache.commons.vfs2.FileSystemException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class S3FileSync implements
        Supplier<Stream<FileDiffEntry<String>>>,
        Function<FileDiffEntry<String>, FileOperationX<?, ?>>, Consumer<FileDiffEntry<String>>, BiConsumer<FileDiffEntry<String>, Executor>,
        Callable<FileDiffStatistics> {
    protected VirtualFileSystem vfs;
    protected FileObjectX context;
    protected FileObjectX source;
    protected FileObjectX target;
    protected int bufferSize = IO.BUFFER_SIZE;

    public S3FileSync(VirtualFileSystem vfs, String context, String source, String target) throws FileSystemException {
        this.vfs = vfs;
        this.context = vfs.resolveFile(context);
        this.source = vfs.resolveFile(source);
        this.target = vfs.resolveFile(target);
    }

    protected FileOperationX<?, ?> copy(String path) {
        try {
            return vfs.getCopyOperation(new Copy.Options(source.resolveFile(path), target.resolveFile(path)));
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected FileOperationX<?, ?> delete(String path) {
        try {
            return vfs.getRemoveOperation(target.resolveFile(path));
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public FileOperationX<?, ?> apply(FileDiffEntry<String> diff) {
        switch (diff.getValue()) {
            case NOT_MODIFIED: {
                return null;
            }
            case CREATE:
            case MODIFY: {
                return copy(diff.getKey());
            }
            case DELETE: {
                return delete(diff.getKey());
            }
            default: {
                throw new IllegalArgumentException(diff.toString());
            }
        }
    }

    @Override
    public void accept(FileDiffEntry<String> diff) {
        FileOperationX<?, ?> o = apply(diff);
        if (o != null) {
            o.call();
        }
    }

    @Override
    public void accept(FileDiffEntry<String> diff, Executor executor) {
        FileOperationX<?, ?> o = apply(diff);
        if (o != null) {
            o.call(executor).join();
        }
    }

    protected void listFileVersion(FileObjectX folder, String versionFile) throws IOException {
        try (FileVersionViewWriter writer = newFileVersionViewWriter(versionFile, folder.getName().getURI())) {
            try (Stream<FileVersion<String>> versions = new S3FileVersionProvider().getVersions(folder)) {
                versions.map(FileVersionView::of)
                        .forEach(writer);
                writer.flush();
            }
        }
    }

    protected void diffFileVersion(String newVersionFile, String oldVersionFile, String diffFile) throws IOException {
        try (Writer writer = newWriter(diffFile)) {
            try (Stream<FileVersionView> newList = readFileVersionView(newVersionFile);
                 Stream<FileVersionView> oldList = readFileVersionView(oldVersionFile)) {
                newFileDiffIterator(newList, oldList).stream()
                        .map(FileDiffEntry::toString)
                        .forEach(new AppendableConsumer(writer, "", "\n"));
                writer.flush();
            }
        }
    }

    protected FileDiffIterator<FileVersionView, String, FileVersionView> newFileDiffIterator(Stream<FileVersionView> newList,
                                                                                    Stream<FileVersionView> oldList) {
        return new FileDiffIterator<>(
                FileVersionView::getName,
                Function.identity(),
                this::isSame,
                new LinkedHashMap<>(),
                newList.iterator(),
                oldList.iterator());
    }

    protected boolean isSame(FileVersionView o, FileVersionView n) {
        return o.getSize() == n.getSize() && o.getLastModifiedTime() <= n.getLastModifiedTime();
    }

    @Override
    public Stream<FileDiffEntry<String>> get() {
        try {
            context.deleteAll();

            listFileVersion(source, "new.txt");
            listFileVersion(target, "old.txt");
            diffFileVersion("new.txt", "old.txt", "diff.txt");
            return readFileDiffEntry("diff.txt");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public FileDiffStatistics call() {
        try (Writer log = newWriter("log.txt")) {
            log.write("time: ");
            log.write(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now()));
            log.write("\n");
            log.write("source: ");
            log.write(source.getName().getURI());
            log.write("\n");
            log.write("target: ");
            log.write(target.getName().getURI());
            log.write("\n\n\n");

            FileDiffStatistics statistics = new FileDiffStatistics();
            try (Stream<FileDiffEntry<String>> diff = get()) {
                diff.peek(statistics)
                        .filter(FileDiffEntry::isModified)
                        .peek(this)
                        .map(FileDiffEntry::toString)
                        .forEach(new AppendableConsumer(log, "", "\n"));
            }

            log.write("\n\n\n");
            log.write(statistics.toString());
            log.flush();
            return statistics;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Writer newWriter(String path) throws IOException {
        return new OutputStreamWriter(new BufferedOutputStream(
                context.resolveFile(path).getOutputStream(), bufferSize), StandardCharsets.UTF_8);
    }

    public FileVersionViewWriter newFileVersionViewWriter(String path, String prefix) throws IOException {
        return new FileVersionViewWriter(new BufferedOutputStream(
                context.resolveFile(path).getOutputStream(), bufferSize), prefix);
    }

    public Stream<FileVersionView> readFileVersionView(String path) throws IOException {
        return new BufferedReader(new InputStreamReader(context.resolveFile(path).getInputStream())).lines()
                .map(FileVersionView::parse);
    }

    public Stream<FileDiffEntry<String>> readFileDiffEntry(String path) throws IOException {
        return new BufferedReader(new InputStreamReader(context.resolveFile(path).getInputStream())).lines()
                .map(FileDiffEntry::parse);
    }
}
