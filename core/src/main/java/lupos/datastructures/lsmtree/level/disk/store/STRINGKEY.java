package lupos.datastructures.lsmtree.level.disk.store;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;

import lupos.datastructures.simplifiedfractaltree.StringKey;
import lupos.io.Registration;
import lupos.io.Registration.DeSerializerConsideringSubClasses;
import lupos.io.helper.InputHelper;
import lupos.io.helper.LengthHelper;
import lupos.io.helper.OutHelper;

/**
 * This class is a DeSerializer for <tt>StringKey</tt> for the <tt>Luposdate</tt> de/serialization. In order to work it must be registered in
 * the <tt>Registration</tt> class. It falls back to default java serialization if the key and value type is unsupported.
 * @author Denis Fäcke
 * @see DeSerializerConsideringSubClasses
 * @see Registration
 */
public class STRINGKEY extends DeSerializerConsideringSubClasses<StringKey> {
	/**
	 * Deserializes a <tt>StringKey</tt> from the <tt>InputStream</tt>.
	 * @param in A <tt>InputStream</tt>
	 * @see InputStream
	 * @return A <tt>StringKey</tt>
	 */
	@Override
	public StringKey deserialize(final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
		return new StringKey(InputHelper.readLuposString(in));
	}

	/**
	 * Returns a Class object with the type <tt>StringKey</tt>.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends StringKey>[] getRegisteredClasses() {
		return new Class[] { StringKey.class };
	}

	@Override
	public int length(final StringKey arg0) {
		return LengthHelper.lengthLuposString(arg0.toString());
	}

	/**
	 * Serializes a <tt>StringKey</tt> onto a <tt>OutputStream</tt>.
	 * @see StringKey
	 * @see OutputStream
	 * @param stringKey A <tt>StringKey</tt>
	 * @param os A <tt>OutputStream</tt>
	 */
	@Override
	public void serialize(final StringKey stringKey, final OutputStream os) throws IOException {
		if (stringKey == null){
            return;
		}
		OutHelper.writeLuposString(stringKey.toString(), os);
	}

	/**
	 * Tests if the given objects is an instance of <tt>StringKey</tt>.
	 * @param o A <tt>Object</tt>
	 * @see StringKey
	 */
	@Override
	public boolean instanceofTest(final Object o) {
		return o instanceof StringKey;
	}
}