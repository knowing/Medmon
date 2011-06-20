package de.sendsor;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.XYPlot;

import weka.core.Instances;
import javax.swing.JProgressBar;

/**
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 19.06.2011
 * 
 */
public class AppFrame extends JFrame implements PropertyChangeListener {

	private static final long serialVersionUID = 1L;

	private JPanel contentPane;
	private JTextField tInput;
	private JFormattedTextField tDate;

	private final SDRConverter converter = new SDRConverter();
	private final TimeSeriesChart chart = new TimeSeriesChart();
	private final DateFormat df = DateFormat.getDateTimeInstance();

	private File outputFile;

	private JProgressBar progressBar;
	private JPanel chartPanel;
	

	/**
	 * Create the frame.
	 */
	public AppFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 950, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_contentPane.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		JLabel lInput = new JLabel("Input");
		GridBagConstraints gbc_lInput = new GridBagConstraints();
		gbc_lInput.insets = new Insets(0, 0, 5, 5);
		gbc_lInput.anchor = GridBagConstraints.EAST;
		gbc_lInput.gridx = 0;
		gbc_lInput.gridy = 0;
		contentPane.add(lInput, gbc_lInput);

		tInput = new JTextField();
		GridBagConstraints gbc_tInput = new GridBagConstraints();
		gbc_tInput.insets = new Insets(0, 0, 5, 5);
		gbc_tInput.fill = GridBagConstraints.HORIZONTAL;
		gbc_tInput.gridx = 1;
		gbc_tInput.gridy = 0;
		contentPane.add(tInput, gbc_tInput);
		tInput.setColumns(10);

		JButton bOpen = new JButton("open");
		GridBagConstraints gbc_bOpen = new GridBagConstraints();
		gbc_bOpen.fill = GridBagConstraints.HORIZONTAL;
		gbc_bOpen.insets = new Insets(0, 0, 5, 0);
		gbc_bOpen.gridx = 2;
		gbc_bOpen.gridy = 0;
		contentPane.add(bOpen, gbc_bOpen);
		bOpen.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser dialog = new JFileChooser();
				dialog.setFileFilter(new FileFilter() {

					@Override
					public String getDescription() {
						return "SDR Files";
					}

					@Override
					public boolean accept(File f) {
						boolean ret = f.getName().toLowerCase().endsWith(".sdr");
						ret = ret || f.isDirectory();
						return ret;
					}
				});

				int ret = dialog.showOpenDialog(contentPane);
				if (ret == JFileChooser.APPROVE_OPTION) {
					File file = dialog.getSelectedFile();
					try {
						converter.setFile(file);
						tInput.setText(file.getAbsolutePath());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		JButton bConfigure = new JButton("configure ");
		GridBagConstraints gbc_bConfigure = new GridBagConstraints();
		gbc_bConfigure.anchor = GridBagConstraints.NORTHEAST;
		gbc_bConfigure.insets = new Insets(0, 0, 5, 0);
		gbc_bConfigure.gridx = 2;
		gbc_bConfigure.gridy = 1;
		contentPane.add(bConfigure, gbc_bConfigure);
		bConfigure.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showConfirmDialog(contentPane,"Not implemented yet" , "Not implemented", JOptionPane.WARNING_MESSAGE);
			}
		});

		JPanel timePanel = new JPanel();
		GridBagConstraints gbc_timePanel = new GridBagConstraints();
		gbc_timePanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_timePanel.insets = new Insets(0, 0, 5, 5);
		gbc_timePanel.anchor = GridBagConstraints.NORTH;
		gbc_timePanel.gridwidth = 2;
		gbc_timePanel.gridx = 0;
		gbc_timePanel.gridy = 2;
		contentPane.add(timePanel, gbc_timePanel);
		timePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

		JLabel lFrom = new JLabel("From");
		timePanel.add(lFrom);

		final JSpinner sFrom = new JSpinner();
		sFrom.setModel(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_YEAR));
		timePanel.add(sFrom);

		JLabel lTo = new JLabel("To");
		timePanel.add(lTo);

		final JSpinner sTo = new JSpinner();
		sTo.setModel(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_YEAR));
		timePanel.add(sTo);

		JButton bPreview = new JButton("Preview");
		GridBagConstraints gbc_bPreview = new GridBagConstraints();
		gbc_bPreview.fill = GridBagConstraints.HORIZONTAL;
		gbc_bPreview.insets = new Insets(0, 0, 5, 0);
		gbc_bPreview.gridx = 2;
		gbc_bPreview.gridy = 2;
		contentPane.add(bPreview, gbc_bPreview);
		bPreview.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					// Reset old chart
					chart.reset();
					chartPanel.removeAll();
					// Create new chart
					Instances dataSet = converter.getDataSet();
					chart.buildContent(dataSet);
					chartPanel.add(chart.getChartPanel(), BorderLayout.CENTER);
					chartPanel.validate();
					chart.getChart().addProgressListener(new ChartListener(tDate));

				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}
		});

		chartPanel = new JPanel(new BorderLayout());
		GridBagConstraints gbc_chartPanel = new GridBagConstraints();
		gbc_chartPanel.insets = new Insets(0, 0, 5, 0);
		gbc_chartPanel.gridwidth = 3;
		gbc_chartPanel.fill = GridBagConstraints.BOTH;
		gbc_chartPanel.gridx = 0;
		gbc_chartPanel.gridy = 3;
		contentPane.add(chartPanel, gbc_chartPanel);

		JPanel buttonPanel = new JPanel();
		GridBagConstraints gbc_buttonPanel = new GridBagConstraints();
		gbc_buttonPanel.insets = new Insets(0, 0, 5, 5);
		gbc_buttonPanel.anchor = GridBagConstraints.SOUTHWEST;
		gbc_buttonPanel.gridwidth = 2;
		gbc_buttonPanel.gridx = 0;
		gbc_buttonPanel.gridy = 4;
		contentPane.add(buttonPanel, gbc_buttonPanel);
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JLabel lDate = new JLabel("Date");
		buttonPanel.add(lDate);

		/* === Shows selected date === */
		tDate = new JFormattedTextField();
		tDate.setEditable(false);
		tDate.setColumns(20);
		buttonPanel.add(tDate);

		/* === Save JFreeChart Selection to Spinner "From" === */
		JButton bSaveAsFrom = new JButton("From");
		buttonPanel.add(bSaveAsFrom);
		bSaveAsFrom.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String text = tDate.getText();
				try {
					Date date = df.parse(text);
					sFrom.setValue(date);
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
			}
		});

		/* === Save JFreeChart Selection to Spinner "To" === */
		JButton bSaveAsTo = new JButton("  To  ");
		buttonPanel.add(bSaveAsTo);
		bSaveAsTo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String text = tDate.getText();
				try {
					Date date = df.parse(text);
					sTo.setValue(date);
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
			}
		});

		JButton bWrite = new JButton("write");
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.gridx = 2;
		gbc_btnNewButton.gridy = 4;
		contentPane.add(bWrite, gbc_btnNewButton);
		bWrite.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser dialog = new JFileChooser();
				int ret = dialog.showSaveDialog(contentPane);
				if (ret == JFileChooser.APPROVE_OPTION) {
					outputFile = dialog.getSelectedFile();
				}
				Date from = (Date) sFrom.getValue();
				Date to = (Date) sTo.getValue();
				Persister persister = new Persister(from, to, converter);
				try {
					persister.persistAsCSV(outputFile);
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(contentPane, e1.getMessage(), "Error while converting", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
				JOptionPane.showMessageDialog(contentPane, "Conversion successfull to file " + outputFile.getAbsolutePath());
			}
		});

		progressBar = new JProgressBar();
		progressBar.setMaximum(100);
		progressBar.setMinimum(0);
		progressBar.setStringPainted(true);
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.gridwidth = 3;
		gbc_progressBar.gridx = 0;
		gbc_progressBar.gridy = 5;
		contentPane.add(progressBar, gbc_progressBar);
		chart.addPropertyChangeListener(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		Integer progress = (Integer) e.getNewValue();
		progressBar.setValue(progress);
		progressBar.setString(progress + "%");
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AppFrame frame = new AppFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 
	 * @author Nepomuk Seiler
	 * @version 0.1
	 * @since 19.06.2011
	 * 
	 */
	private class ChartListener implements ChartProgressListener {

		private final JFormattedTextField tDate;

		public ChartListener(JFormattedTextField tDate) {
			this.tDate = tDate;
		}

		@Override
		public void chartProgress(ChartProgressEvent event) {
			if (event.getType() != ChartProgressEvent.DRAWING_FINISHED)
				return;
			// Detect mouse clicks and print the date to tDatePreview
			JFreeChart chart = null;
			XYPlot plot = null;
			if (event.getSource() instanceof JFreeChart) {
				chart = (JFreeChart) event.getSource();
				plot = (XYPlot) chart.getPlot();
			} else if (event.getSource() instanceof XYPlot) {
				plot = (XYPlot) event.getSource();
			}
			double time = plot.getDomainCrosshairValue();
			tDate.setText(df.format(new Date((long) time)));
		}
	}

}
