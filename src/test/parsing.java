package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class parsing {
	
	static Map<String, String> dataMap = new HashMap<String, String>();
	static String startRecord = "SZ[";
	static String endRecord = "]SZ";

	public static void main(String[] args) {
		try {
				File file = new File("testdata.txt");
				Scanner scanner = new Scanner(file);
				 
				String line;
				int fileCount = 1;
				String filename = null;
				boolean isStartTrans = false;
				boolean isEndTrans = false;
				
				lineLoop:
				while (scanner.hasNextLine()) {
					
					line = scanner.nextLine();
					if (!line.isEmpty()) {
						
						String checkStart = line.replaceAll("[\\s+]", "");
											
						if(checkStart.contains(startRecord)) {
							
							/* It will check end of record exists*/
							if(checkStart.indexOf(endRecord)>-1 ) {
								
								isEndTrans = true;
								isStartTrans = true;
								
								/*Check for the data appears in the same line of start and end tag*/
								if(checkStart.indexOf(startRecord)<checkStart.indexOf(endRecord)) {
									System.out.println("Start of Transactions");
									filename = "TransactionDetails_"+(fileCount++)+".xlsx";
								}	
								
								/* Case where current record end and next record starts in the same line */
								else {
									
									fetchData(line, isEndTrans);
									writeDataToExcel(filename);
									System.out.println(dataMap);
									dataMap.clear();
									
									filename = "TransactionDetails_"+(fileCount++)+".xlsx";
									isEndTrans = false;
									continue lineLoop;
								}
							}
							
							else { /* Only start of record found get the filename */
								System.out.println("Start of Transactions");
								filename = "TransactionDetails_"+(fileCount++)+".xlsx";
								isStartTrans = true;
							}	
						}
						
						if(isStartTrans) {
							if(line.contains("\""))
								fetchData(line, isEndTrans);
							
							if(line.contains(endRecord)) {
								writeDataToExcel(filename);
								System.out.println(dataMap);
								dataMap.clear();
								isStartTrans = false;
							}
						}					
					}		
				}
		}
		catch(Exception e) {
			System.out.println("Not able to parse the test data due to exception \n");
			System.out.println(e.toString());
		}
		
	}
	
	
	public static void fetchData(String line, boolean isEnd) {
		
		String value;
		String[]  data = null;
		String checkStart = line.replaceAll("[\\s+]", "");
		
		
		if(isEnd && checkStart.indexOf(startRecord)>checkStart.indexOf(endRecord)) {
			String[] pre_data = line.replaceAll("[\\[\\]]", "").split("]]");
			data = pre_data[0].split("\\s+");
		}
		else {
			data = line.replaceAll("[\\[\\]]", "").split("\\s+");	
		}
		
		/* Get the data as key value pair and update the map */
		for (int i=0; i<data.length; i++) {
			if (data[i].startsWith("\"") && data[i].endsWith("\"")) {
				value = data[i].replaceAll("[\"]", "");
				
				if(value.isEmpty())
					value = null;
				dataMap.put(data[i-1], value);
			}	
		}			 
		
	}

	public static void writeDataToExcel(String filename) throws IOException {
		
		int rowNum = 0;
		
		XSSFWorkbook workbook= new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("Transactions");
		
		for(Map.Entry data: dataMap.entrySet()) {	
			XSSFRow row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue((String)data.getKey());
			row.createCell(1).setCellValue((String)data.getValue());
		}
		
		FileOutputStream file = new FileOutputStream(filename);
		workbook.write(file);
		file.close();
		
		System.out.println("Data written to excel");
	}
}
