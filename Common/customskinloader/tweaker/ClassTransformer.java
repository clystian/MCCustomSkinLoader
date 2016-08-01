package customskinloader.tweaker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;

import customskinloader.utils.MinecraftUtil;
import net.minecraft.launchwrapper.IClassTransformer;

public class ClassTransformer implements IClassTransformer {

	private ZipFile zipFile = null;
	private ArrayList<String> classes=new ArrayList<String>();

	public ClassTransformer() {
		ModSystemTweaker.logger.info("ClassTransformer Begin");
		try {
			URLClassLoader ucl = (URLClassLoader)this.getClass().getClassLoader();
			URL urls[] = ucl.getURLs();
			for (URL url : urls) {
				ZipFile tempZipFile = getZipFile(url);
				if(tempZipFile==null)
					continue;
				if (tempZipFile.getEntry("customskinloader/tweaker/ClassTransformer.class") == null){
					tempZipFile.close();
					continue;
				}
				zipFile = tempZipFile;
				Enumeration<ZipEntry> entries=(Enumeration<ZipEntry>) zipFile.entries();
				StringBuilder sb=new StringBuilder();
				while(entries.hasMoreElements()){
					ZipEntry entry=entries.nextElement();
					String name=entry.getName();
					if(name.endsWith(".class")&&!name.contains("/")){
						classes.add(name);
						sb.append(" ");
						sb.append(name);
					}
				}
				ModSystemTweaker.logger.info("Jar File URL: " + url);
				ModSystemTweaker.logger.info("Classes:" + sb.toString());
				break;
			}
		} catch (Exception e) {
			ModSystemTweaker.logger.warning(e);
		}
		if (zipFile == null) {
			ModSystemTweaker.logger.info("Can not find JAR in the classpath.");
		}
	}

	private static ZipFile getZipFile(URL url)
	{
		if(MinecraftUtil.isCoreFile(url))
			return null;
		ZipFile zipFile0=null;
		try {
			File file = new File(url.toURI());
			if(!file.exists()||!file.isFile())
				return null;
			zipFile0 = new ZipFile(file);
			return zipFile0;
		} catch (Exception e) {
			ModSystemTweaker.logger.warning(e);
		}
		return null;
	}

	public byte[] transform(String name, String transformedName, byte bytes[]) {
		if (zipFile == null)
			return bytes;
		
		String fullName = name + ".class";
		if(!classes.contains(fullName))
			return bytes;
		ZipEntry ze = zipFile.getEntry(fullName);
		if (ze == null)
			return bytes;
		byte diBytes[] = getClass(ze);
		if (diBytes != null) {
			ModSystemTweaker.logger.info("Class '" + name + "'("+transformedName+") transformed.");
			return diBytes;
		}
		else
			return bytes;
	}

	private byte[] getClass(ZipEntry ze) {
		try {
			InputStream is = zipFile.getInputStream(ze);
			byte[] bytes = IOUtils.toByteArray(is);
			if ((long)bytes.length == ze.getSize())
				return bytes;
			ModSystemTweaker.logger.info("Failed: " + ze.getName() + " " + bytes.length + " / " + ze.getSize());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
		
	}
}
