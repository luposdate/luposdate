package lupos.io;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedList;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.codemap.CodeMapLiteral;
import lupos.datastructures.items.literal.string.StringLiteral;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.optimizations.logical.statistics.VarBucket;

public class LuposObjectOutputStream extends ObjectOutputStream {

	protected OutputStream os;

	public LuposObjectOutputStream() throws IOException {
	}

	public LuposObjectOutputStream(final OutputStream arg0) throws IOException {
		super(arg0);
		os = arg0;
	}

	protected Literal lastSubject = null;
	protected Literal lastPredicate = null, lastObject = null;

	public void writeLuposObject(final Object arg0) throws IOException {
		Registration.serializeWithoutId(arg0, this);
	}

	protected void writeLuposVarBucket(final VarBucket vb) throws IOException {
		writeLuposInt(vb.selectivityOfInterval.size());
		if (vb.minimum == null) {
			if (vb.maximum == null) {
				writeLuposByte((byte) 0);
			} else {
				writeLuposByte((byte) 1);
				LiteralFactory.writeLuposLiteral(vb.maximum, this);
			}
		} else {
			if (vb.maximum == null) {
				writeLuposByte((byte) 2);
				LiteralFactory.writeLuposLiteral(vb.minimum, this);
			} else {
				writeLuposByte((byte) 3);
				LiteralFactory.writeLuposLiteral(vb.minimum, this);
				LiteralFactory.writeLuposLiteral(vb.maximum, this);
			}
		}
		for (final lupos.optimizations.logical.statistics.Entry entry : vb.selectivityOfInterval) {
			this
					.writeLuposLong(Double
							.doubleToLongBits(entry.distinctLiterals));
			this.writeLuposLong(Double.doubleToLongBits(entry.selectivity));
			LiteralFactory.writeLuposLiteral(entry.literal, this);
		}
	}

	public void writeLuposTriplePattern(final TriplePattern tp)
			throws IOException {
		int id;
		final Integer alreadyid = LuposObjectInputStream.triplePatternHashMapID
				.get(tp);
		if (alreadyid != null) {
			id = alreadyid.byteValue();
		} else {
			id = LuposObjectInputStream.triplePatternID;
			LuposObjectInputStream.triplePatternID++;
			LuposObjectInputStream.triplePatternHashMapID.put(tp, id);
			LuposObjectInputStream.triplePatternHashMap.put(id, tp);
		}
		writeLuposByte((byte) id);
	}

	protected Bindings previousBindings = null;

	public void writeLuposTriple(final Triple t) throws IOException {
		int diff = 0;
		if (lastSubject == null
				|| t.getSubject() instanceof LazyLiteral
				&& !t.getSubject().equals(lastSubject)
				|| !(t.getSubject() instanceof LazyLiteral)
				&& t.getSubject().originalString().compareTo(
						lastSubject.originalString()) != 0) {
			diff = 1;
			lastSubject = t.getSubject();
		}
		if (lastPredicate == null
				|| t.getPredicate() instanceof LazyLiteral
				&& !t.getPredicate().equals(lastPredicate)
				|| !(t.getPredicate() instanceof LazyLiteral)
				&& t.getPredicate().originalString().compareTo(
						lastPredicate.originalString()) != 0) {
			diff += 2;
			lastPredicate = t.getPredicate();
		}
		if (lastObject == null
				|| t.getObject() instanceof LazyLiteral
				&& !t.getObject().equals(lastObject)
				|| !(t.getObject() instanceof LazyLiteral)
				&& t.getObject().originalString().compareTo(
						lastObject.originalString()) != 0) {
			diff += 4;
			lastObject = t.getObject();
		}
		os.write(diff);
		if (diff % 2 == 1)
			writeLuposObject(t.getSubject());
		if ((diff / 2) % 2 == 1)
			writeLuposObject(t.getPredicate());
		if ((diff / 4) % 2 == 1)
			writeLuposObject(t.getObject());
	}

	protected void writeStringKey(final String s) throws IOException {
		if (s.startsWith(lastSubject.toString())) {
			if (s.compareTo(lastSubject.toString() + lastPredicate.toString()
					+ lastObject.toString()) == 0) {
				os.write(1);
				return;
			} else if (s.compareTo(lastSubject.toString()
					+ lastObject.toString() + lastPredicate.toString()) == 0) {
				os.write(2);
				return;
			}
		} else if (s.startsWith(lastPredicate.toString())) {
			if (s.compareTo(lastPredicate.toString() + lastSubject.toString()
					+ lastObject.toString()) == 0) {
				os.write(3);
				return;
			} else if (s.compareTo(lastPredicate.toString()
					+ lastObject.toString() + lastSubject.toString()) == 0) {
				os.write(4);
				return;
			}
		} else if (s.startsWith(lastObject.toString())) {
			if (s.compareTo(lastObject.toString() + lastSubject.toString()
					+ lastPredicate.toString()) == 0) {
				os.write(5);
				return;
			} else if (s.compareTo(lastObject.toString()
					+ lastPredicate.toString() + lastSubject.toString()) == 0) {
				os.write(6);
				return;
			}
		}
		os.write(0);
		writeLuposString(s);
	}

	public void writeLuposString(final String s) throws IOException {
		if (s == null)
			return;
		final boolean flag = s.compareTo(new String(s.getBytes())) == 0;
		final int length = s.length();
		final int offset = flag ? 0 : 32;
		if (length < 256) {
			this.writeLuposInt1Byte(offset + 0);
			this.writeLuposInt1Byte(length);
		} else if (length < 256 * 256) {
			this.writeLuposInt1Byte(offset + 1);
			this.writeLuposInt2Bytes(length);
		} else if (length < 256 * 256 * 256) {
			this.writeLuposInt1Byte(offset + 2);
			this.writeLuposInt3Bytes(length);
		} else {
			this.writeLuposInt1Byte(offset + 3);
			this.writeLuposInt(length);
		}
		if (flag) {
			os.write(s.getBytes());
		} else {
			final char[] chararray = s.toCharArray();
			final ByteBuffer buf = ByteBuffer.allocate(chararray.length * 2);
			for (final char c : chararray) {
				buf.putChar(c);
			}
			os.write(buf.array());
		}
	}

	public void writeLuposBoolean(final boolean flag) throws IOException {
		if (flag)
			os.write(0);
		else
			os.write(1);
	}

	public void writeLuposInt(final int i) throws IOException {
		final int i1 = i % 256;
		final int i2 = (i / 256) % 256;
		final int i3 = (i / (256 * 256)) % 256;
		final int i4 = (i / (256 * 256 * 256)) % 256;
		os.write(i1);
		os.write(i2);
		os.write(i3);
		os.write(i4);
	}

	public void writeLuposInt1Byte(final int i) throws IOException {
		os.write(i % 256);
	}

	public void writeLuposInt2Bytes(final int i) throws IOException {
		final int i1 = i % 256;
		final int i2 = (i / 256) % 256;
		os.write(i1);
		os.write(i2);
	}

	public void writeLuposInt3Bytes(final int i) throws IOException {
		final int i1 = i % 256;
		final int i2 = (i / 256) % 256;
		final int i3 = (i / (256 * 256)) % 256;
		os.write(i1);
		os.write(i2);
		os.write(i3);
	}

	public void writeLuposLong(final long l) throws IOException {
		writeLuposInt((int) (l % ((long) 256 * 256 * 256 * 256)));
		writeLuposInt((int) (l / ((long) 256 * 256 * 256 * 256)));
	}

	public void writeLuposByte(final byte b) throws IOException {
		os.write(b);
	}

	public static void writeLuposInt(int i, final ObjectOutput out)
			throws IOException {
		if (i <= 251)
			out.write(i);
		else {
			i -= 251;
			if (i >= 256) {
				if (i / 256 >= 256) {
					if (i / (256 * 256) >= 256) {
						out.writeByte(255);
					} else
						out.writeByte(254);
				} else
					out.writeByte(253);
			} else
				out.writeByte(252);
			while (i > 0) {
				out.writeByte(i % 256);
				i /= 256;
			}
		}
	}

	public static void writeLuposLiteral(final Literal literal,
			final ObjectOutput out) throws IOException {
		if (literal instanceof StringLiteral)
			out.writeObject(((StringLiteral) literal).originalString());
		else
			LuposObjectOutputStream.writeLuposInt(((CodeMapLiteral) literal)
					.getCode(), out);
	}

	private static Collection<String> splitStringInto64KBUTFBlocks(
			final String value) throws IOException {
		String remaining = value;
		final LinkedList<String> result = new LinkedList<String>();
		final int len = value.length();
		long sum = 0;
		int i = 0;
		while (i < remaining.length()) {
			final char c = remaining.charAt(i);
			if (c >= '\u0001' && c <= '\u007f')
				sum += 1;
			else if (c == '\u0000' || (c >= '\u0080' && c <= '\u07ff'))
				sum += 2;
			else
				sum += 3;
			if (sum >= 65500) {
				result.add(remaining.substring(0, i));
				remaining = remaining.substring(i);
				sum = 0;
				i = 0;
			}
			++i;
		}

		if (remaining.length() > 0)
			result.add(remaining);
		return result;
	}
}
