package cc.whohow.vfs.provider.aliyun.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.ObjectListing;

import java.util.Iterator;

/**
 * OSS原始对象遍历器
 */
public class AliyunOSSObjectListingIterator implements Iterator<ObjectListing> {
    private final OSS oss;
    private final ListObjectsRequest listObjectsRequest;
    private ObjectListing objectListing;

    public AliyunOSSObjectListingIterator(OSS oss, String bucketName, String prefix) {
        this(oss, new ListObjectsRequest(bucketName, prefix, null, null, 1000));
    }

    public AliyunOSSObjectListingIterator(OSS oss, String bucketName, String prefix, String delimiter) {
        this(oss, new ListObjectsRequest(bucketName, prefix, null, delimiter, 1000));
    }

    public AliyunOSSObjectListingIterator(OSS oss, ListObjectsRequest listObjectsRequest) {
        this.oss = oss;
        this.listObjectsRequest = listObjectsRequest;
    }

    public OSS getOSS() {
        return oss;
    }

    public String getBucketName() {
        return listObjectsRequest.getBucketName();
    }

    public String getPrefix() {
        return listObjectsRequest.getPrefix();
    }

    @Override
    public boolean hasNext() {
        if (objectListing == null) {
            objectListing = oss.listObjects(listObjectsRequest);
            return true;
        }
        if (objectListing.isTruncated()) {
            objectListing = oss.listObjects(listObjectsRequest.withMarker(objectListing.getNextMarker()));
            return true;
        }
        return false;
    }

    @Override
    public ObjectListing next() {
        return objectListing;
    }
}