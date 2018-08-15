//This file is part of RiverFerry.
//
//RiverFerry is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//RiverFerry is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with RiverFerry.  If not, see <http://www.gnu.org/licenses/>.
//
//Author: Matthieu Decorde 
//Contact: mdecorde.riverferry@gmail.com

package fr.hnit.riverferry;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class BuildSchedules {
	File tsvFile;
	String encoding;

	/**
	 * 
	 * @param tsvFile Tabulation separator and no text separator
	 * @param encoding
	 */
	BuildSchedules(File tsvFile, String encoding) {
		this.tsvFile = tsvFile;
		this.encoding = encoding;
	}

	/**
	 * Creates the Schedules Object
	 * 
	 * @return The schedules
	 * @throws Exception
	 */
	public Schedules createHoraires() throws Exception {

		Schedules schedules = new Schedules();

		FileInputStream freader = new FileInputStream(tsvFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(freader, encoding));

		// first line is header, skip it
		String line = reader.readLine();

		// read the TSV file
		line = reader.readLine();
		while (line != null) {
			String[] split = line.split("\t");
			int ncols = split.length;
			if (ncols >= 3) {
				if (!schedules.containsKey(split[0])) {
					Bank rive = Bank.valueOf(split[1]);
					schedules.put(split[0], new DepartureTimeList(split[0], rive));
					if (rive.equals(Bank.LEFT)) {
						schedules.lefts.add(split[0]);
					} else {
						schedules.rights.add(split[0]);
					}
					
				} 
				
				DepartureTimeList hl = schedules.get(split[0]);
				
				ArrayList<DepartureTime> times = new ArrayList<DepartureTime>();
				for (int c = 3 ; c < ncols ; c++) {
					DepartureTime h = new DepartureTime(split[c]);
					times.add(h);
				}
				
				String[] days = split[2].split(","); // get days for the column
				for (String day : days) {
					hl.put(Integer.parseInt(day), times);				
				}
			}
			
			line = reader.readLine();
		}
		reader.close();

		return schedules;
	}

	public static void main(String args[]) {
		File csvFile = new File("C:\\Documents and Settings\\H\\Bureau\\BAC76_2013.csv");
		File binFile = new File("E:\\workspaceAndroid\\BAC76\\res\\xml\\Horaires.bin");
		String encoding = "UTF-8";
		BuildSchedules bh = new BuildSchedules(csvFile, encoding);
		try {
			Schedules schedules = bh.createHoraires();
			//System.out.println(schedules);
			Schedules.write(binFile, schedules);
			System.out.println("Result saved in "+binFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
