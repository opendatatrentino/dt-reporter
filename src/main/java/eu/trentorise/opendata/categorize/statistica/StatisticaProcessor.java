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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ckan.Client;
import org.ckan.resource.impl.Dataset;
import org.ckan.resource.impl.Group;
import org.ckan.resource.impl.Organization;
import org.ckan.resource.impl.Resource;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import eu.trentorise.opendata.categorize.models.OutputCSVModel;
import eu.trentorise.opendata.categorize.utility.ResourceUtility;
import eu.trentorise.opendata.downloader.Downloader;

/**
 * This is a processor for statistical dataset. 
 * @author Alberto Zanella <a.zanella@trentorise.eu>
 * @since Last modified by azanella On 26/nov/2013
 */
public class StatisticaProcessor {
	private static final String ORGANIZATION_NAME = "statistica";
	private static Organization oz = null;
	private static Group gp = null;

	private boolean toUpdate;
	private Resource currentResource;
	private Dataset currentDataset;
	private String downloadDirPath;
	private String outputFileName;
	private ArrayList<String> headers;
	private Map<String, OutputCSVModel> output;

	/**
	 * 
	 * @param downloadDirPath - Directory where temporary file can be downloaded
	 * @param outputFileName - Complete path and filename for the output CSV file
	 */
	public StatisticaProcessor(String downloadDirPath, String outputFileName) {
		toUpdate = false;
		this.downloadDirPath = downloadDirPath;
		this.outputFileName = outputFileName;
		reset();
	}
	
	/**
	 * Reset after exporting
	 */
	private void reset()
	{
		toUpdate = false;
		headers = new ArrayList<String>();
		output = new HashMap<String, OutputCSVModel>();
		importPreviousOutput();
	}

	/**
	 * This method process a dataset identified as a dataset from "statistica"
	 * @param ds - Dataset CKAN object
	 */
	public void processDataset(Dataset ds) {
		if(toUpdate) reset();
		for (Resource r : ds.getResources()) {
			if (r.getFormat().toLowerCase().contains("csv")) {
				currentDataset = ds;
				currentResource = r;
				processResource();
			}
		}
	}

	private void processResource() {
		Downloader dwn = Downloader.getInstance();
		dwn.setFilepath(downloadDirPath);
		dwn.setUrl(currentResource.getUrl());
		dwn.download();
		OutputCSVModel toadd = getOutputResource(currentResource.getId());
		if (isModifiedResource(toadd,dwn.getFilename())) {
			if (toadd.getLastDate().equals("D")) {
				toadd.setLastDate("U");
			}
			toadd.setType(CategoryIdentifier.getCategoryType(downloadDirPath
					+ "/" + dwn.getFilename()));
		} else {
			toadd.setLastDate("S");
		}
		output.put(currentResource.getId(), toadd);
		new File(downloadDirPath + "/" + dwn.getFilename()).delete();

	}

	private void addEmpty(OutputCSVModel om)
	{
		if(om.getDates().isEmpty() && (headers.size() - 5) > 0)
		{
			ArrayList<String> emptydates = new ArrayList<String>();
			for (int i = 0; i < headers.size() - 5; i++) {
				emptydates.add("");
			}
			om.setDates(emptydates);
		}
	}
	
	private ArrayList<String> createHeaders() {
		ArrayList<String> retval = new ArrayList<String>();
		retval.add("Resource ID");
		retval.add("Resource Name");
		retval.add("Dataset Name");
		retval.add("Classification type");
		retval.add("SHA");
		return retval;
	}

	/**
	 * Export CSV file in outputFileName specified in the constructor method.
	 */
	public void exportCSVFile() {
		CSVWriter cw = writerInizializer();
		if (headers.isEmpty()) {
			headers = createHeaders();
		}
		headers.add(new Date().toString());
		cw.writeNext(headers.toArray(new String[headers.size()]));
		for (OutputCSVModel towrite : output.values()) {
			ArrayList<String> input = new ArrayList<String>();
			input.add(towrite.getResourceId());
			input.add(towrite.getResourceName());
			input.add(towrite.getDatasetName());
			input.add(towrite.getType());
			input.add(towrite.getSha());
			if (towrite.getDates() != null) {
				input.addAll(towrite.getDates());
			}
			input.add(towrite.getLastDate());
			cw.writeNext((input.toArray(new String[input.size()])));
		}
		try {
			cw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new File(outputFileName).delete();
		new File(outputFileName+".new").renameTo(new File(outputFileName));
		toUpdate = true;
	}

	private CSVWriter writerInizializer() {
		String csvf = outputFileName + ".new";
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
		csv = new CSVWriter(fw, ';');
		return csv;
	}

	private OutputCSVModel getOutputResource(String resourceId) {
		if (output.containsKey(resourceId)) {
			return output.get(resourceId);
		} else {
			OutputCSVModel retval = new OutputCSVModel();
			retval.setResourceId(resourceId);
			retval.setLastDate("A");
			addEmpty(retval);
			retval.setDatasetName(currentDataset.getName());
			retval.setResourceName(currentResource.getName());
			return retval;
		}
	}

	private void importPreviousOutput() {
		CSVReader cr;
		try {
			cr = new CSVReader(new FileReader(outputFileName),';');
			List<String[]> raw = cr.readAll();
			raw.remove(0);
			Collections.addAll(headers, raw.remove(0));
			for (String[] entry : raw) {
				OutputCSVModel add = new OutputCSVModel();
				add.setResourceId(entry[0]);
				add.setResourceName(entry[1]);
				add.setDatasetName(entry[2]);
				add.setType(entry[3]);
				add.setSha(entry[4]);
				ArrayList<String> dates = new ArrayList<String>();
				Collections.addAll(dates,
						Arrays.copyOfRange(entry, 5, entry.length));
				add.setDates(dates);
				output.put(entry[0], add);
			}
			cr.close();
		} catch (FileNotFoundException e) {
			output = new HashMap<String, OutputCSVModel>();
		} catch (IOException e) {
			output = new HashMap<String, OutputCSVModel>();
		}
	}

	private boolean isModifiedResource(OutputCSVModel old, String filename) {
		String sha;
		try {
			sha = ResourceUtility.computeSHA(downloadDirPath+"/"+filename);
			boolean retval = !sha.equals(old.getSha());
			old.setSha(sha);
			return retval;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return false;
		}
		
	}

	/**
	 * This method is used to identify if a dataset can be processed with this processor.
	 * If so, this class can be instantiated and the method processDataset can be used to analyze the dataset.
	 * @param cl -- instance of CKAN client connected to the dati.trentino.it catalog
	 * @param ds -- Dataset CKAN object
	 * @return true if the dataset is coming from "statistica", false otherwise.
	 */
	public static boolean isDatasetOfStatistica(Client cl, Dataset ds) {
		try {
			if (oz == null) {
				oz = cl.getOrganization(ORGANIZATION_NAME);
			}
			return ds.getOrganization().equals(oz);
		} catch (Exception e) {
			// Compatibility with dati.trentino.it v1.8 standard
			try {
				if (gp == null) {
					gp = cl.getGroup(ORGANIZATION_NAME);
				}
				return ds.getGroups().contains(gp);
			} catch (Exception e1) {
				return false;
			}
		}
	}

}
