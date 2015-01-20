/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package lupos.datastructures.simplifiedfractaltree.buffermanager;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import lupos.datastructures.simplifiedfractaltree.FractalTreeEntry;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class MemoryManager_NextFit<K extends Comparable<K> & Serializable, V extends Serializable> implements Serializable {
	/**
	 * Serial Version ID
	 */
	private static final long serialVersionUID = 6602141451211162408L;
	private HashMap<Integer, ArrayList<Point>> map;
	private final int pageSize = 8 * 1024;
	private final int pPage = -1;
	private final int pBound = -1;
	private final int max = 20;

	public MemoryManager_NextFit() {
		this.map = new HashMap<>();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MemoryManager_NextFit(final File file) {
		ObjectInputStream ois = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			ois = new ObjectInputStream(fis);
			final Object obj = ois.readObject();
			if (obj instanceof MemoryManager_NextFit) {
				this.map = ((MemoryManager_NextFit) obj).getMap();
			}
		} catch (final FileNotFoundException e) {
			this.map = new HashMap<>();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public Pointer acquire(final int pageCount, final int size) {
		ArrayList<Point> tmp;
		final int lmax = this.max;
		for (int i = this.pPage; ; ) {
			if(lmax == 0){
				break;
			}
			tmp = this.map.get(new Integer(i));
			if (tmp != null) {
				int j;
				if(i == this.pPage){
					j = this.pBound;
				} else {
					j = 0;
				}
				for (; j < tmp.size(); j++) {
					final Point gab = tmp.get(j);
					if (gab.x + gab.y <= this.pageSize && gab.y >= size) {
						final Pointer p = new Pointer(i, gab.x, size);
						tmp.get(j).x += size;
						tmp.get(j).y -= size;
						return p; // x = page, y = left bound
					} else if (j == tmp.size() - 1) {
						final ArrayList<Point> list = this.getRemaining(i, size - (this.pageSize - gab.x), new ArrayList<Point>());
						if (list.size() > 0) {
							final Pointer p = new Pointer(i, gab.x, size);
							this.map.get(new Integer(i)).remove(j);
							for (int l = 0; l < list.size(); l++) {
								if (this.map.get(new Integer(list.get(l).x)).get(0).y == list.get(l).y) {
									this.map.get(new Integer(list.get(l).x)).remove(0);
								} else {
									this.map.get(new Integer(list.get(l).x)).get(0).x += list.get(l).y;
									this.map.get(new Integer(list.get(l).x)).get(0).y -= list.get(l).y;
								}
							}
							return p;
						}
					}
				}
			}

			// ForEnd
			i++;
			if(i == pageCount && this.pPage > 0){
				i = 0;
			}
		}
		return null;
	}

	private ArrayList<Point> getRemaining(final int page, final int remainingSize, final ArrayList<Point> list) {
		final Point gab = this.map.get(new Integer(page)).get(0);

		if (gab.y == this.pageSize && gab.y < remainingSize) {
			list.add(new Point(page, remainingSize));
			return this.getRemaining(page + 1, remainingSize - this.pageSize, list);
		} else if (gab.y >= remainingSize) {
			list.add(new Point(page, this.pageSize));
			return list;
		} else {
			return new ArrayList<Point>();
		}
	}

	public void release(final int page, final int leftBound, final int size) {
		if (leftBound + size <= this.pageSize) {
			if (this.map.get(new Integer(page)) != null) {
				for (int i = 0; i < this.map.get(new Integer(page)).size(); i++) {
					if (leftBound < this.map.get(new Integer(page)).get(i).x) {
						if (i - 1 >= 0 && leftBound == this.map.get(new Integer(page)).get(i - 1).x + this.map.get(new Integer(page)).get(i - 1).y
								&& leftBound + size + 1 == this.map.get(new Integer(page)).get(i).x) {
							final Point p = new Point(this.map.get(new Integer(page)).get(i - 1).x, this.map.get(new Integer(page)).get(i - 1).y + this.map.get(new Integer(page)).get(i).y + size);
							this.map.get(new Integer(page)).remove(i - 1);
							this.map.get(new Integer(page)).remove(i - 1);
							this.map.get(new Integer(page)).add(i - 1, p);
							return;
						} else if (i - 1 >= 0 && leftBound == this.map.get(new Integer(page)).get(i - 1).x + this.map.get(new Integer(page)).get(i - 1).y) {
							this.map.get(new Integer(page)).get(i - 1).y += size;
							return;
						} else if (leftBound + size + 1 == this.map.get(new Integer(page)).get(i).x) {
							this.map.get(new Integer(page)).get(i).y += size;
							this.map.get(new Integer(page)).get(i).x -= size;
							return;
						}
						this.map.get(new Integer(page)).add(i, new Point(leftBound, size));
						return;
					} else if (i == this.map.get(new Integer(page)).size() - 1) {
						if (leftBound == this.map.get(new Integer(page)).get(i).x + this.map.get(new Integer(page)).get(i).y) {
							this.map.get(new Integer(page)).get(i).y += size;
							return;
						} else {
							this.map.get(new Integer(page)).add(new Point(leftBound, size));
							return;
						}
					}
				}

			} else {
				final ArrayList<Point> tmp = new ArrayList<>();
				tmp.add(new Point(leftBound, size));
				this.map.put(new Integer(page), tmp);
				return;
			}
		} else {
			if (this.map.get(new Integer(page)) != null) {
				if (this.map.get(new Integer(page)).get(this.map.get(new Integer(page)).size() - 1).x + this.map.get(new Integer(page)).get(this.map.get(new Integer(page)).size() - 1).y == leftBound) {
					this.map.get(new Integer(page)).get(this.map.get(new Integer(page)).size() - 1).y += this.pageSize - leftBound;
					this.release(page + 1, 0, size - (this.pageSize - leftBound));
					return;
				} else {
					this.map.get(new Integer(page)).add(new Point(leftBound, this.pageSize - leftBound));
					this.release(page + 1, 0, size - (this.pageSize - leftBound));
					return;
				}
			} else {
				final ArrayList<Point> tmp = new ArrayList<>();
				tmp.add(new Point(leftBound, size));
				this.map.put(new Integer(page), tmp);
				return;
			}
		}
	}


	public ArrayList<Triple> getAll() {
		throw new NotImplementedException();
	}

	public void writeToDisk(final File file) {
		ObjectOutputStream oos = null;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(this);
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public HashMap<Integer, ArrayList<Point>> getMap() {
		return this.map;
	}

	public void clear() {
		this.map = new HashMap<>();
	}

	public void defragment(final BufferedList_LuposSerialization<FractalTreeEntry<K, V>> bufferedList){
		final FractalTreeEntry<K, V> entry = new FractalTreeEntry<>();
		final BufferedList_LuposSerialization<FractalTreeEntry<K, V>> tempBufferedList = new BufferedList_LuposSerialization<FractalTreeEntry<K, V>>(8 * 1024, (new File("tempbf")).getAbsoluteFile(), entry);
		for(int i = 0;i < bufferedList.size();i++){
			tempBufferedList.add(bufferedList.get(i));
		}
		bufferedList.clear();
		for(int i = 0;i < tempBufferedList.size();i++){
			bufferedList.add(tempBufferedList.get(i));
		}
	}
}