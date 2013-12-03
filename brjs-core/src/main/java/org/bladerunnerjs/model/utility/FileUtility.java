package org.bladerunnerjs.model.utility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileUtility {
	public static File createTemporaryDirectory(String prependedFolderName) throws IOException
	{
		if (prependedFolderName.contains("/"))
		{
			throw new IOException("prependedFolderName can't contain a /");
		}
		final File tempdir = File.createTempFile(prependedFolderName, "");
		tempdir.delete();
		tempdir.mkdir();	
		Runtime.getRuntime().addShutdownHook(new DeleteTempFileShutdownHook(tempdir));
		return tempdir;
	}
	
	public static List<File> getSortedListOfSubFolders(File sourceFolder)
	{
		List<File> subfolders = new ArrayList<File>();
		
		for(File content : sortFileArray(sourceFolder.listFiles()))
		{
			if(content.isDirectory() && !content.getName().startsWith("."))
			{
				subfolders.add(content);
			}
		}
		
		return subfolders;
	}
	
	public static File[] sortFileArray(File[] files)
	{
		if (files == null)
		{
			return new File[]{};
		}
		else
		{
			List<File> filesList = Arrays.asList(files);
			Collections.sort(filesList, new Comparator<File>()
			{
				@Override
				public int compare(File file1, File file2)
				{
					return file1.getAbsolutePath().compareTo(file2.getAbsolutePath());
				}
			});
			return (File[]) filesList.toArray();
		}
	}
}
