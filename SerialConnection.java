/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zeldacon;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import javax.swing.JOptionPane;

/**
 *
 * @author User
 */
public class SerialConnection implements SerialPortEventListener {    
    private PortListner listener;
    SerialPort serialPort;
    private static final String PORT_NAMES[] = {"COM9"};
    private BufferedReader input;
    private OutputStream output;
    private static final int TIME_OUT = 2000;
    private static final int DATA_RATE = 9600;
    int period;
      
    public SerialConnection(PortListner listner, int period){
        this.listener = listner;
        this.period = period;
    }
    
    public void initialize() {
        System.setProperty("gnu.io.rxtx.SerialPorts", "COM9");

        CommPortIdentifier portId = null;
	Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

	while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            for (String portName : PORT_NAMES) {
		if (currPortId.getName().equals(portName)) {
                    portId = currPortId;
                    break;
		}
            }
	}
        
	if (portId == null) {
            JOptionPane.showMessageDialog(null, "Scanner not detected!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
	}
        
	try {
            serialPort = (SerialPort) portId.open(this.getClass().getName(),TIME_OUT);
            serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
            output = serialPort.getOutputStream();
            
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
	} catch (Exception e) {
            System.err.println(e.toString());
	}
    }
    
    public synchronized void close() {
	if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
	}
    }

  
    @Override
    public void serialEvent(SerialPortEvent spe) {
        if (spe.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
		String inputLine=input.readLine();
                listener.onReceived(inputLine);
		System.out.println(inputLine);
                if(period == PERIOD.TEMPORARY){
                    close();
                }
            } catch (Exception e) {
		System.err.println(e.toString());
            }
	}
    }
    
    public void listen(){
        this.initialize();
	Thread t=new Thread() {
            public void run() {
                try {Thread.sleep(1000000);} catch (InterruptedException ie) {}
            }
	};
	t.start();
    }
    
    public interface PortListner{
        public void onReceived(String data);
    }
}
