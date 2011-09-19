package org.cmdbuild.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtils {

	public static String getContents(String file) {
		File aFile = new File(file);

		StringBuilder contents = new StringBuilder();

		try {
			BufferedReader input =  new BufferedReader(new FileReader(aFile));
			try {
				String line = null;
				while (( line = input.readLine()) != null){
					contents.append(line);
					contents.append(System.getProperty("line.separator"));
				}
			}
			finally {
				input.close();
			}
		}
		catch (IOException ex){
			ex.printStackTrace();
		}

		return contents.toString();
	}
	
	public static String getContents(InputStream is) {
		StringBuffer sbuf = new StringBuffer();
		BufferedReader bufread = new BufferedReader(new InputStreamReader(is));
		
		try{
			try{
				String line = null;
				while ((line=bufread.readLine()) != null){
					sbuf.append(line);
					sbuf.append(System.getProperty("line.separator"));
				}
			} finally {
				bufread.close();
			}
		} catch(IOException ex){ex.printStackTrace();}
		
		return sbuf.toString();
	}
}
