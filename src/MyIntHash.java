import java.util.LinkedList;

// TODO: Auto-generated Javadoc
/**
 * The Class MyIntHash.
 */
public class MyIntHash {
	
	/**
	 * The Enum MODE.
	 */
	enum MODE {Linear, Quadratic,  LinkedList,  Cuckoo};
	
	/** The Constant INITIAL_SIZE. */
	private final static int INITIAL_SIZE = 31;
	
	/** The mode of operation. */
	private MODE mode = MODE.Linear;
	
	/** The physical table size. */
	private int tableSize;
	
	/** The size of the hash - the number of elements in the hash. */
	private int size;
	
	/** The load factor. */
	private double load_factor; 
	
	/** The hash table 1. */
	private int[] hashTable1;
	
	// The following variables will be defined but not used until later in the project..
	/** The hash table 2. */
	private int[] hashTable2;
	
	/** The hash table LL. */
	private LinkedList<Integer>[] hashTableLL;
	
	/** The max offset. */
	private final int MAX_QP_OFFSET = 2<<15;
	
	/** The limit for loops in add_QP, remove_QP and contains_QP. */
	private int MAX_QP_LOOP;
	
	/** Counts amount of loops in add_Cuckoo method to prevent overflow */
	private int recursiveCount;
	
	/** Amount of loops allowed before growing the hash */
	private final static int MAX_RECURSIVE_COUNT = 20;
	
	/**
	 * Instantiates a new my int hash. For Part1 JUnit Testing, the load_factor will be set to 1.0
	 *
	 * @param mode the mode
	 * @param load_factor the load factor
	 */
	public MyIntHash(MODE mode, double load_factor) {
		// TODO Part1: initialize table size, size, mode, and load_factor
		//             Instantiate hashTable1 and initialize it
		tableSize = INITIAL_SIZE;
		size = 0;
		this.mode = mode;
		this.load_factor = load_factor;
		
		hashTable1 = new int[tableSize];
		initHashTable(hashTable1);
		
		if (MAX_QP_OFFSET >= tableSize / 2) {
			MAX_QP_LOOP = tableSize / 2;
		} else {
			MAX_QP_LOOP = MAX_QP_OFFSET;
		}
		
		hashTableLL = new LinkedList[tableSize];
		initHashTable(hashTableLL);
		
		hashTable2 = new int[tableSize];
		initHashTable(hashTable2);
	}
	
	/**
	 * Instantiates a new my int hash. This specific constructor is used for benchmarking assignment
	 * 
	 * @param mode the mode
	 * @param load_factor the load factor
	 * @param initialSize the initial size
	 */
	public MyIntHash(MODE mode, double load_factor, int initialSize) {
		// TODO Part1: initialize table size, size, mode, and load_factor
		//             Instantiate hashTable1 and initialize it
		tableSize = initialSize;
		size = 0;
		this.mode = mode;
		this.load_factor = load_factor;
		
		hashTable1 = new int[tableSize];
		initHashTable(hashTable1);
		
		if (MAX_QP_OFFSET >= tableSize / 2) {
			MAX_QP_LOOP = tableSize / 2;
		} else {
			MAX_QP_LOOP = MAX_QP_OFFSET;
		}
		
		hashTableLL = new LinkedList[tableSize];
		initHashTable(hashTableLL);
		
		hashTable2 = new int[tableSize];
		initHashTable(hashTable2);
	}

	/**
	 * Initializes the provided int[] hashTable - setting all entries to -1
	 * Note that this function will be overloaded to initialize hash tables in other modes
	 * of operation. This method should also reset size to 0!
	 *
	 * @param hashTable the hash table
	 */
	private void initHashTable(int[] hashTable) {
		// TODO Part1: Write this method 
		for (int i = 0; i < hashTable.length; i++) {
			hashTable[i] = -1;
		}
		size = 0;
	}
	
	/**
	 * Initializes the provided LinkedList<Integer>[] hashTable - setting all entries to null
	 * Overloaded method for LinkedList implementation
	 * 
	 * @param hashTable the hash table
	 */
	private void initHashTable(LinkedList<Integer>[] hashTable) {
		for (int i = 0; i < hashTable.length; i++) {
			hashTable[i] = null;
		}
		size = 0;
	}
	
	/**
	 * Initializes the provided int[] hashTable1 and int[] hashTable2 for Cuckoo mode - setting all entries to -1
	 * This method does not reset size to 0.
	 * 
	 * @param hashTable1 the primary hash table
	 * @param hashTable2 the secondary hash table
	 */
	private void initHashTable(int[] hashTable1, int[] hashTable2) {
		for (int i = 0; i < hashTable1.length; i++) {
			hashTable1[i] = -1;
			hashTable2[i] = -1;
		}
	}
	
	/**
	 * Hash fx.  This is the hash function that translates the key into the index into the hash table.
	 *
	 * @param key the key
	 * @return the int
	 */
	private int hashFx(int key) {
		// TODO Part1: Write this method.
		return key % tableSize;
	}
	
	/**
	 * Hash fx2. This is the hash function that translates the key into the index into the secondary hash table.
	 * 
	 * @param key the key
	 * @return the int
	 */
	private int hashFx2(int key) {
		return (key / tableSize) % tableSize;
	}
	
	/**
	 * Adds the key to the hash table. Note that this is a helper function that will call the 
	 * required add function based upon the operating mode. However, before calling the specific
	 * add function, determine if the hash should be resized; if so, grow the hash.
	 *
	 * @param key the key
	 * @return true, if successful
	 */
	public boolean add(int key) {
		// TODO: Part2 - if adding this key would cause the hash load to exceed the load_factor, grow the hash.
		//      Note that you cannot just use size in the numerator... 
		//      Write the code to implement this check and call growHash() if required (no parameters)
		double currentLoad = (size + 1.0) / tableSize;
		
		if (mode == MODE.Cuckoo) { // Cuckoo mode requires different load factor due to the use of two hash tables
			currentLoad = (size + 1.0) / (tableSize * 2.0);

		}
		
		if (currentLoad >= load_factor) {
			growHash();
		}
		
		switch (mode) {
			case Linear : return add_LP(key); 
			case Quadratic : return add_QP(key);
			case LinkedList : return add_LL(key);
			case Cuckoo : return add_Cuckoo(key);
			default : return add_LP(key);
		}
	}
	
	/**
	 * Contains. Note that this is a helper function that will call the 
	 * required contains function based upon the operating mode
	 *
	 * @param key the key
	 * @return true, if successful
	 */
	public boolean contains(int key) {
		switch (mode) {
			case Linear : return contains_LP(key); 
			case Quadratic : return contains_QP(key);
			case LinkedList : return contains_LL(key);
			case Cuckoo : return contains_Cuckoo(key);
			default : return contains_LP(key);
		}
	}
	
	/**
	 * Remove. Note that this is a helper function that will call the 
	 * required remove function based upon the operating mode
	 *
	 * @param key the key
	 * @return true, if successful
	 */
	public boolean remove(int key) {
		switch (mode) {
			case Linear : return remove_LP(key); 
			case Quadratic : return remove_QP(key);
			case LinkedList : return remove_LL(key);
			case Cuckoo : return remove_Cuckoo(key);
			default : return remove_LP(key);
		}
	}
	
	/**
	 * Grow hash. Note that this is a helper function that will call the 
	 * required overloaded growHash function based upon the operating mode.
	 * It will get the new size of the table, and then grow the Hash. Linear case
	 * is provided as an example....
	 */
	private void growHash() {
		int newSize = getNewTableSize(tableSize);
		switch (mode) {
			case Linear: growHash(hashTable1,newSize); break;
			case Quadratic : growHash_QP(hashTable1,newSize); break;
			case LinkedList : growHash(hashTableLL, newSize); break;
			case Cuckoo : growHash(hashTable1, hashTable2, getNewTableSize(tableSize + 1000)); break;
		}
	}
	
	/**
	 * Grow hash. This is the specific function that will grow the hash table in Linear or 
	 * Quadratic modes. This method will:
	 * 	1. save the current hash table, 
	 *  2. create a new version of hashTable1
	 *  3. update tableSize and size
	 *  4. add all valid entries from the old hash table into the new hash table
	 * 
	 * @param table the table
	 * @param newSize the new size
	 */
	private void growHash(int[] table, int newSize) {
		// TODO Part2:  Write this method
		int[] currentTable = table.clone(); // save the current hash table
		hashTable1 = new int[newSize];
		initHashTable(hashTable1);
		tableSize = newSize;
		
		for (int i = 0; i < currentTable.length; i++) {
			if (currentTable[i] != -1) { // only add valid entries (empty entries are -1)
				add(currentTable[i]);
			}
		}
	}
	
	/**
	 * Grow hash. This is the specific function that will grow the hash table in Quadratic modes.
	 * This method will:
	 * 
	 * 	1. save the current hash table, 
	 *  2. create a new version of hashTable1
	 *  3. update tableSize and size
	 *  4. add all valid entries from the old hash table into the new hash table
	 *  5. Calculate new MAX_QP_OFFSET value based off tableSize
	 * 
	 * @param table the table
	 * @param newSize the new size
	 */
	private void growHash_QP(int[] table, int newSize) {
		int[] currentTable = table.clone(); // save the current hash table
		hashTable1 = new int[newSize];
		initHashTable(hashTable1);
		tableSize = newSize;
		
		if (MAX_QP_OFFSET >= tableSize / 2) { // recalculate the value for MAX_QP_OFFSET based off potentially new tableSize
			MAX_QP_LOOP = tableSize / 2;
		} else {
			MAX_QP_LOOP = MAX_QP_OFFSET;
		}
		
		for (int i = 0; i < currentTable.length; i++) {
			if (currentTable[i] != -1) { // only add valid entries (empty entries are -1)
				add(currentTable[i]);
			}
		}
	}
	
	/**
	 *  Grow hash. This is the specific function that will grow the hash table in LinkedList mode.
	 *  Overloaded method for LinkedList structure
	 *  
	 *  1. save the current hash table, 
	 *  2. create a new version of hashTableLL
	 *  3. update tableSize and size
	 *  4. add all valid entries from the old hash table into the new hash table
	 *  
	 * @param table the table
	 * @param newSize the new size
	 */
	private void growHash(LinkedList<Integer>[] table, int newSize) {
		LinkedList<Integer>[] currentTable = table.clone(); // save the current hash table
		hashTableLL = new LinkedList[newSize];
		initHashTable(hashTableLL);
		tableSize = newSize;
		
		for (int i = 0; i < currentTable.length; i++) {
			LinkedList<Integer> counterList = currentTable[i];
			
			if (counterList != null) { // only add valid entries (empty entries are null)
				for (int j = 0; j < counterList.size(); j++) {
					add_LL(counterList.get(j));
				}
			}
		}
	}
	
	
	/**
	 *  Grow hash. This is the specific function that will grow the hash table in Cuckoo mode.
	 *  
	 *  1. save the current hash tables, 
	 *  2. create a new version of hash tables
	 *  3. update tableSize and size
	 *  4. add all valid entries from the old hash tables into the new hash tables
	 *  5. change the growHash variable when growing hash (true if growing, false if finished)
	 * 
	 * @param pTable the primary table
	 * @param sTable the secondary table
	 * @param newSize the new size
	 */
	private void growHash(int[] pTable, int[] sTable, int newSize) {
		int[] currentTable1 = pTable.clone(); // save the current hash table
		int[] currentTable2 = sTable.clone(); // save the current hash table
		hashTable1 = new int[newSize];
		hashTable2 = new int[newSize];
		initHashTable(hashTable1, hashTable2);
		tableSize = newSize;
		
		for (int i = 0; i < currentTable1.length; i++) {
			if (currentTable1[i] != -1) {
				add(currentTable1[i]);
			}
			if (currentTable2[i] != -1) {
				add(currentTable2[i]);
			}
		}
	}
	
	/**
	 * Gets the new table size. Finds the next prime number
	 * that is greater than 2x the passed in size (startSize)
	 *
	 * @param startSize the start size
	 * @return the new table size
	 */
	private int getNewTableSize(int startSize) {
		// TODO Part2: Write this method
		int newSize = startSize * 2; // greater than 2x the passed in size
		
		if (mode == MODE.Cuckoo) { // Cuckoo mode requires different startSize due to the use of two hash tables
			newSize = startSize;
		}
		
		while (!isPrime(newSize)) { // iterate through every number until prime number is found
			newSize++;
		}
		
		return newSize;
	}
	
	/**
	 * Checks if is prime.  
	 *
	 * @param size the size
	 * @return true, if is prime
	 */
	private boolean isPrime(int size) {
		// TODO Part2: Write this method
		if (size <= 1) { // automatically not a prime number
			return false;
		}
		for (int i = 2; i < Math.sqrt(size); i++) {
			if (size % i == 0) { // if not prime return false
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Adds the key using the Linear probing strategy:
	 * 
	 * 1) Find the first empty slot sequentially, starting at the index from hashFx(key)
	 * 2) Update the hash table with the key
	 * 3) increment the size
	 * 
	 * If no empty slots are found, return false - this would indicate that the hash needs to grow...
	 *
	 * @param key the key
	 * @return true, if successful
	 */
	private boolean add_LP(int key) {
		// TODO Part1: Write this function
		int index = hashFx(key);
		
		for (int i = 0; i < tableSize; i++) {
			if (index == tableSize) {
				index = 0;
			}
			if (hashTable1[index] == key) {
				return false;
			}
			if (hashTable1[index] < 0) { // < 0 because of remove method utilization of -2
				hashTable1[index] = key;
				size++;
				return true;
			}
			index++;
		}
		
		return false;
	}
	
	/**
	 * Adds the key using the Quadratic probing strategy:
	 * 
	 * If no empty slots are found, return false - this would indicate that the hash needs to grow...
	 * 
	 * @param key the key
	 * @return true, if successful
	 */
	private boolean add_QP(int key) {
		int st_index = hashFx(key);
		int index;

		for (int i = 0; i < MAX_QP_LOOP; i++) {
			index = (st_index + i * i) % tableSize;

			if (hashTable1[index] == key) {
				return false;
			}
			if (hashTable1[index] < 0) { // < 0 because of remove method utilization of -2
				hashTable1[index] = key;
				size++;
				return true;
			}
		}
		
		growHash(); // grow the hash if no space is available
		return add_QP(key); // repeat process until either added or no more space is possible
	}
	
	/**
	 *  Adds the key using the LinkedList structure:
	 *  
	 *  Only adds if key doesn't already exist, otherwise always successful
	 *  
	 * @param key the key
	 * @return true, if successful
	 */
	private boolean add_LL(int key) {
		int index = hashFx(key);
		
		if (hashTableLL[index] == null) {
			hashTableLL[index] = new LinkedList<Integer>();
			hashTableLL[index].add(key);
			size++;
			return true;
		}
		if (contains_LL(key)) {
			return false;
		}
		
		hashTableLL[index].add(key);
		size++;
		return true;
	}
	
	/**
	 * Adds the key using the Cuckoo structure:
	 * 
	 * Only adds if key doesn't already exist, otherwise always successful
	 * 
	 * @param key the key
	 * @return true, if successful
	 */
	private boolean add_Cuckoo(int key) {
		if (contains_Cuckoo(key)) {
			return false;
		}
		
		recursiveCount = 0; // initialize loop count to 0 (start of new add cycle)
		boolean placed = placeCuckoo(key, 1);
		
		if (placed == false) { // no space, retry adding to hash tables
			growHash();
			add_Cuckoo(key);
		}
		
		size++;
		return true; // successfully added to table
	}
	
	/**
	 * Recursive helper method for the add_Cuckoo method
	 * 
	 * @param key the key
	 * @param table, the table
	 * @return true, if successful
	 */
	private boolean placeCuckoo(int key, int table) {
		int[] currentTable = hashTable1;
		int index = hashFx(key);
		
		if (table == 2) {
			currentTable = hashTable2;
			index = hashFx2(key);
		}
		
		if (currentTable[index] != -1) {
			int evicted = currentTable[index];
			currentTable[index] = key;
			
			if ((key == evicted && table == 2) || (recursiveCount >= MAX_RECURSIVE_COUNT)) {
				return false;
			} 
			
			if (table == 1) {
				recursiveCount++;
				return placeCuckoo(evicted, 2);
			} else {
				recursiveCount++;
				return placeCuckoo(evicted, 1);
			}
		}
		
		currentTable[index] = key;
		return true;
	}
	
	/**
	 * Contains - uses the Linear Probing method to determine if the key exists in the hash
	 * A key condition is that there are no open spaces between any values with collisions, 
	 * independent of where they are stored.
	 * 
	 * Starting at the index from hashFx(key), sequentially search through the hash until:
	 * a) the key matches the value at the index --> return true
	 * b) there is no valid data at the current index --> return false
	 * 
	 * If no matches found after walking through the entire table, return false
	 *
	 * @param key the key
	 * @return true, if successful
	 */
	private boolean contains_LP(int key) {
		// TODO Part1: Write this method.
		int index = hashFx(key);
		
		for (int i = 0; i < tableSize; i++) {
			if (index == tableSize) {
				index = 0;
			}
			if (hashTable1[index] == key) {
				return true;
			}
			index++;
		}
		
		return false;
	}
	
	/**
	 * Contains - uses the Quadratic Probing method to determine if the key exists in the hash
	 * 
	 * If no matches found after walking through table, return false
	 * 
	 * @param key the key
	 * @return true, if successful
	 */
	private boolean contains_QP(int key) {
		int st_index = hashFx(key);
		int index;
		
		for (int i = 0; i < MAX_QP_LOOP; i++) {
			index = (st_index + i * i) % tableSize;
			
			if (hashTable1[index] == -1) {
				return false;
			}
			if (hashTable1[index] == key) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Contains - uses the LinkedList structure to determine if the key exists in the hash
	 * 
	 * If no matches found return false
	 * 
	 * @param key the key
	 * @return true, if successful
	 */
	private boolean contains_LL(int key) {
		int index = hashFx(key);
		
		if (hashTableLL[index] == null) {
			return false;
		}
		
		return hashTableLL[index].contains(key);
	}
	
	
	/**
	 * Contains - uses the Cuckoo structure to determine if the key exists in the hash
	 * 
	 * If no matches found return false
	 * 
	 * @param key the key
	 * @return true, if successful
	 */
	private boolean contains_Cuckoo(int key) {
		return (hashTable1[hashFx(key)] == key) || (hashTable2[hashFx2(key)] == key);
	}
	
	
	/**
	 * Remove - uses the Linear Problem method to evict a key from the hash, if it exists
	 * A key requirement of this function is that the evicted key cannot introduce an open space
	 * if there are subsequent values which had collisions...
	 *
	 * @param key the key
	 * @return true, if successful
	 */
	private boolean remove_LP(int key) {
		// TODO Part2: Write this function
		int target = hashFx(key);
		
		for (int i = 0; i < tableSize; i++) {
			if (target == tableSize) {
				target = 0;
			}
			if (hashTable1[target] == key) { // found target number
				if (((target + 1 < tableSize) && (hashTable1[target + 1] != -1)) ||
						((target + 1 >= tableSize) && (hashTable1[0] != -1))) {
					hashTable1[target] = -2; // indicate that the entry has been removed
					size--;
					return true;
				} else {
					hashTable1[target] = -1; // indicate empty entry
					size--;
					return true;
				}
			}
			target++;
		}
		
		return false;
	}
	
	/**
	 * Remove - uses Quadratic Problem method to evict a key from the hash, if it exists
	 * 
	 * @param key the key
	 * @return true, if successful
	 */
	private boolean remove_QP(int key) {
		int st_index = hashFx(key);
		int index;
		
		for (int i = 0; i < MAX_QP_LOOP; i++) {
			index = (st_index + i * i) % tableSize;

			if (hashTable1[index] == key) { // found target number
				if (((index < tableSize) && (hashTable1[index] != -1)) ||
						((index >= tableSize) && (hashTable1[0] != -1))) {
					hashTable1[index] = -2; // indicate that the entry has been removed
					size--;
					return true;
				} else {
					hashTable1[index] = -1; // indicate empty entry
					size--;
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Remove - uses LinkedList structure to evict a key from the hash, if it exists
	 * 
	 * @param key the key
	 * @return true, if successful
	 */
	private boolean remove_LL(int key) {
		if (!contains_LL(key)) {
			return false;
		}
		
		int index = hashFx(key);
		
		hashTableLL[index].remove((Integer) key);
		size--;
		
		if (hashTableLL[index].isEmpty()) {
			hashTableLL[index] = null;
		}
		
		return true;
	}
	
	
	/** 
	 * Remove - uses Cuckoo structure to evict a key from the hash, if it exists
	 * 
	 * @param key the key
	 * @return true, if successful
	 */
	private boolean remove_Cuckoo(int key) {
		if (!contains_Cuckoo(key)) {
			return false;
		}
		
		if (hashTable1[hashFx(key)] == key) {
			hashTable1[hashFx(key)] = -1;
		} else {
			hashTable2[hashFx2(key)] = -1;
		}
		
		size--;
		return true;
	}
		
	/**
	 * Gets the hash at. Returns the value of the hash at the specified index, and (if required by the operating mode) the specified offset.
	 * Use a switch statement to implement this code. This is FOR DEBUG AND TESTING PURPOSES ONLY
	 * 
	 * @param index the index
	 * @param offset the offset
	 * @return the hash at
	 */
	Integer getHashAt(int index, int offset) {
		// TODO Part1: as you code this project, you will add different cases. for now, complete the case for Linear Probing
		switch (mode) {
			case Linear : return hashTable1[index]; // What needs to go here??? write this and uncomment
			case Quadratic : return hashTable1[index];
			case LinkedList : 
				if (hashTableLL[index] == null) {
					return null;
				}
				if (offset < 0 || offset >= hashTableLL[index].size()) {
					return -1;
				} else {
					return hashTableLL[index].get(offset);
				}
			case Cuckoo :
				if (offset == 0) {
					return hashTable1[index];
				} else {
					return hashTable2[index];
				}
		}
		return -1;
	}
	
	/**
	 * Gets the number of elements in the Hash
	 *
	 * @return size
	 */
	public int size() {
		// TODO Part1: Write this method
		return size;
	}

	/**
	 * resets all entries of the hash to -1. This should reuse existing code!!
	 *
	 */
	public void clear() {
		// TODO Part1: Write this method
		switch (mode) {
			case Linear : initHashTable(hashTable1);
			case Quadratic : initHashTable(hashTable1);
			case LinkedList : initHashTable(hashTableLL);
			case Cuckoo : initHashTable(hashTable1, hashTable2); size = 0;
			default : initHashTable(hashTable1);
		}
	}

	/**
	 * Returns a boolean to indicate of the hash is empty
	 *
	 * @return ????
	 */
	public boolean isEmpty() {
		// TODO Part1: Write this method
		return size == 0;
	}

	/**
	 * Gets the load factor.
	 *
	 * @return the load factor
	 */
	public double getLoad_factor() {
		return load_factor;
	}

	/**
	 * Sets the load factor.
	 *
	 * @param load_factor the new load factor
	 */
	public void setLoad_factor(double load_factor) {
		this.load_factor = load_factor;
	}

	/**
	 * Gets the table size.
	 *
	 * @return the table size
	 */
	public int getTableSize() {
		return tableSize;
	}
	
	/**
	 * Gets the current hash load.
	 * 
	 * @return the current hash load
	 */
	public double getCurrHashLoad() {
		switch (mode) {
			case Linear : return (size + 1.0) / tableSize;
			case Quadratic : return (size + 1.0) / tableSize;
			case LinkedList : return (size + 1.0) / tableSize;
			case Cuckoo : return (size + 1.0) / (tableSize * 2.0);
			default : return (size + 1.0) / tableSize;
		}
	}
}