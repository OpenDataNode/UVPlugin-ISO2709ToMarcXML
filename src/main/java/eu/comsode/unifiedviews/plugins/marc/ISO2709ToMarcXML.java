package eu.comsode.unifiedviews.plugins.marc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Date;

import eu.unifiedviews.helpers.dataunit.copy.CopyHelpers;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.config.migration.ConfigurationUpdate;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;

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
public class ISO2709ToMarcXML extends AbstractDpu<ISO2709ToMarcXMLConfig_V1> {
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

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    @ExtensionInitializer.Init(param = "eu.unifiedviews.plugins.marc.ISO2709ToMarcXMLConfig_V1")
    public ConfigurationUpdate _ConfigurationUpdate;

    public ISO2709ToMarcXML() {
        super(ISO2709ToMarcXMLVaadinDialog.class, ConfigHistory.noHistory(ISO2709ToMarcXMLConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        String shortMessage = this.getClass().getSimpleName() + " starting.";
        String longMessage = String.valueOf(config);

        ContextUtils.sendMessage(ctx, DPUContext.MessageType.INFO, shortMessage, longMessage);

        FilesDataUnit.Iteration filesIteration;
        try {
            filesIteration = filesInput.getIteration();
        } catch (DataUnitException ex) {
            throw ContextUtils.dpuException(ctx, ex, "error.obtain.input");
        }

        long index = 0L;
        boolean shouldContinue = !ctx.canceled();
        File outputMarcXMLFile = null;
        Date start = null;
        try {
            FilesDataUnit.Entry entry = null;
            while (shouldContinue && filesIteration.hasNext()) {
                index++;
                entry = filesIteration.next();

                start = new Date();
                if (ctx.getExecMasterContext().getDpuContext().isDebugging()) {
                    LOG.debug("Processing {}. file {}", index, entry);
                }

                try {
                    File inputFile = new File(URI.create(entry.getFileURIString()));

                    outputMarcXMLFile = File.createTempFile("___", inputFile.getName(), new File(URI.create(filesOutput.getBaseFileURIString())));

                    FileInputStream is = new FileInputStream(inputFile);
                    FileOutputStream os = new FileOutputStream(outputMarcXMLFile);
                    MarcReader reader;
                    if ("AUTO".equalsIgnoreCase(config.getCharset())) {
                        reader = new MarcStreamReader(is);
                    } else {
                        reader = new MarcStreamReader(is, config.getCharset());
                    }
                    MarcWriter writer = new MarcXmlWriter(os, true);
                    try {
                        while (reader.hasNext()) {
                            Record record = reader.next();
                            writer.write(record);
                        }
                    } finally {
                        try {
                            is.close();
                        } catch (IOException ex) {
                        }
                        writer.close();
                    }
                    CopyHelpers.copyMetadata(entry.getSymbolicName(), filesInput, filesOutput);
                    filesOutput.updateExistingFileURI(entry.getSymbolicName(), outputMarcXMLFile.toURI().toASCIIString());

                    if (ctx.getExecMasterContext().getDpuContext().isDebugging()) {
                        LOG.debug("Processed {}. file in {}s", index, (System.currentTimeMillis() - start.getTime()) / 1000);
                    }
                } catch (DataUnitException | IOException e) {
                    if (config.isSkipOnError()) {
                        LOG.warn("Error processing {}. file {}", index, String.valueOf(entry), e);
                    } else {
                        throw ContextUtils.dpuException(ctx, e, "error.process.mrc", String.valueOf(entry));
                    }
                }

                shouldContinue = !ctx.canceled();
            }
        } catch (DataUnitException ex) {
            /**
             * There is nothing we can do
             */
            throw ContextUtils.dpuException(ctx, ex, "errors.dpu.failed");
        } finally {
            try {
                filesIteration.close();
            } catch (DataUnitException ex) {
            }
        }
    }
}
