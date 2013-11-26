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

package eu.trentorise.opendata.categorize.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
/**
 * 
 * @author Alberto Zanella <a.zanella@trentorise.eu>
 * @since Last modified by azanella On 26/nov/2013
 */
public class ResourceUtility {
	public static String computeSHA(String filename) throws IOException
	{
		FileInputStream fis = new FileInputStream(new File(filename));
		String retval = org.apache.commons.codec.digest.DigestUtils.shaHex(fis);
		fis.close();
		return retval;
	}
}
