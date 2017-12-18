package com.malab.protein;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class ProteinMR {
	public static class TwoAlignMapper extends Mapper<Object, Text, Text, Text> {
		String sequence1 = "";
		boolean isSequence1 = false;
		
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			String sequence = "";
			if (!isSequence1) {
				if (value.charAt(0) != '>') {
					sequence1 = new String(value.getBytes());
					isSequence1 = true;
				}
			} else {
				if (value.charAt(0) != '>') {
					sequence = new String(value.getBytes());
					TwoAlign centerAlign = new TwoAlign();
					centerAlign.align(sequence1, sequence);
					context.write(new Text(new String(TwoAlign.alignResultOne)), 
							new Text(new String(TwoAlign.aligntResultTwo)));
				}
			}
		}
	}
}
