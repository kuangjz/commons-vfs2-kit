package cc.whohow.vfs;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.operations.FileOperations;

public interface FileOperationsX extends FileOperations {
    <T, R, O extends FileOperationX<T, R>> O getOperation(Class<? extends O> fileOperation, T args) throws FileSystemException;
}
