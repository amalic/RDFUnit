package org.aksw.rdfunit.webdemo.view;

import com.vaadin.data.Property;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.aksw.rdfunit.RDFUnitConfiguration;
import org.aksw.rdfunit.services.FormatService;
import org.aksw.rdfunit.sources.SchemaSource;
import org.aksw.rdfunit.sources.SourceFactory;
import org.aksw.rdfunit.webdemo.RDFUnitDemoSession;
import org.aksw.rdfunit.webdemo.components.SchemaSelectorComponent;
import org.aksw.rdfunit.webdemo.utils.SchemaOption;
import org.aksw.rdfunit.webdemo.utils.WorkflowUtils;

import java.util.Arrays;
import java.util.Collection;

/**
 * Description
 *
 * @author Dimitris Kontokostas
 * @since 8/30/14 12:25 PM
 */
public class SchemaSelectorView extends CustomComponent implements WorkflowItem {

    private final NativeSelect inputFormatsSelect = new NativeSelect("Select Input Format");
    private final OptionGroup inputTypeSelect = new OptionGroup("Constraints Input Type");
    private final TextArea inputText = new TextArea();
    private final Label messageLabel = new Label();
    private final Button clearButton = new Button("Clear");
    private final Button continueButton = new Button("Load");

    private final Label autoOWLMessage = new Label();
    private final Label specificSchemasMessage = new Label();

    private final SchemaSelectorComponent schemaSelectorWidget = new SchemaSelectorComponent();

    private WorkflowItem previous;
    private WorkflowItem next;

    private volatile boolean isReady = false;

    public SchemaSelectorView() {
        initLayout();

    }


    private void initLayout() {
        VerticalLayout root = new VerticalLayout();
        root.setSpacing(true);

        HorizontalLayout components = new HorizontalLayout();
        components.setSpacing(true);
        components.setWidth("100%");


        setInputTypes();
        setInputFormats();
        setInputText();

        VerticalLayout optionSelection = new VerticalLayout();
        optionSelection.setSpacing(true);
        optionSelection.setWidth("200px");

        optionSelection.addComponent(inputTypeSelect);
        optionSelection.addComponent(inputFormatsSelect);

        VerticalLayout optionValues = new VerticalLayout();
        optionSelection.setSpacing(true);

        optionValues.addComponent(autoOWLMessage);
        optionValues.addComponent(specificSchemasMessage);
        optionValues.addComponent(schemaSelectorWidget);
        schemaSelectorWidget.setWidth("50%");
        optionValues.addComponent(inputText);

        components.addComponent(optionSelection);
        components.addComponent(optionValues);
        components.setExpandRatio(optionValues, 1.0f);

        HorizontalLayout bottomLayout = new HorizontalLayout();
        bottomLayout.setWidth("100%");
        bottomLayout.setSpacing(true);
        bottomLayout.addComponent(messageLabel);
        bottomLayout.setExpandRatio(messageLabel, 1.0f);
        messageLabel.setContentMode(ContentMode.HTML);

        bottomLayout.addComponent(clearButton);
        bottomLayout.addComponent(continueButton);


        root.addComponent(components);
        root.addComponent(bottomLayout);

        setDefaultValues();


        setCompositionRoot(root);


        clearButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                isReady = false;
                setDefaultValues();
            }
        });

        continueButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                SchemaSelectorView.this.continueButton.setEnabled(false);
                UI.getCurrent().push();
                SchemaSelectorView.this.execute();
                SchemaSelectorView.this.continueButton.setEnabled(true);
                UI.getCurrent().push();
            }
        });

    }

    @Override
    public void setMessage(final String message, final boolean isError) {

        this.isReady = !isError;
        WorkflowUtils.setMessage(messageLabel, message, isError);
    }

    private void setDefaultValues() {
        messageLabel.setValue("Note that we do not provide full OWL/DSP/RS (but will in time) support see our <a href='https://github.com/AKSW/RDFUnit/wiki' target=\"_blank\">wiki</a> " +
                "and <a href='https://github.com/AKSW/RDFUnit/issues' target=\"_blank\">issues</a> for details.");
        messageLabel.setStyleName(ValoTheme.LABEL_LIGHT);
        inputFormatsSelect.setValue("turtle");
        inputTypeSelect.setValue(SchemaOption.AUTO_OWL);

    }


    private void setInputFormats() {
        inputFormatsSelect.addItem("turtle");
        inputFormatsSelect.setItemCaption("turtle", "Turtle");
        inputFormatsSelect.addItem("ntriples");
        inputFormatsSelect.setItemCaption("ntriples", "N-Triples");
        inputFormatsSelect.addItem("n3");
        inputFormatsSelect.setItemCaption("n3", "N3");
        inputFormatsSelect.addItem("jsonld");
        inputFormatsSelect.setItemCaption("jsonld", "JSON-LD");
        inputFormatsSelect.addItem("rdfjson");
        inputFormatsSelect.setItemCaption("rdfjson", "RDF/JSON");
        inputFormatsSelect.addItem("rdfxml");
        inputFormatsSelect.setItemCaption("rdfxml", "RDF/XML");

        // Select turtle
        inputFormatsSelect.setNullSelectionAllowed(false);
    }

    private void setInputTypes() {
        inputTypeSelect.addItem(SchemaOption.AUTO_OWL);
        inputTypeSelect.setItemCaption(SchemaOption.AUTO_OWL, "Automatic (RDFS/OWL)");
        inputTypeSelect.addItem(SchemaOption.SPECIFIC_URIS);
        inputTypeSelect.setItemCaption(SchemaOption.SPECIFIC_URIS, "Specific Schemas");
        inputTypeSelect.addItem(SchemaOption.CUSTOM_TEXT);
        inputTypeSelect.setItemCaption(SchemaOption.CUSTOM_TEXT, "Direct Input");

        inputTypeSelect.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {

                SchemaOption option = (SchemaOption) valueChangeEvent.getProperty().getValue();

                switch (option) {
                    case AUTO_OWL:
                        inputFormatsSelect.setVisible(false);
                        specificSchemasMessage.setVisible(false);
                        schemaSelectorWidget.setVisible(false);
                        inputText.setVisible(false);

                        autoOWLMessage.setVisible(true);

                        break;

                    case SPECIFIC_URIS:
                        inputFormatsSelect.setVisible(false);
                        inputText.setVisible(false);
                        autoOWLMessage.setVisible(false);

                        specificSchemasMessage.setVisible(true);
                        schemaSelectorWidget.setVisible(true);


                        break;
                    case CUSTOM_TEXT:
                        specificSchemasMessage.setVisible(false);
                        schemaSelectorWidget.setVisible(false);
                        autoOWLMessage.setVisible(false);

                        inputFormatsSelect.setVisible(true);
                        inputText.setVisible(true);

                        break;
                }
            }
        });
    }

    private void setInputText() {

        inputText.setCaption("Direct Constraints Input");
        inputText.setInputPrompt("Paste constraints in RDF directly here.\n" +
                "Constraints can be in the form of:\n" +
                "1) RDFS/OWL axioms (e.g. rdfs:range / rdfs:domain / cardinality constraints e.t.c.)\n" +
                "2) DSP Profile rules (http://dublincore.org/documents/2008/03/31/dc-dsp/)\n" +
                "3) IBM Resource (typed) Shapes (http://www.w3.org/Submission/2014/SUBM-shapes-20140211/)\n" +
                "(We do not yet support SPIN rules but will do soon)");

        inputText.setRows(8);
        inputText.setColumns(40);
        inputText.setWidth("100%");

        autoOWLMessage.setContentMode(ContentMode.HTML);
        autoOWLMessage.setValue("<h3>Automatic Constraint Detection based on RDFS/OWL</h3>" +
                "We will parse the input source and identify all used properties and classes.<br/>" +
                "Based on them we will try to dereference all mentions vocabularies & ontologies.<br/>" +
                "Then we will use these vocabularies in our algorithms to generate automatic RDFUnit Test Cases.<br/>" +
                "<b>Note that atm (for safety reasons) we automatically dereference only schemas that are available in LOV</b>");

        specificSchemasMessage.setContentMode(ContentMode.HTML);
        specificSchemasMessage.setValue("<h3>Set Specific Schema Constraints</h3>" +
                "Schemas from LOV are available with auto-complete and will be used for RDFS/OWL checking. " +
                "You can also paste a <b>custom URI</b> and press \"Enter\" to insert it.<br/>" +
                "Note that custom URIs <b>can contain OWL/RDFS, <a href=\"http://dublincore.org/documents/2008/03/31/dc-dsp/\" target=\"_blank\">DSP Constraints</a> " +
                "or <a href=\"http://www.w3.org/Submission/2014/SUBM-shapes-20140211/\" target=\"_blank\">Resource (typed) Shapes</a> constraints</b> (SPIN is not yet supported).<br/>" +
                "Even if you mix different types of constraints  RDFUnit will still work but the result will have to make sense ;)<br/>&nbsp;");
    }

    @Override
    public boolean isReady() {
        return isReady;
    }

    @Override
    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    @Override
    public void setPreviousItem(WorkflowItem item) {
        previous = item;
    }

    @Override
    public void setNextItem(WorkflowItem item) {
        next = item;
    }

    @Override
    public WorkflowItem getPreviousItem() {
        return previous;
    }

    @Override
    public WorkflowItem getNextItem() {
        return next;
    }

    @Override
    public boolean execute() {
        this.setReady(false);

        if (!WorkflowUtils.checkIfPreviousItemIsReady(SchemaSelectorView.this)) {
            this.setMessage("Please Complete previous step correctly", true);
            return false;
        }

        SchemaOption schemaOption = (SchemaOption) inputTypeSelect.getValue();
        Collection<SchemaSource> schemaSources = schemaSelectorWidget.getSelections();
        String text = inputText.getValue();
        String format = inputFormatsSelect.getValue().toString();

        RDFUnitConfiguration configuration = RDFUnitDemoSession.getRDFUnitConfiguration();

        if (configuration == null) {
            this.setMessage("Data Selection not configured properly", true);
            return false;
        }

        switch (schemaOption) {
            case AUTO_OWL:
                configuration.setAutoSchemataFromQEF(configuration.getTestSource().getExecutionFactory());
                break;
            case SPECIFIC_URIS:
                configuration.setSchemata(schemaSources);
                break;
            case CUSTOM_TEXT:
                String oficialFormat = FormatService.getInputFormat(format).getName();
                Collection<SchemaSource> customTestSource = Arrays.asList(
                        SourceFactory.createSchemaSourceFromText("http//rdfunit.aksw.org/custom#", text, oficialFormat)
                );

                configuration.setSchemata(customTestSource);
                break;

            default:
                this.setMessage("Unknown error, try again", true);
                return false;
        }

        this.setMessage("Constraints loaded successfully: (" + getSchemaDesc(configuration.getAllSchemata()) + ")", false);
        return true;
    }

    private String getSchemaDesc(Collection<SchemaSource> schemaSources) {
        StringBuilder builder = new StringBuilder();
        boolean firstTime = true;
        for (SchemaSource src : schemaSources) {
            if (!firstTime) {
                builder.append(", ");
            } else {
                firstTime = false;
            }

            builder.append(src.getPrefix());
        }
        return builder.toString();
    }


}