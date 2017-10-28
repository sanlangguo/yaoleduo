package com.ldyy.tool;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

//flag can`t be chinese
public class EndStream {
	private InputStream is = null;
	private byte[] b = new byte[2048];
	private int len = 0;

	public EndStream(InputStream is) {
		this.is = is;
	}

	public String readE(String flag) {
		return new String(readEnd(flag));
	}

	public byte[] readEnd(String flag) {
		try {
			byte[] bc = new byte[2048];
			int lenc = 0;
			int rst;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			baos.write(b, 0, len);
			while (true) {
				lenc = is.read(bc);
				if (lenc == -1) {
					lenc = 0;
					// here have a bug,stream is end,I need the method behind.
					// do a flag to stop it.
				}
				baos.write(bc, 0, lenc);
				if ((rst = Bytes.indexOf(baos.toByteArray(), flag)) < 0) {

				} else {
					byte[] br = new byte[rst];
					System.arraycopy(baos.toByteArray(), 0, br, 0, rst);
					len = baos.toByteArray().length - rst - flag.length();
					b = new byte[len];
					System.arraycopy(baos.toByteArray(), rst + flag.length(), b, 0, len);
					return br;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public byte[][] readStop(String flag) {
		try {
			byte[] bc = new byte[2048];
			int lenc = 0;
			int rst;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			lenc = is.read(bc);
			if (lenc == -1) {
				lenc = 0;
				// here have a bug,stream is end,I need the method behind.
				// do a flag to stop it.
			}
			baos.write(b, 0, len);
			baos.write(bc, 0, lenc);
			if ((rst = Bytes.indexOf(baos.toByteArray(), flag)) < 0) {
				byte[] br = new byte[len];
				System.arraycopy(b, 0, br, 0, len);
				b = bc;
				len = lenc;
				return new byte[][] { new byte[] { 1 }, br };
			} else {
				byte[] br = new byte[rst];
				System.arraycopy(baos.toByteArray(), 0, br, 0, rst);
				len = baos.toByteArray().length - rst - flag.length();
				b = new byte[len];
				System.arraycopy(baos.toByteArray(), rst + flag.length(), b, 0, len);
				return new byte[][] { new byte[] { 0 }, br };
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
