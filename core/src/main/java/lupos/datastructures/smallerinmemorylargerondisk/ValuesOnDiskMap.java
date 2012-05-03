package lupos.datastructures.smallerinmemorylargerondisk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import lupos.misc.FileHelper;

public class ValuesOnDiskMap<K extends Comparable<K>, V extends Serializable>
		implements Map<K, V> {

	private final HashMap<K, String> pointersToFilesDisk = new HashMap<K, String>();

	protected static int currentFolderID = 0;

	protected final static String mainFolder = "tmp//valuesOnDiskMap//";
	protected final String folder = mainFolder + (currentFolderID++) + "//";
	
	protected int currentFileID = 0;

	static {
		FileHelper.deleteDirectory(new File(mainFolder));
	}

	public ValuesOnDiskMap() {
		FileHelper.deleteDirectory(new File(folder));
		final File f = new File(folder);
		f.mkdirs();
	}
	
	protected String storeOnDisk(final V value) {
		final String fileName = "" + currentFileID;
		currentFileID++;
		final File file = new File(fileName);
		if (file.exists())
			file.delete();
		try {
			final ObjectOutputStream os = new ObjectOutputStream(
					new FileOutputStream(file));
			os.writeObject(value);
			os.close();
		} catch (final FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fileName;
	}

	protected V retrieveFromDisk(final String filename) {
		try {
			final ObjectInputStream in = new ObjectInputStream(
					new FileInputStream(filename));
			final V v = (V) in.readObject();
			in.close();
			return v;
		} catch (final FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void clear() {
		// TODO Auto-generated method stub

	}

	public boolean containsKey(final Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean containsValue(final Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public Set<java.util.Map.Entry<K, V>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}

	public V get(final Object arg0) {
		final String filename = pointersToFilesDisk.get(arg0);
		if (filename == null)
			return null;
		return retrieveFromDisk(filename);
	}

	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	public Set<K> keySet() {
		// TODO Auto-generated method stub
		return null;
	}

	public V put(final K arg0, final V arg1) {
		pointersToFilesDisk.put(arg0, storeOnDisk(arg1));
		// We do not support to return the previous entry sue to performance
		// reasons!
		return null;
	}

	public void putAll(final Map<? extends K, ? extends V> arg0) {
		// TODO Auto-generated method stub

	}

	public V remove(final Object arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Collection<V> values() {
		// TODO Auto-generated method stub
		return null;
	}

}
