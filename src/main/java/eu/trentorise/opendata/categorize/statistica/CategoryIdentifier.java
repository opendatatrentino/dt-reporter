/**
 * *****************************************************************************
 * Copyright 2012-2013 Trento Rise (www.trentorise.eu/)
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the GNU Lesser General Public License (LGPL)
 * version 2.1 which accompanies this distribution, and is available at
 *
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 *******************************************************************************
 */

package eu.trentorise.opendata.categorize.statistica;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

/**
 * This class identifies the catetgory of a dataset resource coming from the Statistica organization.
 * It identifies following types:
 * 
 * 1-	Anno,cod.ente (0-229, 1001 – 1029, 9999),valore
 * 11-	Anno,cod.ente (0-229, 1001 – 1029),valore
 * 12-	Anno,cod.ente (0-229, [9999]),valore
 * 13-	Anno,cod.ente (1001 – 1029, [9999]),valore
 * 
 * 2-	Anno,Trentino
 * 
 * Other columns are listed as 2# (with # the columns count).
 * 
 * 4-	UNDEFINED

 *   
 * @author Alberto Zanella <a.zanella@trentorise.eu>
 * @since Last modified by azanella On 26/nov/2013
 */
public class CategoryIdentifier {
	public static String getCategoryType(String filepath) {
		try {
			CSVReader cr = new CSVReader(new FileReader(filepath));
			int code = processHeader(cr.readNext());
			if(code == 1)
			{
				String retval = processCodEnte(cr.readAll());
				cr.close();
				return retval;
			}
			else {
				cr.close();
				return Integer.toString(code);
			}
		} catch (FileNotFoundException e) {
			return "4a";
		} catch (IOException e) {
			return "4b";
		}
	}

	private static int processHeader(String[] headers) {
		if (headers.length > 1) {
			if (headers[1].toLowerCase().equals("codente")) {
				return 1;
			} else if (headers[1].toLowerCase().equals("trentino")) {
				if (headers.length > 2) {
					return 20 + headers.length - 1;
				} else {
					return 2;
				}
			} else
				return 42;
		} else
			return 41;
	}

	private static String processCodEnte(List<String[]> rawcsv) {
		boolean group999 = false;
		boolean group1999 = false;
		boolean group9999 = false;
			for (String[] row : rawcsv) {
			try {
				int codente = Integer.parseInt(row[1]);
				if (codente < 999)
					group999 = true;
				else if ((codente > 999) && (codente < 1999))
					group1999 = true;
				else if (codente == 9999)
					group9999 = true;
			} catch (NumberFormatException e) {	}
			}
			if (group999 && group1999) {
				if (group9999) {
					return "1";
				} else {
					return "11";
				}
			}
			else if(group999 && !group1999)
			{
				return "12";
			}
			else if(group1999 && !group999)
			{
				return "13";
			}
			else return "4c";

	}

}
