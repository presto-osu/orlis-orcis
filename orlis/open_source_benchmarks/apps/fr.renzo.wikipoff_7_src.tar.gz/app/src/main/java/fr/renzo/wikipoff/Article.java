/*

Copyright 2014 "Renzokuken" (pseudonym, first committer of WikipOff project) at
https://github.com/conchyliculture/wikipoff

This file is part of WikipOff.

    WikipOff is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    WikipOff is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with WikipOff.  If not, see <http://www.gnu.org/licenses/>.

*/
package fr.renzo.wikipoff;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Article {
	public Wiki wiki;
	public String title;
	public int id_;
	public String text;

	public  Article(int id_, String title, byte[] coded, Wiki wiki) {
		this.id_ = id_;
		this.title = title;
		this.text = decodeBlob(coded);
		this.wiki = wiki;
	}
	
	private String decodeBlob(byte[]coded) {
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		try {
			int propertiesSize = 5;
			byte[] properties = new byte[propertiesSize];
			
			InputStream inStream = new ByteArrayInputStream(coded);
			
			OutputStream outStream = new BufferedOutputStream(baos,1024*1024);
			SevenZip.Compression.LZMA.Decoder decoder = new SevenZip.Compression.LZMA.Decoder();

			if (inStream.read(properties, 0, propertiesSize) != propertiesSize)
				throw new Exception("input .lzma file is too short");
			if (!decoder.SetDecoderProperties(properties))
				throw new Exception("Incorrect stream properties");
			long outSize = 0;
			for (int i = 0; i < 8; i++)
			{
				int v = inStream.read();
				if (v < 0)
					throw new Exception("Can't read stream size");
				outSize |= ((long)v) << (8 * i);
			}
		
			if (!decoder.Code(inStream, outStream, outSize))
				throw new Exception("Error in data stream");
			outStream.flush();
			outStream.close();
			inStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return baos.toString();
	}
}
