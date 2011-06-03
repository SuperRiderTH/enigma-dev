package org.enigma;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.main.LGM;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.Resource.Kind;

public class EFileWriter
	{
	public void writeEgmFile(File loc)
		{
		try
			{
			writeEgmFile(new FileOutputStream(loc));
			}
		catch (IOException e)
			{
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		}

	public void writeEgmFile(OutputStream os) throws IOException
		{
		writeEgmFile(new ZipOutputStream(os));
		}

	static Map<Resource.Kind,String> typestrs = new HashMap<Resource.Kind,String>();
	static
		{
		typestrs.put(Kind.SPRITE,"spr");
		typestrs.put(Kind.SOUND,"snd");
		typestrs.put(Kind.BACKGROUND,"bkg");
		typestrs.put(Kind.PATH,"pth");
		typestrs.put(Kind.SCRIPT,"scr");
		typestrs.put(Kind.FONT,"fnt");
		typestrs.put(Kind.TIMELINE,"tml");
		typestrs.put(Kind.OBJECT,"obj");
		typestrs.put(Kind.ROOM,"rom");
		typestrs.put(Kind.GAMEINFO,"inf");
		typestrs.put(Kind.GAMESETTINGS,"set");
		typestrs.put(Kind.EXTENSIONS,"ext");
		}

	public void writeEgmFile(ZipOutputStream os) throws IOException
		{
		ResNode root = LGM.root;
		os.putNextEntry(new ZipEntry("toc.txt"));

		OutputStreamWriter out = new OutputStreamWriter(os); //charset?
		int children = root.getChildCount();
		for (int i = 0; i < children; i++)
			{
			ResNode node = (ResNode) root.getChildAt(i);
			out.write(typestrs.get(node.kind));
			out.write(' ');
			out.write((String) node.getUserObject());
			out.write("\r\n"); //newline?
			}
		out.flush();

		for (int i = 0; i < children; i++)
			{
			String dir = (String) ((ResNode) root.getChildAt(i)).getUserObject() + "/";
			os.putNextEntry(new ZipEntry(dir));
			os.putNextEntry(new ZipEntry(dir + "toc.txt"));
			}

		os.close();
		}

	public void writeSprite()
		{

		}

	APNG readApng(File f) throws IOException
		{
		return readApng(new FileInputStream(f));
		}

	static final long PNG_MAGIC = 0x89504E470D0A1A0AL;
	static final int IHDR_TYPE = 0x49484452;
	static final int IEND_TYPE = 0x49454E44;
	static final int IDAT_TYPE = 0x49444154;

	static final int acTL_TYPE = 0x6163544C;
	static final int fcTL_TYPE = 0x6663544C;
	static final int fdAT_TYPE = 0x66644154;

	APNG readApng(InputStream is) throws IOException
		{
		return null;
		}

	/**
	 * This method attempts to read an animated PNG by disassembling and then repeatedly gluing for ImageIO.
	 * Basically, we pull out the aCTL chunk, and every other DAT chunk except the one we're parsing.
	 * We then repeat this process for each DAT chunk.
	 * @param is
	 * @throws IOException
	 */
	APNG readApngHack(InputStream is) throws IOException
		{
		DataInputStream in = new DataInputStream(is);
		long magic = in.readLong();
		if (magic != PNG_MAGIC) throw new IOException(String.format("%016X",magic) + " != png");

		Chunk c = readChunk(in);
		if (c.id != IHDR_TYPE) throw new IOException(String.format("%08X",c.id) + " != IHDR");

		IHDR h = new IHDR();
		DataInputStream d = new DataInputStream(c.data);
		h.width = d.readInt();
		h.height = d.readInt();
		h.bitDepth = d.readByte();
		h.colType = d.readByte();
		h.compression = d.readByte();
		h.filter = d.readByte();
		h.interlace = d.readByte();
		d.reset();

		APNG r = new APNG(h);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		dos.writeLong(PNG_MAGIC);
		byte[] buf = new byte[d.available()];
		d.read(buf);
		dos.write(buf);

		while ((c = readChunk(in)).id != IEND_TYPE)
			{
			switch (c.id)
				{
				//			case
				}
			}

		return r;
		}

	Chunk readChunk(DataInputStream in) throws IOException
		{
		int size = in.readInt();
		byte[] buf = new byte[size + 4];
		in.read(buf);
		int crc1 = crc(buf), crc2 = in.readInt();
		if (crc1 != crc2) throw new IOException("CRC " + crc1 + " != " + crc2);
		ByteArrayInputStream data = new ByteArrayInputStream(buf);

		byte[] bid = new byte[4];
		data.read(bid);
		int id = (bid[0] << 24) | (bid[1] << 16) | (bid[2] << 8) | bid[3];
		return new Chunk(size,id,data);
		}

	class APNG extends ArrayList<BufferedImage>
		{
		private static final long serialVersionUID = 1L;
		IHDR header;

		public APNG(IHDR h)
			{
			header = h;
			}
		}

	class Chunk
		{
		int size, id;
		InputStream data;

		Chunk(int s, int i, InputStream d)
			{
			size = s;
			id = i;
			data = d;
			}
		}

	class IHDR
		{
		int width, height;
		byte bitDepth, colType, compression, filter, interlace;
		}

	static int crc(byte[] b)
		{
		CRC32 crc = new CRC32();
		crc.update(b);
		return (int) crc.getValue();
		}
	}
