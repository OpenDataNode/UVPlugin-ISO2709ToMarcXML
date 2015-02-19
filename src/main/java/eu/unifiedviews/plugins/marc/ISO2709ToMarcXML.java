package eu.unifiedviews.plugins.marc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.Locale;

import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import eu.unifiedviews.helpers.dpu.config.ConfigDialogProvider;
import eu.unifiedviews.helpers.dpu.config.ConfigurableBase;
import eu.unifiedviews.helpers.dpu.localization.Messages;

/**
 * We choose the type of this {@link DPU}, it can be {@link DPU.AsExtractor}, {@link DPU.AsLoader}, {@link DPU.AsTransformer}.
 * For this tutorial, we will program transformer DPU.
 *
 * If your {@link DPU} does not have any configuration dialog, you can declare is simply by
 * <p><blockquote><pre>
 * public class {@link ISO2709ToMarcXML} implements {@link DPU}
 * </pre></blockquote></p>
 */
@DPU.AsTransformer
public class ISO2709ToMarcXML extends ConfigurableBase<ISO2709ToMarcXMLConfig_V1> implements ConfigDialogProvider<ISO2709ToMarcXMLConfig_V1> {
    /**
     * We use slf4j for logging
     */
    private static final Logger LOG = LoggerFactory.getLogger(ISO2709ToMarcXML.class);

    /**
     * Files data unit, containing (one?) .mrc files ({@link FilesDataUnit}).
     */
    @DataUnit.AsInput(name = "filesInput")
    public FilesDataUnit filesInput;

    /**
     * We define one data unit on output, containing files ({@link FilesDataUnit}).
     */
    @DataUnit.AsOutput(name = "filesOutput")
    public WritableFilesDataUnit filesOutput;
    
    /**
     * We define class used for retrieving internationalized messages.
     */
    private Messages messages;
    
    private static final String ENCODING_STRING = "UTF-8";

    /**
     * Public non-parametric constructor has to call super constructor in {@link ConfigurableBase}
     */
    public ISO2709ToMarcXML() {
        super(ISO2709ToMarcXMLConfig_V1.class);
    }

    /**
     * Simple getter which is used by container to obtain configuration dialog instance.
     */
    @Override
    public AbstractConfigDialog<ISO2709ToMarcXMLConfig_V1> getConfigurationDialog() {
        return new ISO2709ToMarcXMLVaadinDialog();
    }

    @Override
    public void execute(DPUContext dpuContext) throws DPUException {
        Locale locale = dpuContext.getLocale();
        
        this.messages = new Messages(locale, this.getClass().getClassLoader());

        String shortMessage = this.getClass().getSimpleName() + " starting.";
        String longMessage = String.valueOf(config);

        dpuContext.sendMessage(DPUContext.MessageType.INFO, shortMessage, longMessage);

        FilesDataUnit.Iteration filesIteration;
        try {
            filesIteration = filesInput.getIteration();
        } catch (DataUnitException ex) {
            throw new DPUException(messages.getString("error.obtain.input"), ex);
        }

        long index = 0L;
        boolean shouldContinue = !dpuContext.canceled();
        File outputMarcXMLFile = null;
        Date start = null;
        try {
            FilesDataUnit.Entry entry = null;
            while (shouldContinue && filesIteration.hasNext()) {
                index++;
                entry = filesIteration.next();

                start = new Date();
                if (dpuContext.isDebugging()) {
                    LOG.debug("Processing {}. file {}", index, entry);
                }
                
                try {
                    File inputFile = new File(URI.create(entry.getFileURIString()));
                    
                    outputMarcXMLFile = File.createTempFile("___", inputFile.getName(), new File(URI.create(filesOutput.getBaseFileURIString())));

                    FileInputStream is = new FileInputStream(inputFile);
                    FileOutputStream os = new FileOutputStream(outputMarcXMLFile);
                    
                    MarcReader reader = new MarcStreamReader(is, ENCODING_STRING);
                    MarcWriter writer = new MarcXmlWriter(os, true);
                    
                    while (reader.hasNext()) {
                        Record record = reader.next();
                        writer.write(record);
                    }
                    
                    is.close();
                    writer.close();
                    
                    filesOutput.addExistingFile(entry.getSymbolicName(), outputMarcXMLFile.toURI().toASCIIString());
                    
                    if (dpuContext.isDebugging()) {
                        LOG.debug("Processed {}. file in {}s", index, (System.currentTimeMillis() - start.getTime()) / 1000);
                    }
                } catch (DataUnitException | IOException e) {
                    if (config.isSkipOnError()) {
                        LOG.warn("Error processing {}. file {}", index, String.valueOf(entry), e);
                    } else {
                        throw new DPUException(messages.getString("error.process.mrc", String.valueOf(entry)), e);
                    }
                }
                
                shouldContinue = !dpuContext.canceled();
            }
        } catch (DataUnitException ex) {
            /**
             * There is nothing we can do
             */
            throw new DPUException(messages.getString("errors.dpu.failed"), ex);
        } finally {
        }
    }
}
