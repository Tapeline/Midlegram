package midp.tapeline.midlegram.database.vls;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import java.io.IOException;

public class VLSEngine {

    private final String rootDir;
    private final TableOfContents toc;

    public VLSEngine(String rootDir) {
        this.rootDir = rootDir;
        this.toc = new TableOfContents(rootDir + "toc.dat");
    }

    public void open() throws IOException {
        toc.open();
        toc.ensureExists(4096);
    }

    public void close() {
        try {
            toc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long append(byte[] data) throws IOException {
        PageIndex index = null;
        Page page = null;
        try {
            PageDescriptor lastPage = toc.maybeReadLastPageDescriptor();
            boolean needNewPage = false;
            if (lastPage != null && !lastPage.isAvailable) needNewPage = true;
            else if (lastPage != null) {
                long lastPageSize = getRealPageSize(lastPage.id);
                if (lastPageSize + data.length > toc.readMaxPageSize()) {
                    // close page as full
                    toc.updatePageDescriptor(new PageDescriptor(
                        lastPage.id, lastPage.start, lastPage.end, false
                    ));
                    // won't compensate on this in case following code fails
                    needNewPage = true;
                }
            }
            long newId = lastPage != null? toc.readLargestGid() + 1 : 0;
            PageDescriptor targetPage = lastPage;
            index = lastPage != null? getPageIndex(targetPage.id) : getPageIndex(0);
            index.open();
            // won't compensate on this either
            long recordOffset;
            if (needNewPage || lastPage == null) {
                targetPage = toc.appendPageDescriptor(new PageDescriptor(
                    PageDescriptor.UNKNOWN_ID, newId, newId, true
                ));
                index.createNew();
                recordOffset = 0;
            } else {
                recordOffset = getRealPageSize(targetPage.id);
            }
            RecordDescriptor targetRecord;
            try {
                targetRecord = index.appendRecordDescriptor(
                    new RecordDescriptor(
                        newId, recordOffset, data.length, false
                    )
                );
            } catch (IOException e) {
                if (needNewPage || lastPage == null)
                    toc.writePageCount(toc.readPageCount() - 1);
                // compensate on creating page if needed
                throw e;
            }
            page = getPage(targetPage.id);
            try {
                page.open();
                if (needNewPage || lastPage == null)
                    page.createNew();
                page.write(targetRecord, data);
            } catch (IOException e) {
                if (needNewPage || lastPage == null)
                    toc.writePageCount(toc.readPageCount() - 1);
                // compensate on creating page if needed
                index.writeRecordCount(index.readRecordCount() - 1);
                // compensate on failed record
                throw e;
            }
            return targetRecord.gid;
        } finally {
            if (page != null) page.close();
            if (index != null) index.close();
        }
    }

    public void delete(long gid) throws IOException {
        PageDescriptor page = toc.maybeGetPageContainingGid(gid);
        if (page == null) return;
        PageIndex index = getPageIndex(page.id);
        index.open();
        try {
            RecordDescriptor record = index.maybeGetRecordByGid(gid);
            if (record != null)
                index.updateRecordDescriptor(
                    new RecordDescriptor(
                        record.gid, record.offset, record.length, true
                    )
                );
        } finally {
            index.close();
        }
    }

    public byte[] maybeGet(long gid) throws IOException {
        PageDescriptor pageDescriptor = toc.maybeGetPageContainingGid(gid);
        if (pageDescriptor == null) return null;
        PageIndex index = getPageIndex(pageDescriptor.id);
        index.open();
        Page page = null;
        try {
            RecordDescriptor record = index.maybeGetRecordByGid(gid);
            if (record == null) return null;
            page = getPage(pageDescriptor.id);
            page.open();
            return page.read(record);
        } finally {
            if (page != null) page.close();
            index.close();
        }
    }

    public void inPlaceUpdate(long gid, byte[] newData) throws IOException {
        PageDescriptor pageDescriptor = toc.maybeGetPageContainingGid(gid);
        if (pageDescriptor == null) return;
        PageIndex index = getPageIndex(pageDescriptor.id);
        index.open();
        Page page = null;
        try {
            RecordDescriptor record = index.maybeGetRecordByGid(gid);
            if (record == null) return;
            page = getPage(pageDescriptor.id);
            page.open();
            page.inPlaceUpdate(record, newData);
        } finally {
            if (page != null) page.close();
            index.close();
        }
    }

    private PageIndex getPageIndex(int pageId) {
        return new PageIndex(rootDir + "idx" + pageId + ".dat");
    }

    private Page getPage(int pageId) {
        return new Page(rootDir + "page" + pageId + ".dat");
    }

    private long getRealPageSize(int pageId) throws IOException {
        FileConnection fc = null;
        try {
            fc = (FileConnection) Connector.open(rootDir + "page" + pageId + ".dat");
            return fc.fileSize();
        } finally {
            if (fc != null) fc.close();
        }
    }

}
