/**
 * 
 */
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
		gbc_timePanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_timePanel.insets = new Insets(0, 0, 5, 0);
		gbc_timePanel.gridx = 0;
		gbc_timePanel.gridy = 0;
		contentPanel.add(timePanel, gbc_timePanel);
		timePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

		ButtonGroup timeGroup = new ButtonGroup();
		final JRadioButton bRadioAbsolute = new JRadioButton("absolute");
		bRadioAbsolute.setSelected(true);
		timePanel.add(bRadioAbsolute);

		JRadioButton bRadioRelative = new JRadioButton("relative");
		timePanel.add(bRadioRelative);

		timeGroup.add(bRadioRelative);
		timeGroup.add(bRadioAbsolute);

		bRadioAbsolute.setSelected(!config.isRelative());
		bRadioRelative.setSelected(config.isRelative());
		ActionListener timestampListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (bRadioAbsolute.isSelected())
					configuration.setRelative(false);
				else
					configuration.setRelative(true);
			}
		};
		bRadioAbsolute.addActionListener(timestampListener);
		bRadioRelative.addActionListener(timestampListener);

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
