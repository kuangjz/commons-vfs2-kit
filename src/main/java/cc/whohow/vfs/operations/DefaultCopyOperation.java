package cc.whohow.vfs.operations;

import cc.whohow.vfs.io.ReadableChannel;
import cc.whohow.vfs.io.WritableChannel;

import java.io.IOException;
import java.io.UncheckedIOException;

public class DefaultCopyOperation extends AbstractFileOperation<Copy.Options, Object> implements Copy {
    @Override
    public Object apply(Options options) {
        try (ReadableChannel src = options.getSource().getReadableChannel();
             WritableChannel dst = options.getDestination().getWritableChannel()) {
            dst.transferFrom(src);
            return null;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
