package eu.unifiedviews.plugins.marc;

import java.util.Locale;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.config.BaseConfigDialog;
import eu.unifiedviews.helpers.dpu.config.InitializableConfigDialog;
import eu.unifiedviews.helpers.dpu.localization.Messages;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU
 * configuration.
 */
public class ISO2709ToMarcXMLVaadinDialog extends BaseConfigDialog<ISO2709ToMarcXMLConfig_V1> implements InitializableConfigDialog {

    private static final long serialVersionUID = 4106235356505425558L;

    private ObjectProperty<Boolean> skipOnError = new ObjectProperty<Boolean>(false);

    private ObjectProperty<String> charset = new ObjectProperty<String>("");

    public ISO2709ToMarcXMLVaadinDialog() {
        super(ISO2709ToMarcXMLConfig_V1.class);
    }

    @Override
    public void initialize() {
        Locale locale = getContext().getLocale();
        Messages messages = new Messages(locale, this.getClass().getClassLoader());

        FormLayout mainLayout = new FormLayout();

        setWidth("100%");
        setHeight("100%");

        mainLayout.addComponent(new TextField(messages.getString("dialog.charset"), charset));
        mainLayout.addComponent(new CheckBox(messages.getString("dialog.skipOnError"), skipOnError));

        setCompositionRoot(mainLayout);
    }

    @Override
    public void setConfiguration(ISO2709ToMarcXMLConfig_V1 conf) throws DPUConfigException {
        skipOnError.setValue(conf.isSkipOnError());
        charset.setValue(conf.getCharset());
    }

    @Override
    public ISO2709ToMarcXMLConfig_V1 getConfiguration() throws DPUConfigException {
        ISO2709ToMarcXMLConfig_V1 conf = new ISO2709ToMarcXMLConfig_V1();
        conf.setSkipOnError(skipOnError.getValue());
        conf.setCharset(charset.getValue());
        return conf;
    }

}
