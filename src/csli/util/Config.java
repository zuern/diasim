/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Danilo Mirkovic
 * @date Aug 21, 2003
 */
public class Config {
	/**
	 * Main config file for the application. Should be set when the application starts.
	 */
	public static Config main = null; // main config file

	private static String configDir = "lib/config";

	private static Hashtable<String, Config> configs = new Hashtable<String, Config>();

	/**
	 * Returns the config object for the specified filename. Efficient -- if it's not available in memory, reads it from
	 * a file in the config directory. Returns null if the file does not exist.
	 * 
	 * @param filename
	 * @return
	 */
	public static Config getConfig(String filename) {
		Config c = (Config) configs.get(filename);
		if (c == null) {
			// Construct/initialize a new config file
			c = new Config();
			c.setFilename(filename);
			c.configProperties = getProperties(filename);
			if (c.configProperties == null)
				return null;

			// Put in the table of config files
			configs.put(filename, c);
		}
		return c;
	}

	public static String getConfigDir() {
		return configDir;
	}

	public static Config getMainConfig() {
		return main;
	}

	public static void setConfigDir(String path) {
		configDir = path;
	}

	/**
	 * Sets the main properties file. The path to this file will be considered a path where all other config files
	 * reside.
	 */
	public static void setMainConfigFile(String filename) {
		main = getConfig(filename);
		if (main.equals(null)) {
			System.err.println("ERROR: Config file " + filename + " not found!");
		}
		if (main.getBoolean("run.minpriority")) {
			Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		}
	}

	public static void setMainEmptyConfig() {
		main = new Config();
	}

	/**
	 * @return the main config properties, freshly reloaded from file
	 */
	public static Config reloadMainConfig() {
		main = reloadConfig(main.getFilename());
		return main;
	}

	/**
	 * @param filename
	 * @return the specified config properties, freshly reloaded from filename
	 */
	public static Config reloadConfig(String filename) {
		configs.remove(filename);
		return getConfig(filename);
	}

	private static Properties getProperties(String filename) {
		Properties myProps = new Properties();

		// Load properties
		try {
			// if the .local.config doesn't exist, try the regular .config
			if (filename.endsWith(".local.config")) {
				File f = new File(configDir + File.separator + filename);
				if (!f.exists()) {
					filename = filename.substring(0, filename.length() - ".local.config".length()) + ".config";
					System.out.println(f.getAbsolutePath() + " does not exist, trying " + filename);
				}

			}

			// Load own properties
			FileInputStream configStream = new FileInputStream(configDir + File.separator + filename);
			System.out.println("Loading config: " + configDir + File.separator + filename);
			myProps.load(configStream);
			configStream.close();

			// Inherited Properties:
			// One can specify multiple files in the inherits field, separated
			// by a comma. Any properties already specified in the current
			// config will not be overridden by inherited properties. However,
			// in the list of inherited configs, a config listed earlier in the
			// list will override those specified in a later config.
			String str = myProps.getProperty("inherits");
			if (str != null) {
				String[] allParents = str.split(",");
				for (int cnt = 0; cnt < allParents.length; cnt++) {
					String parentFilename = allParents[cnt];
					if (parentFilename != null) {
						Properties parent = getProperties(parentFilename);

						Iterator it = parent.keySet().iterator();
						while (it.hasNext()) {
							String parentKey = (String) it.next();
							if (parentKey.equals("inherits")) {
								continue;
							}
							if (myProps.getProperty(parentKey) == null) {
								myProps.setProperty(parentKey, parent.getProperty(parentKey));
							}
						}
					}
				}
			}
		} catch (Exception e) {
			System.err.println("ERROR: Cannot read config file: " + configDir + File.separator + filename);
			e.printStackTrace();

			return null;
		}
		return myProps;
	}

	private static void printProperties(Properties p) {
		ArrayList<String> list = new ArrayList(p.keySet());
		Collections.sort(list);
		for (Iterator i = list.iterator(); i.hasNext();) {
			String key = (String) i.next();
			if (key.equals("inherits")) {
				continue;
			}
			System.out.println("    " + key);
			System.out.println("        -> " + p.getProperty(key));
		}
	}

	private Properties configProperties;

	private String filename;

	private Config() {
		configProperties = new Properties();
		configs.put("", this);
	}

	private Config(String filename) {
		setFilename(filename);
		configProperties = getProperties(filename);
		System.out.println("Current configuration parameters: ");
		printProperties(configProperties);
		configs.put(filename, this);
	}

	/**
	 * Returns the config property, null if it doesn't exist.
	 * 
	 * @return
	 */
	public String get(String key) {
		String value = (String) configProperties.get(key);
		if (value != null && value.startsWith("reflection:")) {
			Object o = getFromReflection(value);
			if (o != null)
				return o.toString();
			else
				return "key_not_found";
		}
		return value;
	}

	/**
	 * If the value of the key is true, return true, otherwise return false;
	 */
	public boolean getBoolean(String key) {
		String s = get(key);
		return (s != null && s.equalsIgnoreCase("true"));
	}

	/**
	 * Returns a list of Booleans corresponding to the key, which is specified either as a filename, or comma delimited,
	 * in square brackets. e.g. key=[true, false, false, true] key=file:file.foo
	 * 
	 * @param key
	 * @return the List, or null if the key or specified file do not exist. Non-boolean entries in the list (or file)
	 *         will get the value false
	 */
	public List<Boolean> getBooleanList(String key) {
		List<String> rawList = getList(key);
		if (rawList == null) {
			return null;
		}
		ArrayList<Boolean> list = new ArrayList<Boolean>(rawList.size());
		for (String s : rawList) {
			list.add(s.equalsIgnoreCase("true") ? Boolean.TRUE : Boolean.FALSE);
		}
		return list;
	}

	/**
	 * Returns object in field or from method referenced by value.
	 */
	private Object getFromReflection(String value) {

		Pattern format = java.util.regex.Pattern.compile("^(reflection:)?([\\w\\.]+)\\.(\\w+)(\\(\\))?$");
		Matcher m = format.matcher(value.trim());
		if (!m.matches()) {
			System.err.println("WARNING: " + filename + ":" + value + " is not a valid method or field name");
			return null;
		}

		String className = m.group(2);
		String methodName = m.group(3);
		boolean isMethod = "()".equals(m.group(4));

		Class class1;
		Object o = null;
		try {
			class1 = Class.forName(className);
			if (isMethod) {
				Method method = class1.getMethod(methodName, new Class[0]);
				o = method.invoke(null, new Object[0]);
				return o;
			} else {
				Field f = class1.getField(methodName);
				o = f.get(null);
			}
		} catch (ClassNotFoundException e) {
			System.err.println("WARNING: Config," + value + ": Cannot find class " + className);
		} catch (NoSuchMethodException e) {
			System.err.println("WARNING: Config," + value + ": Cannot find method " + value);
		} catch (IllegalArgumentException e) {
			System.err.println("WARNING: Config," + value + ": Error invoking method " + value);
		} catch (IllegalAccessException e) {
			System.err.println("WARNING: Config," + value + ": Error invoking method " + value);
		} catch (InvocationTargetException e) {
			System.err.println("WARNING: Config," + value + ": Error invoking method " + value);
		} catch (SecurityException e) {
			System.err.println("WARNING: Config," + value + ": Cannot access field " + value);
		} catch (NoSuchFieldException e) {
			System.err.println("WARNING: Config," + value + ": Cannot find field " + value);
		}

		return o;
	}

	/**
	 * Assumes the key's value is a list of file paths, or a file containing such a list (see {@link #getList(String)})
	 * and returns them as a list of Files. Just calls getList(key) and appropriately converts to file using same helper
	 * as {@link #getRelativeFile(String)}
	 * 
	 * @param key
	 * @return a list of File objects
	 */
	public List<File> getFileList(String key) {
		return getRelativeFileList(key);
	}

	/**
	 * Assumes the key's value is a list of file paths, or a file containing such a list (see {@link #getList(String)})
	 * and returns them as a list of Files. Just calls getList(key) and appropriately converts to file using same helper
	 * as {@link #getRelativeFile(String)}
	 * 
	 * @param key
	 * @return a list of File objects, or null on error
	 */
	public List<File> getRelativeFileList(String key) {
		List<String> list = getList(key);
		if (list == null) {
			return null;
		}
		List<File> fileList = new ArrayList<File>(list.size());
		for (String path : list) {
			fileList.add(getRelativeFile(path));
		}
		return fileList;
	}

	/**
	 * Assumes the key's value is a list of file paths, or a file containing such a list (see {@link #getList(String)})
	 * and returns them as a list of Files. Just calls getList(key) and appropriately converts to file using same helper
	 * as {@link #getAbsoluteFile(String)}
	 * 
	 * @param key
	 * @return a list of File objects, or null on error
	 */
	public List<File> getAbsoluteFileList(String key) {
		List<String> list = getList(key);
		if (list == null) {
			return null;
		}
		List<File> fileList = new ArrayList<File>(list.size());
		for (String path : list) {
			fileList.add(getAbsoluteFile(path));
		}
		return fileList;
	}

	/**
	 * Assumes the key's value is a list of file paths, or a file containing such a list (see {@link #getList(String)})
	 * and returns them as a list of Files. Just calls getList(key) and converts to file using same method as
	 * {@link #getExistingFileProperty(String)} - each value is first treated as a path relative to the config
	 * directory; if the file doesn't exist, it is treated as an absolute path; if it still doesn't exist, we return
	 * null.
	 * 
	 * @param key
	 * @return a list of File objects, or null on error
	 */
	public List<File> getExistingFileList(String key) {
		List<String> list = getList(key);
		if (list == null) {
			return null;
		}
		List<File> fileList = new ArrayList<File>(list.size());
		for (String path : list) {
			File file = getRelativeFile(path);
			if (!(file == null) && file.exists()) {
				fileList.add(file);
			} else {
				file = getAbsoluteFile(get(key));
				if (!(file == null) && file.exists()) {
					fileList.add(file);
				} else {
					return null;
				}
			}
		}
		return fileList;
	}

	/**
	 * @return the filename from which this config was generated
	 */
	public String getFilename() {
		return filename;
	}

	private void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * This returns a File object as described by the specified key, which must be a path relative to the config
	 * directory.
	 * 
	 * For example, given "whatever.dir=../stuff/" in the config file, and given the current value of
	 * Config.getConfigDir() is the file e:/lib/config, specifying "whatever.dir" as the argument to this method would
	 * return the file e:/lib/stuff/.
	 */
	public File getFileProperty(String key) {
		String value = get(key);
		if (value != null && (value.startsWith(File.separator) || value.contains(":")))
			return getAbsoluteFile(value);
		return getRelativeFile(value);
	}

	/**
	 * This returns a File object as described by the specified key, which must be an absolute path.
	 * 
	 * For example, given "whatever.dir=e:/lib/stuff/" in the config file, specifying "whatever.dir" as the argument to
	 * this method would return the file e:/lib/stuff/.
	 */
	public File getAbsoluteFileProperty(String key) {
		return getAbsoluteFile(get(key));
	}

	/**
	 * This returns a File object as described by the specified key. The description is first treated as a path relative
	 * to the config directory; if the file doesn't exist, it is treated as an absolute path; if it still doesn't exist,
	 * we return null.
	 * 
	 * @param key
	 *            the config key
	 * @return a File object if such a file exists, null if not
	 */
	public File getExistingFileProperty(String key) {
		File file = getRelativeFile(get(key));
		if (!(file == null) && file.exists()) {
			return file;
		}
		file = getAbsoluteFile(get(key));
		if (!(file == null) && file.exists()) {
			return file;
		}
		return null;
	}

	/**
	 * Returns an integer corresponding to the key, -1 if none
	 * 
	 * @param key
	 * @return
	 */
	public int getInteger(String key) {
		try {
			return Integer.parseInt(get(key));
		} catch (NumberFormatException e1) {
			return -1;
		} catch (NullPointerException e2) {
			return -1;
		}
	}

	/**
	 * Returns a list of Integers corresponding to the key, which is specified either as a filename, or comma delimited,
	 * in square brackets. e.g. key=[1, 2, 3, 4] key=file:file.foo
	 * 
	 * @param key
	 * @return the List, or null if the key or specified file do not exist. Non-integer entries in the list (or file)
	 *         will get the value -1
	 */
	public List<Integer> getIntegerList(String key) {
		List<String> rawList = getList(key);
		if (rawList == null) {
			return null;
		}
		ArrayList<Integer> list = new ArrayList<Integer>(rawList.size());
		for (String s : rawList) {
			try {
				list.add(Integer.parseInt(s));
			} catch (NumberFormatException e1) {
				list.add(-1);
			}
		}
		return list;
	}

	/**
	 * Returns a double corresponding to the key, NaN if none
	 * 
	 * @param key
	 * @return
	 */
	public double getDouble(String key) {
		try {
			return Double.parseDouble(get(key));
		} catch (NumberFormatException e1) {
			return Double.NaN;
		} catch (NullPointerException e2) {
			return Double.NaN;
		}
	}

	/**
	 * Returns a list of Doubles corresponding to the key, which is specified either as a filename, or comma delimited,
	 * in square brackets. e.g. key=[1.1, 2.0, -3.3, 4] key=file:file.foo
	 * 
	 * @param key
	 * @return the List, or null if the key or specified file do not exist. Non-double entries in the list (or file)
	 *         will get the value Double.NaN
	 */
	public List<Double> getDoubleList(String key) {
		List<String> rawList = getList(key);
		if (rawList == null) {
			return null;
		}
		ArrayList<Double> list = new ArrayList<Double>(rawList.size());
		for (String s : rawList) {
			try {
				list.add(Double.parseDouble(s));
			} catch (NumberFormatException e1) {
				list.add(Double.NaN);
			}
		}
		return list;
	}

	public Collection getKeys() {
		return configProperties.keySet();
	}

	/**
	 * Gets a list, which is specified either as a filename, or comma delimited, in square brackets. e.g. key=[a, b, c,
	 * d] key=file:file.foo
	 * 
	 * In the file, list consists of lines which are not empty and don't begin with // or #
	 * 
	 * Non-list-like values will be converted to singleton lists.
	 * 
	 * @return the List, or null if the key or specified file do not exist
	 */
	// TODO: cache
	public List<String> getList(String key) {
		String val = get(key);
		if (val == null)
			return null;
		if (val.charAt(0) == '[' && val.charAt(val.length() - 1) == ']') {
			// zero length? return
			if (val.length() < 3) {
				return new ArrayList<String>(0);
			}
			// pull out the list from the line
			String[] sarr = val.substring(1, val.length() - 1).split("\\s*,\\s*");
			return Arrays.asList(sarr);
		}

		if (val.startsWith("file:")) {

			// read the list from the file
			String filename = val.substring(5);
			File f = new File(configDir + File.separator + filename);
			List<String> list = new ArrayList<String>();

			try {
				BufferedReader r;
				r = new BufferedReader(new FileReader(f));
				while (true) {
					String line = r.readLine();
					if (line == null)
						break;

					if (line.trim().equals("") || line.trim().startsWith("//") || line.trim().startsWith("#"))
						continue;
					list.add(line);
				}
				r.close();
				return list;

			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		// assume it's a singleton
		return Collections.singletonList(val);
	}

	/**
	 * Returns a file relative to the main config dir.
	 * 
	 * For example, given "../stuff/file" as the argument, and given the current value of Config.getConfigDir() is the
	 * file e:/lib/config, this method would return the file e:/lib/stuff/file.
	 * 
	 * @param relativePath
	 * @return
	 */
	public File getRelativeFile(String value) {
		if (value == null) {
			return null;
		}
		return new File(getConfigDir() + File.separator + value);

	}

	/**
	 * Returns a file assuming that it is specified as an absolute path.
	 * 
	 * For example, given "e:/stuff/file" as the argument, this will simply return a File object created directly from
	 * that string. If you want to specify relative paths, e.g. "../stuff/file", use getRelativeFile(String) instead
	 * 
	 * @param absolutePath
	 * @return
	 */
	public File getAbsoluteFile(String value) {
		if (value == null) {
			return null;
		}
		return new File(value);
	}

	/**
	 * Same as getList, only returning a set (each element is unique) See comments for getList.
	 */
	public Set<String> getSet(String key) {
		List<String> l = getList(key);
		if (l == null)
			return null;
		return new HashSet<String>(l);
	}

	/**
	 * Save the config properties
	 */
	public void save() {
		try {
			FileOutputStream configStream = new FileOutputStream(configDir + File.separator + filename);
			// System.out.print("SAVING config: " + filename);
			configProperties.store(configStream, null);
			// System.out.println("... DONE");
			configStream.close();
		} catch (Exception e) {
			System.out.println("ERROR! Could not save config file:" + filename);
			e.printStackTrace();
		}
	}

	/**
	 * Save the list in the file specified by the config setting. the value of the setting must be of from file:filename
	 * 
	 * @param configKey
	 * @param el
	 */
	public void saveList(String configKey, List list) {
		String val = this.get(configKey);
		if (val == null) {
			throw new RuntimeException("val is null");
		}
		if (!val.startsWith("file:")) {
			throw new RuntimeException("Key doesn't contain a filename:" + configKey + "=" + val);
		}

		String filename = val.substring(5);

		try {
			File f = new File(configDir + File.separator + filename);
			FileWriter fw = new FileWriter(f);
			for (int i = 0; i < list.size(); i++) {
				fw.write(list.get(i).toString() + "\n");
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Sets the config property ( and auto-saves the file). Don't use often.
	 */
	public void set(String key, String value) {
		set(key, value, true);
	}

	public void set(String key, String value, boolean save) {
		// System.out.println("CONFIG: SETTING "+key+" TO "+value);
		configProperties.put(key, value);
		if (save)
			save();
	}

	public void setBoolean(String key, boolean b) {
		setBoolean(key, b, true);
	}

	public void setBoolean(String key, boolean b, boolean save) {
		if (b)
			set(key, "true", save);
		else
			set(key, "false", save);
	}

	public void setInteger(String key, int i) {
		set(key, String.valueOf(i));
	}

	public Properties getConfigProperties() {
		return configProperties;
	}

}