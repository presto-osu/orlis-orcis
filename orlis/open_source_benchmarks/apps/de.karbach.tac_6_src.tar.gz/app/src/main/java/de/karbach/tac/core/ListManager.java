/**
	MoTAC - digital board for TAC board game
    Copyright (C) 2013-2014  Carsten Karbach
    
    Contact by mail carstenkarbach@gmx.de

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package de.karbach.tac.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import android.app.Activity;
import android.content.Context;

/**
 * Manages a list of names. Stores it persistently.
 * Names can be added. The entire list can be cleared.
 * 
 * @author Carsten Karbach
 *
 */
public class ListManager {

	/**
     * Filename for storing, whether a button was used already
     */
    protected static final String filename = "buttonsUsed.txt";
    /**
     * Stores directly in memory, whether a button was used before or not
     */
    protected List<String> listUsed;
    
    /**
     * The activity for storing data
     */
    protected Activity activity;
    
    public ListManager(Activity activity){
    	this.activity = activity;
    	
    	init();
    }
    
    /**
     * Read from File, which buttons were used already
     */
    protected void init(){
    	
    	listUsed = new ArrayList<String>();
    	
    	try {
			FileInputStream input = activity.openFileInput(filename);
			if(input == null){
				return;
			}
    		Scanner in = new Scanner(input);
			while(in.hasNext()){
				String name = in.nextLine();
				listUsed.add(name);
			}
			in.close();
		} catch (FileNotFoundException e) {
		}
    }
    
    /**
     * Check if the given item was used before.
     * @param itemName the name of the requested item
     * @return true, if button was used before, false otherwise
     */
    public boolean wasItemUsed(String itemName){
    	return listUsed.contains(itemName);
    }
    /**
     * Store that the given item name was used.
     * @param itemName the name of the used button
     */
    public void usedItem(String itemName){
    	if(wasItemUsed(itemName)){
    		return;
    	}
    	listUsed.add(itemName);
    	//Store entire new file
    	try {
			FileOutputStream fileoutput = activity.openFileOutput(filename, Context.MODE_PRIVATE);
			if(fileoutput == null){
				return;
			}
    		PrintWriter pw = new PrintWriter( fileoutput );
			for(String button: listUsed){
				pw.println(button);
			}
			pw.close();
		} catch (FileNotFoundException e) {
		}
    }
    
    /**
     * Clear file and list.
     */
    public void clearList(){
    	listUsed.clear();
    	activity.deleteFile(filename);
    }
	
}
