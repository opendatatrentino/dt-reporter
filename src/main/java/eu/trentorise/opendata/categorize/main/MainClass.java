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

package eu.trentorise.opendata.categorize.main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.ckan.CKANException;
import org.ckan.Client;
import org.ckan.Connection;
import org.ckan.resource.impl.Dataset;


import eu.trentorise.opendata.categorize.statistica.StatisticaProcessor;

/**
 * This is the main class for the categorization part of the project.
 * It cycle over all datasets and process ones that have an associated processor.
 * At the moment it process statistica ones.
 * Each processor export its results. At the moment the StatisticaProcessor export
 * analysis in CSV format.
 * @author Alberto Zanella <a.zanella@trentorise.eu>
 * @since Last modified by azanella On 26/nov/2013
 */
public class MainClass {
	private static String downloadDirPath = "tmp/";
	public static void main(String[] args)
	{
		Client ccl = new Client(new Connection("http://dati.trentino.it"), null);
		File dp = new File(downloadDirPath);
		dp.mkdirs();
		List<String> datasets = new ArrayList<String>();
		try {
			datasets = ccl.getDatasetList().result;	
			
		} catch (CKANException e) {		}
		StatisticaProcessor sp = new StatisticaProcessor("tmp/", "statistica.csv");
		for (String dsname : datasets) {
			try {
				Dataset ds = ccl.getDataset(dsname);
				if(StatisticaProcessor.isDatasetOfStatistica(ccl, ds))
				{
					sp.processDataset(ds);
				}
			} catch (Exception e) { 	
				e.printStackTrace();
			}
		}
		sp.exportCSVFile();
	}
}
