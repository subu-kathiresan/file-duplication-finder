package com.subu.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;

/**
 * 
 * @author Kathiresan Subu
 * @date Feb 15, 2020
 */

public class DuplicateFileFilter {

	private static String outFolder = "out";
	private static HashMap<String, String> fileMap = new HashMap<String, String>();

	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

		if (args.length > 1) {
			outFolder = args[1];
		}

		File directory = new File(String.valueOf(outFolder));

		if (!directory.exists()) {
			directory.mkdir();
		}

		// create hash for files in output folder
		processOutFolder(new File(outFolder));

		System.out.println("-----------------------------------");

		// check files in input folders and copy if not duplicate
		for (String folder : args[0].split(",")) {
			processInFolder(new File(folder));
		}
	}

	public static void processOutFolder(final File folder) throws NoSuchAlgorithmException, IOException {
		for (final File fileEntry : folder.listFiles()) {
			if (!fileEntry.isHidden()) {
				String hash = generateFileHash(fileEntry);
				if (fileMap.containsKey(hash)) {
					System.out.println("duplicate   : " + fileEntry.getAbsolutePath());
				} else {
					fileMap.put(hash, fileEntry.getAbsolutePath());
				}
			}
		}
	}

	public static void processInFolder(final File folder) throws NoSuchAlgorithmException, IOException {
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				processInFolder(fileEntry);
			} else {
				if (!fileEntry.isHidden()) {
					String hash = generateFileHash(fileEntry);
					if (fileMap.containsKey(hash)) {
						System.out.println("duplicate   : " + fileEntry.getAbsolutePath());
					} else {
						fileMap.put(hash, fileEntry.getAbsolutePath());
						copyFile(fileEntry.getAbsolutePath(), outFolder + "/" + fileEntry.getName());
					}
				}
			}
		}
	}

	private static String getFileNameWithoutExt(File file) {
		String fileName = "";

		try {
			if (file != null && file.exists()) {
				String name = file.getName();
				fileName = name.replaceFirst("[.][^.]+$", "");
			}
		} catch (Exception e) {
			e.printStackTrace();
			fileName = "";
		}
		return fileName;
	}

	private static String getFileExt(File file) {
		return file.getName().substring(file.getName().lastIndexOf("."));
	}

	public static void copyFile(String from, String to) throws IOException {
		File file = new File(to);
		if (file.exists()) {
			to = outFolder + "/" + getFileNameWithoutExt(file) + "-" + new Date().getTime() + getFileExt(file);
		}

		Path src = Paths.get(from);
		Path dest = Paths.get(to);
		Files.copy(src, dest);
		System.out.println("copied      : " + from + " to " + to);
	}

	public static String generateFileHash(File file) throws NoSuchAlgorithmException, IOException {
		byte[] buffer = new byte[8192];
		int count;
		Base64.Encoder encoder = Base64.getEncoder();
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

		while ((count = bis.read(buffer)) > 0) {
			digest.update(buffer, 0, count);
		}
		bis.close();

		byte[] hash = digest.digest();
		String strHash = new String(encoder.encode(hash));
		System.out.println("processing  : " + file.getAbsolutePath() + " (" + strHash + ")");
		return strHash;
	}
}