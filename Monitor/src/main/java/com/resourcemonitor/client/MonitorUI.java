package com.resourcemonitor.client;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.chart.axis.*;
import com.resourcemonitor.common.Configuration;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.UIManager.*;
import java.awt.*;
import java.util.*;
import com.resourcemonitor.common.MonitorBroker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.resourcemonitor.common.MonitorData;

/**
 * The UI
 */
public class MonitorUI extends JFrame {
	private static final long serialVersionUID = 1L;

	// cpu usage series
	private XYSeries nodeCpu;
	// mem usage series
	private XYSeries nodeMem;

	private XYSeries procCPU;
	private XYSeries procMem;

	// the model we are going to render
	private GraphModel model;
	String query;
	MonitorBroker broker;
	JTextField searchField;
	JLabel metaField;
	DataCollection collection;
	
	void updatePTQL(String ptql){
		broker.send("[PTQL]"+ptql);
		System.out.println("Sending new PTQL to broker: "+ptql);
	}
	
	void updateMetadata(String text){
		metaField.setText(text);
	}
	
	private class PTQLListener implements ActionListener {

	String text;

	public void actionPerformed(ActionEvent e) {
		this.text = searchField.getText();
		System.out.println("Changing query to: " + text);
		updatePTQL(this.text);
	}
	}

	/**
	 * Create the charts using JFree charts
	 * 
	 * @param title
	 *            ui title
	 */
	public MonitorUI(String title) {
		super(title);
		
		query = Configuration.getInstance().getPTQL();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		XYSeriesCollection cpuDataSet = new XYSeriesCollection();
		XYSeriesCollection memDataSet = new XYSeriesCollection();

		JFreeChart cpuChart = ChartFactory.createXYLineChart("CPU Node",
				"Time", "CPU %", cpuDataSet, PlotOrientation.VERTICAL, true,
				true, false);
		JFreeChart memChart = ChartFactory.createXYAreaChart("Memory Node",
				"Time", "Memory Usage %", memDataSet, PlotOrientation.VERTICAL,
				true, true, false);

		nodeCpu = new XYSeries("Overall CPU usage");
		nodeMem = new XYSeries("Overall Memory usage");

		cpuDataSet.addSeries(nodeCpu);
		memDataSet.addSeries(nodeMem);

		ValueAxis range, domain;

		cpuChart.getXYPlot().setDataset(cpuDataSet);
		ChartPanel chartPanel = new ChartPanel(cpuChart);
		// chartPanel.setPreferredSize(new Dimension(500, 270));
		XYPlot plot = cpuChart.getXYPlot();
		plot.setDomainAxis(0, new DateAxis());
		range = (ValueAxis) plot.getRangeAxis();
		range.setRange(0.0, 1.0);

		memChart.getXYPlot().setDataset(memDataSet);
		ChartPanel memPanel = new ChartPanel(memChart);
		// memPanel.setPreferredSize(new Dimension(500, 270));
		plot = memChart.getXYPlot();
		plot.setDomainAxis(0, new DateAxis());
		range = (ValueAxis) plot.getRangeAxis();
		range.setRange(0.0, 1.0);

		XYSeriesCollection procCpuDataSet = new XYSeriesCollection();
		XYSeriesCollection procMemDataSet = new XYSeriesCollection();

		JFreeChart procCpuChart = ChartFactory.createXYLineChart("CPU Process",
				"Time", "CPU %", procCpuDataSet, PlotOrientation.VERTICAL,
				true, true, false);
		JFreeChart procMemChart = ChartFactory.createXYAreaChart(
				"Memory Process", "Time", "Memory Usage MB", procMemDataSet,
				PlotOrientation.VERTICAL, true, true, false);

		procCPU = new XYSeries("CPU usage Process");
		procMem = new XYSeries("Memory usage Process");

		procCpuDataSet.addSeries(procCPU);
		procMemDataSet.addSeries(procMem);

		procCpuChart.getXYPlot().setDataset(procCpuDataSet);
		ChartPanel procChartPanel = new ChartPanel(procCpuChart);
		// procChartPanel.setPreferredSize(new Dimension(500, 270));
		XYPlot procPlot = procCpuChart.getXYPlot();
		procPlot.setDomainAxis(0, new DateAxis());

		procMemChart.getXYPlot().setDataset(procMemDataSet);
		ChartPanel procMemPanel = new ChartPanel(procMemChart);
		// procMemPanel.setPreferredSize(new Dimension(500, 270));
		procPlot = procMemChart.getXYPlot();
		procPlot.setDomainAxis(0, new DateAxis());

		JPanel jpanel = new JPanel(new GridLayout(2, 2));
		jpanel.setPreferredSize(new Dimension(800, 600));

		jpanel.add(chartPanel);
		jpanel.add(memPanel);

		jpanel.add(procChartPanel);
		jpanel.add(procMemPanel);
		
		memChart.getXYPlot().getRenderer().setSeriesPaint(0, java.awt.Color.blue);
		cpuChart.getXYPlot().getRenderer().setSeriesPaint(0, java.awt.Color.blue);
		procMemChart.getXYPlot().getRenderer().setSeriesPaint(0, java.awt.Color.blue);
		procCpuChart.getXYPlot().getRenderer().setSeriesPaint(0, java.awt.Color.blue);

		JPanel entrypanel = new JPanel(new GridBagLayout());
		entrypanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagConstraints epc = new GridBagConstraints();
		epc.fill = GridBagConstraints.HORIZONTAL;
		epc.gridx = 0;
		epc.gridy = 0;
		entrypanel.add(new JLabel("Sigar PTQL query:"), epc);
		searchField = new JTextField(query, 20);
		epc.gridx = 1;
		epc.gridy = 0;
		entrypanel.add(searchField, epc);
		epc.gridx = 2;
		epc.gridy = 0;
		JButton searchButton = new JButton("Search");
		searchButton.addActionListener(new PTQLListener());
		entrypanel.add(searchButton, epc);
		epc.gridx = 3;
		epc.gridy = 0;
		metaField = new JLabel("Hello sir!");
		metaField.setBorder(new EmptyBorder(0, 10, 0, 0));
		entrypanel.add(metaField, epc);

		JPanel mainpanel = new JPanel();

		mainpanel.setLayout(new GridBagLayout());
		GridBagConstraints mpc = new GridBagConstraints();
		mpc.fill = GridBagConstraints.NORTH;
		mpc.gridx = 0;
		mpc.gridy = 0;
		mainpanel.add(entrypanel, mpc);
		mpc.fill = GridBagConstraints.SOUTH;
		mpc.gridx = 0;
		mpc.gridy = 1;
		mainpanel.add(jpanel, mpc);

		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setResizable(false);
		this.getContentPane().add(mainpanel);
		this.pack();
		this.setVisible(true);
	}

	public MonitorUI(String title, GraphModel model, MonitorBroker broker, DataCollection collection) {
		this(title);
		this.model = model;
		this.broker = broker;
		this.collection = collection;
	}
	public void updateQuery(String query){
		searchField.setText(query);
	}
	/**
	 * Render the model
	 */
	public void update() {

		if(nodeCpu.getItemCount() > model.getDefaultSize()){
			nodeCpu.clear();
		}
		
		if(nodeMem.getItemCount() > model.getDefaultSize()){
			nodeMem.clear();
		}

		// add the values again
		for (int i = 0; i < model.getCurrentSize(); i++) {
			try {
				nodeCpu.add(model.getTime().get(i), model.getNodeCpu().get(i));
				nodeMem.add(model.getTime().get(i), model.getNodeMemory().get(i));
			}
			catch(Exception e){}
		}

		if(procCPU.getItemCount() > model.getDefaultSize()){
			procCPU.clear();
		}
		
		if(procMem.getItemCount() > model.getDefaultSize()){
			procMem.clear();
		}

		// add the values again
		
		for (int i = 0; i < model.getCurrentSize(); i++) {
			try {
			 procCPU.add(model.getTime().get(i), model.getProcCpu().get(i));
			 procMem.add(model.getTime().get(i),
			 model.getProcMemory().get(i));
			}
			catch(Exception e){}
		}
		int nodeCount = model.nodeCount;
		String metaStatus =  nodeCount + " node" + (nodeCount == 1 ? "" : "s");
		updateMetadata(metaStatus);
		
		// repaint the UI
		repaint();
	}
}
