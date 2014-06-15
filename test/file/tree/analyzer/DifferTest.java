package file.tree.analyzer;

import java.io.IOException;
import java.util.List;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 *
 * @author afforix
 */
public class DifferTest {

    private static DiffInfo diff;
    private static Document doc1;
    private static Document doc2;

    public DifferTest() {
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        TestFileTree.createFileTree();
        TestFileTree.createChangedFileTree();

        FileInfo dir1 = DiskExplorer.getFileTree("testDir");
        FileInfo dir2 = DiskExplorer.getFileTree("changedTestDir/testDir");

        doc1 = FileInfoConverter.fileInfoToDom(dir1);
        doc2 = FileInfoConverter.fileInfoToDom(dir2);

        diff = Differ.diffDocuments(doc2, doc1);
    }

    @AfterClass
    public static void tearDownClass() {
        TestFileTree.deleteFileTree();
        TestFileTree.deleteChangedFileTree();
    }

    @Test
    public void testDiffer() {
        assertEquals(5, diff.getDiffChildren().size());
        List<DiffInfo> children = diff.getDiffChildren();

        DiffInfo dir1 = children.get(0);
        assertEquals("dir1", dir1.getName());
        assertEquals(ItemState.MODIFIED, dir1.getState());
        assertEquals(2, dir1.getNumberOfFiles());
        assertEquals(1, dir1.getNewNumberOfFiles());
        assertEquals(ItemState.UNMODIFIED, dir1.getDiffChildren().get(0).getState());

        DiffInfo dir2 = children.get(1);
        assertEquals(ItemState.DELETED, dir2.getState());
        assertEquals(ItemState.DELETED, dir2.getDiffChildren().get(0).getState());
        
        DiffInfo newFolder = children.get(2);
        assertEquals(ItemState.CREATED, newFolder.getState());
        assertEquals(ItemState.CREATED, newFolder.getDiffChildren().get(0).getState());
        
        DiffInfo changedFile = children.get(3);
        assertEquals(ItemState.MODIFIED, changedFile.getState());
        assertEquals(Long.valueOf(5), changedFile.getSize());
        assertEquals(Long.valueOf(13), changedFile.getNewSize());
        
        DiffInfo newFile = children.get(4);
        assertEquals(ItemState.CREATED, newFile.getState());
    }
}
