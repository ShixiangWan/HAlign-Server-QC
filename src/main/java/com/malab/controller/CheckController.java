package com.malab.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.malab.utils.PythonSDK;

import javax.servlet.ServletContext;

@Controller
public class CheckController {
    private ServletContext servletContext;
    @Autowired
    public void setServletContext(ServletContext context) {
        this.servletContext  = context;
    }
	// Ajax "GET" post
	@RequestMapping(value="start_hadoops", method = RequestMethod.GET)
	@ResponseBody
	public String start_hadoops(Model map){
		if (!new PythonSDK().run()) {
			return "error"; // Instances starting fail, please contact administrator!
		}
		if (!new PythonSDK().run_hadoops()) {
			return "error"; // Hadoops starting fail, please contact administrator!
		}
		try {
		    // Update Timer File
            String root = this.servletContext.getRealPath("/");
			BufferedWriter bWriter = new BufferedWriter(new FileWriter(root+"timer.txt"));
			bWriter.write("running");
			bWriter.write("\n");
			bWriter.write(Long.toString(System.currentTimeMillis()));
			bWriter.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return "success";
	}
	
	@RequestMapping(value="check_status", method = RequestMethod.GET)
	@ResponseBody
	public String check_status(Model map){
		try {
            String root = this.servletContext.getRealPath("/");
		    if (root.equals("")) {
                return "stopped";
            }
			BufferedReader br = new BufferedReader(new FileReader(root+"timer.txt"));
			String status = br.readLine();
			br.close();
			if (status.equals("running")) {
				return "running";
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return "stopped";
	}
}
