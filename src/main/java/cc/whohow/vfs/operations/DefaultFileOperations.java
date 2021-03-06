package cc.whohow.vfs.operations;

import cc.whohow.vfs.FileOperationX;
import cc.whohow.vfs.FileOperationsX;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.operations.FileOperation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class DefaultFileOperations implements FileOperationsX {
    private Map<Class, Supplier<? extends FileOperationX>> factory = new ConcurrentHashMap<>();

    public DefaultFileOperations() {
        factory.put(Remove.class, DefaultRemoveOperation::new);
        factory.put(Copy.class, DefaultCopyOperation::new);
        factory.put(Move.class, DefaultMoveOperation::new);
    }

    @Override
    public <T, R, O extends FileOperationX<T, R>> O getOperation(Class<? extends O> fileOperation, T args) throws FileSystemException {
        return (O) getOperation(fileOperation).with(args);
    }

    @Override
    public Class<? extends FileOperation>[] getOperations() throws FileSystemException {
        return factory.keySet().toArray(new Class[0]);
    }

    @Override
    public FileOperationX getOperation(Class<? extends FileOperation> operationClass) throws FileSystemException {
        return factory.get(operationClass).get();
    }

    @Override
    public boolean hasOperation(Class<? extends FileOperation> operationClass) throws FileSystemException {
        return factory.containsKey(operationClass);
    }
}
