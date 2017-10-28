package com.ldyy.tool;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

public class Img {
	private static  Logger log = Logger.getLogger(Img.class);
	
	public static byte[] pressImg(byte[] b) {
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(b);
			BufferedImage img = ImageIO.read(bis);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(img, "jpg", bos);
			return bos.toByteArray();
		} catch (IOException e) {
			log.error("", e);
			return null;
		}
	}
	
	public static void main(String[] args) {
		byte[] b = FileUtil.readFileBytes("E://doc0.png");
		b = pressImg(b);
		FileUtil.writeFileAll("E://doc0.jpg", b, 0, b.length);
	}
}
