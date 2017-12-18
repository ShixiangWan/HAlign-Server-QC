package com.malab.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.TimerTask;
import java.util.logging.Logger;

// Note: Stop Hadoops and Instances after 20 mins if no operation.
public class TimerStopHadoops extends TimerTask{

    private static Logger logger = Logger.getLogger(String.valueOf(TimerStopHadoops.class));

	@SuppressWarnings("resource")
	@Override
	public void run() {
		try {
		    String timer_txt = "/home/ubuntu/tomcat/apache-tomcat-7.0.68/webapps/Halign/timer.txt";
			BufferedReader br = new BufferedReader(new FileReader(timer_txt));
			String status = br.readLine();
			if (status.equals("stopped")) {
				return;
			}
            logger.info("Judging TimerStopHadoops...");
			long timer = Long.parseLong(br.readLine());
			if (System.currentTimeMillis()-timer > 20*60*1000) {
				new PythonSDK().stop_hadoops();
				BufferedWriter bw = new BufferedWriter(new FileWriter(timer_txt));
				bw.write("stopped");
				bw.close();
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
