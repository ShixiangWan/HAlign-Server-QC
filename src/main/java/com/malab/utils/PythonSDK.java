package com.malab.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

// Note: Python scripts path of Linux/Windows system !!!
public class PythonSDK {
	public static void main(String[] args) {
		new PythonSDK().run_hadoops();
	}
	
	@SuppressWarnings("static-access")
	public boolean run() {
		// stopped --> running
		String status = "";
		status = new PythonSDK().SDK("describe_instances");
		if (status.equals("stopped")) {
			new PythonSDK().SDK("start_instances");
		}
		long startMili = System.currentTimeMillis();
		while (true) {
			status = new PythonSDK().SDK("describe_instances");
			if (status.equals("running") || (System.currentTimeMillis()-startMili >= 60000)) break;
			else {
				try{
					Thread.currentThread().sleep(5*1000);
				}
				catch(InterruptedException e) {
					return false;
				}
			}
		}
		return true;
	}
	
	@SuppressWarnings("static-access")
	public boolean run_hadoops() {
		// stopped --> running
		String status = "";
		long startMili = System.currentTimeMillis();
		while (true) {
			status = new PythonSDK().SDK("start_hadoops");
			if (status.equals("running") || (System.currentTimeMillis()-startMili >= 60000)) break;
			else {
				try{
					Thread.currentThread().sleep(5*1000);
				}
				catch(InterruptedException e) {
					return false;
				}
			}
		}
		return true;
	}
	
	@SuppressWarnings("static-access")
	public boolean stop_hadoops() {
		// stopped --> running
		String status = "";
		long startMili = System.currentTimeMillis();
		while (true) {
			status = new PythonSDK().SDK("stop_hadoops");
			if (status.equals("stopped") || (System.currentTimeMillis()-startMili >= 60000)) break;
			else {
				try{
					Thread.currentThread().sleep(5*1000);
				}
				catch(InterruptedException e) {
					return false;
				}
			}
		}
		return true;
	}
	
	@SuppressWarnings("static-access")
	public boolean stop() {
		// running --> stopped
		String status = "";
		status = new PythonSDK().SDK("describe_instances");
		if (status.equals("running")) {
			new PythonSDK().SDK("stop_instances");
		}
		long startMili = System.currentTimeMillis();
		while (true) {
			status = new PythonSDK().SDK("describe_instances");
			if (status.equals("stopped") || (System.currentTimeMillis()-startMili >= 60000)) break;
			else {
				try{
					Thread.currentThread().sleep(5*1000);
				}
				catch(InterruptedException e) {
					return false;
				}
			}
		}
		return true;
	}
	
	public String SDK(String api) {
		String status = "";
		try {
			Process process = Runtime.getRuntime().exec("python /home/ubuntu/tomcat/apache-tomcat-7.0.68/webapps/Halign/python/" + api + ".py");
			//Process process = Runtime.getRuntime().exec("python D://python/" + api + ".py");
			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = "";
			while ((line = br.readLine()) != null) {
                System.out.println("Log: " + line);
                if (line.contains("running") || line.contains("be started")) {
                	status = "running";
                	break;
                }
                if (line.contains("stopped") || line.contains("is not active")) {
                	status = "stopped";
                	break;
                }
            }
            br.close();
            process.waitFor();
		} catch (Exception e) {
			System.out.println("ERROR: " + e);
		}
		return status;
	}
	
}
