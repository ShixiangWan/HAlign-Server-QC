package com.malab.protein;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import com.malab.protein.ProteinMR.TwoAlignMapper;
import com.malab.utils.ClearDfsPath;
import com.malab.utils.CopyFile;

import jaligner.matrix.MatrixLoaderException;
import jaligner.util.SequenceParserException;

public class ProteinMSA {
	/*public static void main(String[] args) {
		//long t = System.currentTimeMillis();
		try {
			if (args.length == 1) {
				new ProteinMSA().runOnLocal(args[0]);
			} else if (args.length == 2) {
				new ProteinMSA().runOnHadoop(args[0], args[1]);
			} else {
				System.out.println("Wrong params! Please click here for more: https://github.com/ShixiangWan/MSA-protein");
				System.exit(0);
			}
		} catch (ClassNotFoundException | SequenceParserException | MatrixLoaderException 
				 | IOException | InterruptedException e) {
			e.printStackTrace();
		}
		//System.out.println(System.currentTimeMillis() - t);
		System.out.println("Note: The results is saved as 'output-protein.txt'.");
	}*/
	
	public void runOnHadoop(String inputfile, String outputDFS) throws MatrixLoaderException, SequenceParserException, 
													IOException, ClassNotFoundException, InterruptedException {
        String localpath = inputfile.substring(0, inputfile.lastIndexOf("/"));
        String outputfile = localpath + "/output-protein.txt";
        
		System.out.println(">>Clearing HDFS Path & uploading ...");
		new ClearDfsPath().run(outputDFS);
		CopyFile copyFile = new CopyFile();
		copyFile.local_to_dfs(inputfile, outputDFS + "/input/input.txt");
		
		System.out.println(">>Map reducing ...");
		Configuration conf = new Configuration();
		conf.set("mapred.task.timeout", "0");
		Job job = new Job(conf, "msa_protein");
		job.setJarByClass(ProteinMSA.class);
		job.setInputFormatClass(TextInputFormat.class);
		job.setMapperClass(TwoAlignMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(outputDFS + "/input/input.txt"));
		FileOutputFormat.setOutputPath(job, new Path(outputDFS + "/output"));
		job.setNumReduceTasks(1);
		job.waitForCompletion(true);
		
		copyFile.dfs_to_local(outputDFS + "/output/part-r-00000", localpath + "/firstMR.txt");
		BufferedReader bReader = new BufferedReader(new FileReader(localpath + "/firstMR.txt"));
		String line = "";
		String[] result;
        ArrayList<String> s_out1 = new ArrayList<>();
        ArrayList<String> s_out2 = new ArrayList<>();
		while (bReader.ready()) {
			line = bReader.readLine();
			result = line.split("\t");
			s_out1.add(result[0]);
			s_out2.add(result[1]);
		}
		bReader.close();
		new File(localpath + "/firstMR.txt").delete();
		
        ArrayList<String> s_key = new ArrayList<>();
    	bReader = new BufferedReader(new FileReader(inputfile));
        while (bReader.ready()) {
            line = bReader.readLine();
            if (line.charAt(0) == '>') {
                s_key.add(line);
            }
        }
        bReader.close();
        
        //统计第一个序列的比对结果，得到它的归总比对结果insertSpace1[]
        int index;
        String sequence1 = s_out1.get(0).replace("-", "");
        int sequenceLen1 = sequence1.length();
        int insertSpace1[] = new int[sequenceLen1 + 1];
        for (String line2 : s_out1) {
            int tempSpace1[] = new int[sequenceLen1 + 1];
            index = 0;
            for (int j=0; j < line2.length(); j++) {
                if (line2.charAt(j) == '-') {
                    tempSpace1[index]++;
                } else {
                    if (insertSpace1[index] < tempSpace1[index]) {
                        insertSpace1[index]=tempSpace1[index];
                    }
                    index++;
                }
            }
        }
        
        //以第一条序列为中心序列，计算中心序列sequence1
        StringBuilder stringBuilder = new StringBuilder();
        int insertSpaceLen1 = insertSpace1.length;
        for(int i=0; i<insertSpaceLen1; i++){
            for(int j=0; j<insertSpace1[i]; j++) {
                stringBuilder.append('-');
            }
            if(i != insertSpaceLen1-1) {
                stringBuilder.append(sequence1.charAt(i));
            }
        }
        sequence1 = stringBuilder.toString();
        
        //将归纳得到的sequence1再次与第一次比对结果比对，得到最终各序列比对结果存入文件
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputfile));
            TwoAlign centerAlign = new TwoAlign();
            for (int i=0; i<s_key.size(); i++) {
                bw.write(s_key.get(i));
                bw.newLine();
                if (i == 0) {
                    bw.write(sequence1);
                } else {
                    centerAlign.align(sequence1, s_out2.get(i-1));
                    bw.write(new String(TwoAlign.aligntResultTwo));
                }
                bw.newLine();
                bw.flush();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void runOnLocal(String inputfile, String outputfile) throws SequenceParserException, MatrixLoaderException {
        /*String localpath = inputfile.substring(0, inputfile.lastIndexOf("/"));
        String outputfile = localpath + "/output-protein.txt";*/
        
    	//将原始文件解析成arraylist
        String line;
        ArrayList<String> s_key = new ArrayList<>();
        ArrayList<String> s_val = new ArrayList<>();
        ArrayList<String> s_out1 = new ArrayList<>();
        ArrayList<String> s_out2 = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(inputfile));
            while (br.ready()) {
                line = br.readLine();
                if (line.charAt(0) == '>') {
                    s_key.add(line);
                } else {
                    s_val.add(line);
                }
            }
            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        //将第一个序列作为根，与其他每个序列进行双序列比对，保留第一个序列的比对结果s_out1
        String sequence1 = s_val.get(0);
        int sequenceLen1 = sequence1.length();
        TwoAlign centerAlign = new TwoAlign();
        for (int i=1; i<s_val.size(); i++) {
            centerAlign.align(sequence1, s_val.get(i));
            s_out1.add(new String(TwoAlign.alignResultOne));
            s_out2.add(new String(TwoAlign.aligntResultTwo));
        }

        //统计第一个序列的比对结果，得到它的归总比对结果insertSpace1[]
        int index;
        int insertSpace1[] = new int[sequenceLen1 + 1];
        for (String line2 : s_out1) {
            int tempSpace1[] = new int[sequenceLen1 + 1];
            index = 0;
            for (int j=0; j < line2.length(); j++) {
                if (line2.charAt(j) == '-') {
                    tempSpace1[index]++;
                } else {
                    if (insertSpace1[index] < tempSpace1[index]) {
                        insertSpace1[index]=tempSpace1[index];
                    }
                    index++;
                }
            }
        }

        //以第一条序列为中心序列，计算中心序列sequence1
        StringBuilder stringBuilder = new StringBuilder();
        int insertSpaceLen1 = insertSpace1.length;
        for(int i=0; i<insertSpaceLen1; i++){
            for(int j=0; j<insertSpace1[i]; j++) {
                stringBuilder.append('-');
            }
            if(i != insertSpaceLen1-1) {
                stringBuilder.append(sequence1.charAt(i));
            }
        }
        sequence1 = stringBuilder.toString();

        //将归纳得到的sequence1再次与第一次比对结果比对，得到最终各序列比对结果存入文件
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputfile));
            for (int i=0; i<s_key.size(); i++) {
                bw.write(s_key.get(i));
                bw.newLine();
                if (i == 0) {
                    bw.write(sequence1);
                } else {
                    centerAlign.align(sequence1, s_out2.get(i-1));
                    bw.write(new String(TwoAlign.aligntResultTwo));
                }
                bw.newLine();
                bw.flush();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
