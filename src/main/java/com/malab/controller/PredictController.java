package com.malab.controller;

import com.malab.protein.ProteinMSA;
import com.malab.utils.TimerStopHadoops;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
import java.io.*;
import java.util.Date;
import java.util.Timer;

@Controller
public class PredictController implements ServletContextAware{
		private ServletContext servletContext;
		@Autowired
		public void setServletContext(ServletContext context) {
			this.servletContext  = context;
		}
		@SuppressWarnings({ "unused" })
		@RequestMapping(value="predict", method = RequestMethod.POST)
		public ModelAndView handleUploadData(String type, String alg, String mode,
                                             @RequestParam("file") CommonsMultipartFile file, Model map){
			String fileName = "";
            String root = this.servletContext.getRealPath("/");  //root path
            String timerFile = root+"timer.txt";
            String time = Long.toString(new Date().getTime());  //time
            fileName = root+"upload/input.fasta";
            try {
				if (!file.isEmpty()){
				    File file2 = new File(fileName); //create file for saving uploaded file
                    file.getFileItem().write(file2);
				} else {
					map.addAttribute("error", "Please upload a fasta DNA/RNA/Protein file!!!");
					return new ModelAndView("index");
				}

				//judge DNA/RNA or Protein?
				if (Integer.parseInt(type) == 1) { //DNA and RNA
                    if (Integer.parseInt(mode) == 1) { //Hadoop
                        if (get_hadoop_status(timerFile).equals("stopped")) {
                            map.addAttribute("status", "stopped");
                            return new ModelAndView("index");
                        }
                        update_timer(timerFile);
                    }
                    if (Integer.parseInt(alg) == 0) { //Suffix tree
                        Process process = Runtime.getRuntime().exec("java -jar "+root+"jar/MSA2.0.jar "
                                +root+"upload/input.fasta " + root+"upload/"+time+".fasta 0");
                        process.waitFor();
                    } else if (Integer.parseInt(alg) == 1) { //KBand
                        Process process = Runtime.getRuntime().exec("java -jar "+root+"jar/MSA2.0.jar "
                                +root+"upload/input.fasta " + root+"upload/"+time+".fasta 2");
                        process.waitFor();
                    } else { //Trie tree
                        Process process = Runtime.getRuntime().exec("java -jar "+root+"jar/MSA2.0.jar "
                                +root+"upload/input.fasta " + root+"upload/"+time+".fasta 3");
                        process.waitFor();
                    }
                } else { //Protein
                    if (Integer.parseInt(mode) == 1) { //Hadoop
                        if (get_hadoop_status(timerFile).equals("stopped")) {
                            map.addAttribute("status", "stopped");
                            return new ModelAndView("index");
                        }
                        update_timer(timerFile);
                        new ProteinMSA().runOnLocal(root+"upload/input.fasta", root+"upload/"+time+".fasta");;
                    } else { //Single
                        new ProteinMSA().runOnLocal(root+"upload/input.fasta", root+"upload/"+time+".fasta");
                    }
                }
				map.addAttribute("time", time); //save time link for user to download result
			    return new ModelAndView("index");
			} catch (Exception e) {
				map.addAttribute("error", "Failed, please contact administrator!");
			}
			return new ModelAndView("index");
		}
		
		public void update_timer(String time) {
			try {
				// Timer, gap is 20 min
				Timer timer = new Timer();
				timer.schedule(new TimerStopHadoops(), 1*60*1000, 20*60*1000);
				// Update Timer File
				BufferedWriter bWriter = new BufferedWriter(new FileWriter(time));
				bWriter.write("running");
				bWriter.write("\n");
				bWriter.write(Long.toString(System.currentTimeMillis()));
				bWriter.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

		public String get_hadoop_status (String time) {
		    try {
		        BufferedReader br = new BufferedReader(new FileReader(time));
                String status = br.readLine();
                br.close();
                if (status.equals("running")) {
                    return "running";
                } else {
                    return "stopped";
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
            return "stopped";
        }
}
