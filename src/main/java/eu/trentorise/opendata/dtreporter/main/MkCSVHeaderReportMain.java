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

package eu.trentorise.opendata.dtreporter.main;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.ckan.CKANException;
import org.ckan.Client;
import org.ckan.Connection;
import org.ckan.resource.impl.Dataset;
import org.ckan.resource.impl.Resource;

import eu.trentorise.opendata.downloader.Downloader;



import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * This class analyze all csv files presents on the catalog and extracts header column. It produces a report having the following structure:
 * headerColumn;fileName;resourceName
 * headerColumn is normalized (trim and lowercase)
 * @author Alberto Zanella <a.zanella@trentorise.eu>
 *
 */
public class MkCSVHeaderReportMain {
	
	private static String downloadDirPath = "tmp/";
	
	private static String[] extractHeadersFromFile(String fileName) throws IOException
	{
		CSVReader reader = new CSVReader(new FileReader(fileName));
		String[] retval =  reader.readNext();
		reader.close();
		return retval;
	}
	
	private static void processResource(Resource r, CSVWriter cw, String dsname) throws Exception
	{
		Downloader dwn = Downloader.getInstance();
		dwn.setFilepath(downloadDirPath);
		dwn.setUrl(r.getUrl());
		dwn.download();
		if (dwn.getFilename().toLowerCase().trim().endsWith(".zip")) {
			throw new Exception("ZIP File -- Skipped");
		}
		String[] fields = extractHeadersFromFile(downloadDirPath+dwn.getFilename());
		new File(downloadDirPath+dwn.getFilename()).delete();
		for (String field : fields) {
			cw.writeNext(new String[] {field.trim().toLowerCase(), dwn.getFilename(), dsname});
		}
	}
	
	private static void writeHeaders(CSVWriter cw)
	{
		cw.writeNext(new String[] {"headerColumn","fileName","resourceName"});
	}
	
	public static void main(String[] args)
	{
		Client ccl = new Client(new Connection("http://dati.trentino.it"), null);
		File dp = new File(downloadDirPath);
		dp.mkdirs();
		List<String> datasets;
		datasets = null;
		try {
			datasets = ccl.getDatasetList().result;
		} catch (CKANException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String csvf = "output.csv";
		FileWriter fw = null;
		try {
			fw = new FileWriter(csvf);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			fw.write("\"#(c) 2013 TrentoRISE -- Created by dt-reporter\"\n");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		CSVWriter csv = null;
		csv = new CSVWriter(fw,';');
		writeHeaders(csv);
		for (String dsname : datasets) {
			try {
				Dataset ds = ccl.getDataset(dsname);
				for (Resource r : ds.getResources()) {
					String format = r.getFormat().toLowerCase();
					if ((format.contains("csv")) || (format.contains("tsv")))
					{
						processResource(r, csv,ds.getName());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		dp.delete();
	}
	
}
