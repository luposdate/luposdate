package lupos.datastructures.lsmtree;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;

import lupos.datastructures.lsmtree.level.Container;
import lupos.datastructures.lsmtree.level.disk.store.StoreKeyValue;
import lupos.datastructures.lsmtree.level.factory.DiskLevelFactory;
import lupos.datastructures.lsmtree.level.factory.ILevelFactory;
import lupos.datastructures.lsmtree.sip.ISIPIterator;

/**
 * @author Maike
 *
 */
public class LSMTreeDemo {

	public static void main(final String args[]) throws ClassNotFoundException, IOException, URISyntaxException{
	//final ILevelFactory<Integer, String> testfactory = new MemoryLevelFactory<Integer, String>();
	//	final ILevelFactory<Integer, String> testfactory = new DiskLevelFactory<Integer, String>(new StoreIntegerString());
		final ILevelFactory<String, Integer, Iterator<Map.Entry<String,Container<Integer>>>> testfactory = new DiskLevelFactory<String, Integer>(new StoreKeyValue<String, Integer>(String.class, Integer.class), 2, 2);
//	final LsmTree<Integer, String> testtree=new LsmTree<Integer, String>(testfactory);
//	for(int i=0; i<500; i++){
//		testtree.put(i, "test"+i);
//	}
//	System.out.println("-------------- Test tree 1 -----------------");
//	testtree.printLevels();


	final LSMTree<String, Integer, Iterator<Map.Entry<String,Container<Integer>>>> testtree2=new LSMTree<String, Integer, Iterator<Map.Entry<String,Container<Integer>>>>(testfactory);

	for(int i=0; i<1*300000; i++){
		testtree2.put("hallihallohallöchen"+i, i);
		//testtree2.put(i, "tedhhdfgdjfgst"+i);
	}
//	for(int i=0; i<200; i++){
//	if(i%2==0){
//		testtree2.remove(i);
//	}
//	}
//	for(int i=200; i<350; i++){
//		testtree2.put(i, "tedhhdfgjfgst"+i);
//	}
//	for(int i=0; i<200; i++){
//		if(i%2==0){
//			testtree2.remove(i);
//		}
//	}
	System.out.println("-------------- Test tree 2 -----------------");
	testtree2.printLevels();
	//getestet für keys kleiner und größer als eingefügt
	System.out.println(testtree2.get("hallihallohallöchen1000"));
	System.out.println("-------------- Iterator Test -----------------");
	final ISIPIterator<String, Integer> it = testtree2.isipIterator();
	while(it.hasNext()){
		System.out.println(it.next());
	}
	}
}

