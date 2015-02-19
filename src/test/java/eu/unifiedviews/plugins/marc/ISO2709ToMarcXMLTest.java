package eu.unifiedviews.plugins.marc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.helpers.dataunit.fileshelper.FilesHelper;


public class ISO2709ToMarcXMLTest {
    private static final Logger LOG = LoggerFactory.getLogger(ISO2709ToMarcXMLTest.class);

    @Test
    public void test1() throws Exception {
        ISO2709ToMarcXML dpu = new ISO2709ToMarcXML();
        ISO2709ToMarcXMLConfig_V1 config = new ISO2709ToMarcXMLConfig_V1();
        dpu.configureDirectly(config);

        // prepare test environment
        TestEnvironment env = new TestEnvironment();

        // prepare data units
        WritableFilesDataUnit filesInput = env.createFilesInput("filesInput");
        WritableFilesDataUnit filesOutput = env.createFilesOutput("filesOutput");

        InputStream mrcFileIS = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("comsode.mrc");
        
        File tempMrcFile = File.createTempFile("___", ".mrc");
        FileUtils.copyInputStreamToFile(mrcFileIS, tempMrcFile);
        
        try {
            filesInput.addExistingFile("test", URI.create(tempMrcFile.toURI().toASCIIString()).toString());

            env.run(dpu);

            Set<FilesDataUnit.Entry> outputFiles = FilesHelper.getFiles(filesOutput);
            assertNotNull(outputFiles);
            assertEquals(1, outputFiles.size());
            FilesDataUnit.Entry outputFile = null;
            outputFile = outputFiles.iterator().next();
            assertNotNull(outputFile);
            
            String xml = IOUtils.toString(java.net.URI.create(outputFile.getFileURIString()), "UTF-8");
            assertTrue(!xml.isEmpty());
            
        } catch (Exception e) {
            LOG.error("Some exception when performing test", e);
        } finally {
            if (mrcFileIS != null) {
                mrcFileIS.close();
            }
            
            if (tempMrcFile != null && tempMrcFile.exists()) {
                tempMrcFile.delete();
                LOG.debug("tmp file deleted");
            }
            
            env.release();
            
        }
    }
}
