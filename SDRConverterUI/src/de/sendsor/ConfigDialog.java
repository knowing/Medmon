package de.sendsor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 20.06.2011
 * 
 */
public class ConfigDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private final Configuration configuration;

	private boolean canceled = false;

	/**
	 * Create the dialog.
	 */
	public ConfigDialog(Configuration config) {
		this.configuration = config;
		setAlwaysOnTop(true);
		setTitle("Configuration Dialog");
		setModal(true);
		setBounds(100, 100, 450, 374);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 436, 0 };
		gbl_contentPanel.rowHeights = new int[] { 100, 100, 100, 0 };
		gbl_contentPanel.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);

		JPanel timePanel = new JPanel();
		timePanel.setBorder(new TitledBorder(null, "Timestamp", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_timePanel = new GridBagConstraints();
		gbc_timePanel.anchor = GridBagConstraints.NORTH;
		gbc_timePanel.fill = GridBagConstraints.BOTH;
		gbc_timePanel.insets = new Insets(0, 0, 5, 0);
		gbc_timePanel.gridx = 0;
		gbc_timePanel.gridy = 0;
		contentPanel.add(timePanel, gbc_timePanel);

		GridBagLayout gbl_timePanel = new GridBagLayout();
		gbl_timePanel.columnWidths = new int[] { 0, 77, 68, 0 };
		gbl_timePanel.rowHeights = new int[] { 34, 18, 0 };
		gbl_timePanel.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_timePanel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		timePanel.setLayout(gbl_timePanel);

		/* == Timestamp Output == */
		JLabel lOutput = new JLabel("Output");
		GridBagConstraints gbc_lOutput = new GridBagConstraints();
		gbc_lOutput.insets = new Insets(0, 0, 5, 5);
		gbc_lOutput.gridx = 0;
		gbc_lOutput.gridy = 0;
		timePanel.add(lOutput, gbc_lOutput);

		final JRadioButton bRadioAbsoluteOutput = new JRadioButton("absolute");
		bRadioAbsoluteOutput.setSelected(true);
		GridBagConstraints gbc_bRadioAbsoluteOutput = new GridBagConstraints();
		gbc_bRadioAbsoluteOutput.insets = new Insets(0, 0, 5, 5);
		gbc_bRadioAbsoluteOutput.gridx = 1;
		gbc_bRadioAbsoluteOutput.gridy = 0;
		timePanel.add(bRadioAbsoluteOutput, gbc_bRadioAbsoluteOutput);

		final JRadioButton bRadioRelativeOutput = new JRadioButton("relative");
		GridBagConstraints gbc_bRadioRelativeOutput = new GridBagConstraints();
		gbc_bRadioRelativeOutput.insets = new Insets(0, 0, 5, 0);
		gbc_bRadioRelativeOutput.gridx = 2;
		gbc_bRadioRelativeOutput.gridy = 0;
		timePanel.add(bRadioRelativeOutput, gbc_bRadioRelativeOutput);
		
		ActionListener outputListener2 = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (bRadioAbsoluteOutput.isSelected())
					configuration.setRelativeOutput(false);
				else
					configuration.setRelativeOutput(true);
			}
		};
		
		bRadioAbsoluteOutput.addActionListener(outputListener2);
		bRadioRelativeOutput.addActionListener(outputListener2);
		ButtonGroup timeOutputGroup = new ButtonGroup();
		timeOutputGroup.add(bRadioAbsoluteOutput);
		timeOutputGroup.add(bRadioRelativeOutput);
		bRadioAbsoluteOutput.setSelected(!config.isRelativeOutput());
		bRadioRelativeOutput.setSelected(config.isRelativeOutput());

		/* == Preview Relative/Absolute == */
		ButtonGroup timePreviewGroup = new ButtonGroup();
		JLabel lPreview = new JLabel("Preview");
		GridBagConstraints gbc_lPreview = new GridBagConstraints();
		gbc_lPreview.insets = new Insets(0, 0, 0, 5);
		gbc_lPreview.gridx = 0;
		gbc_lPreview.gridy = 1;
		timePanel.add(lPreview, gbc_lPreview);
		final JRadioButton bRadioAbsolutePreview = new JRadioButton("absolute");
		bRadioAbsolutePreview.setSelected(true);
		GridBagConstraints gbc_bRadioAbsolutePreview = new GridBagConstraints();
		gbc_bRadioAbsolutePreview.anchor = GridBagConstraints.NORTHWEST;
		gbc_bRadioAbsolutePreview.insets = new Insets(0, 0, 0, 5);
		gbc_bRadioAbsolutePreview.gridx = 1;
		gbc_bRadioAbsolutePreview.gridy = 1;
		timePanel.add(bRadioAbsolutePreview, gbc_bRadioAbsolutePreview);
		timePreviewGroup.add(bRadioAbsolutePreview);

		bRadioAbsolutePreview.setSelected(!config.isRelativePreview());

		final JRadioButton bRadioRelativePreview = new JRadioButton("relative");
		GridBagConstraints gbc_bRadioRelativePreview = new GridBagConstraints();
		gbc_bRadioRelativePreview.anchor = GridBagConstraints.NORTHWEST;
		gbc_bRadioRelativePreview.gridx = 2;
		gbc_bRadioRelativePreview.gridy = 1;
		timePanel.add(bRadioRelativePreview, gbc_bRadioRelativePreview);

		timePreviewGroup.add(bRadioRelativePreview);
		bRadioRelativePreview.setSelected(config.isRelativePreview());

		ActionListener previewListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (bRadioAbsolutePreview.isSelected())
					configuration.setRelativePreview(false);
				else
					configuration.setRelativePreview(true);
			}
		};
		bRadioAbsolutePreview.addActionListener(previewListener);
		bRadioRelativePreview.addActionListener(previewListener);
		/* == Aggregation Panel == */
		JPanel aggPanel = new JPanel();
		aggPanel.setBorder(new TitledBorder(null, "Aggregation", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_aggPanel = new GridBagConstraints();
		gbc_aggPanel.fill = GridBagConstraints.BOTH;
		gbc_aggPanel.insets = new Insets(0, 0, 5, 0);
		gbc_aggPanel.gridx = 0;
		gbc_aggPanel.gridy = 1;
		contentPanel.add(aggPanel, gbc_aggPanel);
		GridBagLayout gbl_aggPanel = new GridBagLayout();
		gbl_aggPanel.columnWidths = new int[] { 0, 0, 0 };
		gbl_aggPanel.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_aggPanel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_aggPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		aggPanel.setLayout(gbl_aggPanel);

		final JCheckBox cAggregate = new JCheckBox("Aggregate data", config.getAggregation().equals(SDRConverter.AGGREGATE_AVERAGE));
		GridBagConstraints gbc_cAggregate = new GridBagConstraints();
		gbc_cAggregate.insets = new Insets(0, 0, 5, 0);
		gbc_cAggregate.anchor = GridBagConstraints.WEST;
		gbc_cAggregate.gridwidth = 2;
		gbc_cAggregate.gridx = 0;
		gbc_cAggregate.gridy = 0;
		aggPanel.add(cAggregate, gbc_cAggregate);
		cAggregate.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (cAggregate.isSelected())
					configuration.setAggregation(SDRConverter.AGGREGATE_AVERAGE);
				else
					configuration.setAggregation(SDRConverter.AGGREGATE_NONE);
			}
		});

		JLabel lUnit = new JLabel("Unit");
		GridBagConstraints gbc_lUnit = new GridBagConstraints();
		gbc_lUnit.anchor = GridBagConstraints.EAST;
		gbc_lUnit.insets = new Insets(0, 0, 5, 5);
		gbc_lUnit.gridx = 0;
		gbc_lUnit.gridy = 1;
		aggPanel.add(lUnit, gbc_lUnit);

		final JSpinner sUnit = new JSpinner();
		sUnit.setModel(new SpinnerNumberModel(new Double(1), new Double(0), null, new Double(1)));
		sUnit.setEnabled(cAggregate.isSelected());
		GridBagConstraints gbc_sUnit = new GridBagConstraints();
		gbc_sUnit.insets = new Insets(0, 0, 5, 0);
		gbc_sUnit.fill = GridBagConstraints.HORIZONTAL;
		gbc_sUnit.gridx = 1;
		gbc_sUnit.gridy = 1;
		aggPanel.add(sUnit, gbc_sUnit);
		sUnit.setValue(config.getUnit());
		sUnit.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Double value = (Double) sUnit.getValue();
				configuration.setUnit(value);
			}
		});

		JLabel lInterval = new JLabel("Interval");
		GridBagConstraints gbc_lInterval = new GridBagConstraints();
		gbc_lInterval.anchor = GridBagConstraints.EAST;
		gbc_lInterval.insets = new Insets(0, 0, 0, 5);
		gbc_lInterval.gridx = 0;
		gbc_lInterval.gridy = 2;
		aggPanel.add(lInterval, gbc_lInterval);

		final JComboBox cInterval = new JComboBox();
		cInterval.setModel(new DefaultComboBoxModel(new String[] { "second", "minute", "hour", "day" }));
		cInterval.setEnabled(cAggregate.isSelected());
		GridBagConstraints gbc_cInterval = new GridBagConstraints();
		gbc_cInterval.fill = GridBagConstraints.HORIZONTAL;
		gbc_cInterval.gridx = 1;
		gbc_cInterval.gridy = 2;
		aggPanel.add(cInterval, gbc_cInterval);
		cInterval.setSelectedItem(config.getInterval());
		cInterval.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				configuration.setInterval(cInterval.getSelectedItem().toString());
			}
		});

		/* == Listener for Aggregation Checkbox == */
		cAggregate.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				sUnit.setEnabled(cAggregate.isSelected());
				cInterval.setEnabled(cAggregate.isSelected());
			}
		});

		JPanel outputPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) outputPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		outputPanel.setBorder(new TitledBorder(null, "Output", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_outputPanel = new GridBagConstraints();
		gbc_outputPanel.anchor = GridBagConstraints.WEST;
		gbc_outputPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_outputPanel.gridx = 0;
		gbc_outputPanel.gridy = 2;
		contentPanel.add(outputPanel, gbc_outputPanel);

		ButtonGroup outputGroup = new ButtonGroup();
		JRadioButton bRadioCSV = new JRadioButton("CSV", config.getOutput().equals("csv"));
		outputPanel.add(bRadioCSV);

		final JRadioButton bRadioARFF = new JRadioButton("ARFF", config.getOutput().equals("arff"));
		outputPanel.add(bRadioARFF);

		outputGroup.add(bRadioARFF);
		outputGroup.add(bRadioCSV);

		ActionListener outputListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (bRadioARFF.isSelected())
					configuration.setOutput("arff");
				else
					configuration.setOutput("csv");
			}
		};
		bRadioARFF.addActionListener(outputListener);
		bRadioCSV.addActionListener(outputListener);

		/* === Button Panel === */
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				canceled = false;
				dispose();
			}
		});
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
		JButton cancelButton = new JButton("Cancel");
		buttonPane.add(cancelButton);
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				canceled = true;
				dispose();
			}
		});
	}

	/**
	 * @return the configuration
	 */
	public Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * @return the canceled
	 */
	public boolean isCanceled() {
		return canceled;
	}
}
