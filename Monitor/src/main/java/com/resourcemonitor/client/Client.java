package com.resourcemonitor.client;

import com.resourcemonitor.common.Configuration;
import com.resourcemonitor.common.MonitorBroker;
import com.resourcemonitor.common.MonitorData;
import com.resourcemonitor.common.ReceiveHandler;

/**
 * The main program to run the client
 */
public class Client {
    // the raw data collection
    private DataCollection dataCollection;
    // the aggregator to create the summery
    private Aggregator aggregator;
    // UI
    private MonitorUI monitorUI;
    // Broker
    private MonitorBroker broker;
    // aggregator running interval
    private long aggregatorTime;
    // ui update interval
    private long uiUpdateTime;
    
    public Client() {
        aggregatorTime = Configuration.getInstance().getAggregatorInterval();
        uiUpdateTime = Configuration.getInstance().getUiUpdateInterval();
        long averageWindow = Configuration.getInstance().getAverageWindow();
        // create the collections
        dataCollection = new DataCollection();
        // summery
        GraphModel graphModel = new GraphModel(Configuration.getInstance().getNoOfPoints());
        // aggregator with offset 1 sec
        aggregator = new Aggregator(dataCollection, graphModel, averageWindow);
        // the broker
        broker = new MonitorBroker(false);
        // the UI
        monitorUI = new MonitorUI("P434 Project 4 - Group 1 - Malopinsky, Lowden", graphModel, broker, dataCollection);
		
		broker.send("[HEARTBEAT]");
    }

    public void start() {
        // a thread to update the UI
        Thread t = new Thread(new UIUpdateWorker());
        t.start();

        // start the aggregate thread
        Thread aggregateThread = new Thread(new AggregateWorker());
        aggregateThread.start();

        // start receiving
        broker.startReceive(new ReceiveHandler() {
            public void onMessage(String text) {
				if(text.indexOf("[PTQL]") == -1){
					MonitorData m = new MonitorData();
					m.parseString(text);
					if(m.getWorkerId() > 0){
						dataCollection.addMessage(m);
					}
				}
				else{
					String ptql = text.substring(6);
					monitorUI.updateQuery(ptql);
				}
            }
        });
    }

    // aggregate the results
    private class AggregateWorker implements Runnable {
        public void run() {
            while (true) {
                aggregator.generate();
                try {
                    Thread.sleep(aggregatorTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // we are going to generate every 1 sec and update the UI every 1 sec
    private class UIUpdateWorker implements Runnable {
        public void run() {
            while (true) {
                monitorUI.update();
                try {
                    Thread.sleep(uiUpdateTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Run the client
     * @param args nothing
     */
    public static void main(String[] args) {
        Client c = new Client();
        c.start();
    }
}
