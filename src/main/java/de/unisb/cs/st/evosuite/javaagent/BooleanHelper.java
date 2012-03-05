/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import java.util.Collection;
import java.util.Map;
import java.util.Stack;

import org.objectweb.asm.Opcodes;

import de.unisb.cs.st.evosuite.Properties;

/**
 * @author Gordon Fraser
 * 
 */
public class BooleanHelper {

	private static Stack<Integer> distanceStack = new Stack<Integer>();

	private static Stack<Stack<Integer>> stackStack = new Stack<Stack<Integer>>();

	private static final int MAX_STACK = Properties.TT_stack;

	public static final int K = Integer.MAX_VALUE - 2;

	public static void clearPredicates() {
		distanceStack.clear();
	}

	public static void methodEntered() {
		if (distanceStack != null)
			stackStack.push(distanceStack);
		distanceStack = new Stack<Integer>();
	}

	public static void methodLeft() {
		if (!stackStack.isEmpty())
			distanceStack = stackStack.pop();
		else
			distanceStack = null;
	}

	public static void clearStack() {
		if (!stackStack.isEmpty())
			stackStack.clear();
		if (distanceStack != null)
			distanceStack.clear();
	}

	/**
	 * Helper function that is called instead of Object.equals
	 * 
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	public static int objectEquals(Object obj1, Object obj2) {
		return obj1.equals(obj2) ? K : -K;
	}

	/**
	 * Helper function that is called instead of Collection.isEmpty
	 * 
	 * @param c
	 * @return
	 */
	public static int collectionIsEmpty(Collection<?> c) {
		return c.isEmpty() ? K : -c.size();
	}

	/**
	 * Helper function that is called instead of Collection.contains
	 * 
	 * @param c
	 * @param o1
	 * @return
	 */
	public static int collectionContains(Collection<?> c, Object o1) {
		int matching = 0;
		for (Object o2 : c) {
			if (o2.equals(o1))
				matching++;
		}
		return matching > 0 ? matching : -c.size();
	}

	/**
	 * Helper function that is called instead of Collection.containsAll
	 * 
	 * @param c
	 * @param o1
	 * @return
	 */
	public static int collectionContainsAll(Collection<?> c, Collection<?> c2) {
		int mismatch = 0;
		for (Object o : c2) {
			if (c.contains(o))
				mismatch++;
		}
		return mismatch > 0 ? -mismatch : c2.size();
	}

	/**
	 * Helper function that is called instead of Map.containsKey
	 * 
	 * @param c
	 * @param o1
	 * @return
	 */
	public static int mapContainsKey(Map<?, ?> m, Object o1) {
		return collectionContains(m.keySet(), o1);
	}

	/**
	 * Helper function that is called instead of Map.containsValue
	 * 
	 * @param c
	 * @param o1
	 * @return
	 */
	public static int mapContainsValue(Map<?, ?> m, Object o1) {
		return collectionContains(m.values(), o1);
	}

	/**
	 * Helper function that is called instead of Map.isEmpty
	 * 
	 * @param c
	 * @return
	 */
	public static int mapIsEmpty(Map<?, ?> m) {
		return m.isEmpty() ? K : -m.size();
	}

	public static void pushPredicate(int branchId, int distance) {

	}

	public static void pushPredicate(int distance) {
		//logger.debug("Push: " + distance);
		if (distanceStack != null) {
			while (distanceStack.size() > MAX_STACK)
				distanceStack.remove(0);
			distanceStack.push(Math.abs(distance));
		}
	}

	private static double normalize(int distance) {
		//		double k = K;
		double k = Properties.MAX_INT;
		double d = distance;
		return d / (d + 0.5 * k);
		//return distance / (distance + 1.0);
	}

	public static int getDistance(int original) {
		if (distanceStack == null) {
			if (original > 0)
				return K;
			else
				return -K;
		}
		int l = distanceStack.size();
		int distance = K;
		if (distanceStack.size() > 0)
			distance = distanceStack.peek();
		distanceStack.clear();
		/*
				if (l <= 1) {
					//distance += K;
					if (original <= 0)
						distance = -distance;
					logger.debug("Distance (2)" + distance);
					return distance;
				}
		*/
		double val = (1.0 + normalize(distance)) / Math.pow(2.0, l);

		int d = (int) Math.ceil(K * val);
		//if (d == 0 && val != 0.0)
		//	d = 1; // TODO: This is a problem if the number of pushes is too big
		//if (d == 0)
		//	d = 1;
		if (original <= 0)
			d = -d;

		return d;
	}

	public static int fromDouble(double d) {
		//logger.info("Converting double " + d);
		/*
		if (d > Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		else if (d < Integer.MIN_VALUE)
			return Integer.MIN_VALUE;
		else 
		*/
		if (d == 0.0)
			return 0;
		else {
			double d2 = Math.signum(d) * Math.abs(d) / (1.0 + Math.abs(d));
			//logger.info(" -> " + d2);
			int d3 = (int) Math.round(Integer.MAX_VALUE * d2);
			//logger.info(" -> " + d3);
			return d3;
		}
	}

	public static int fromFloat(float d) {
		//logger.info("Converting float " + d);
		/*
		if (d > Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		else if (d < Integer.MIN_VALUE)
			return Integer.MIN_VALUE;
		else */
		if (d == 0.0f)
			return 0;
		else {
			float d2 = Math.signum(d) * Math.abs(d) / (1f + Math.abs(d));
			//logger.info(" ->" + d2);
			int d3 = Math.round(Integer.MAX_VALUE * d2);
			//logger.info(" -> " + d3);
			return d3;
		}
	}

	public static int fromLong(long d) {
		
		if (d > Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		else if (d < Integer.MIN_VALUE)
			return Integer.MIN_VALUE;
			
		//else
		//	return (int) d;
		if (d == 0L)
			return 0;
		double d2 = Math.signum(d) * Math.abs(d) / (1L + Math.abs(d));
		int d3 = (int) Math.round(Integer.MAX_VALUE * d2);
		return d3;
	}

	public static int booleanToInt(boolean b) {
		if (b)
			return K;
		else
			return -K;
	}

	public static boolean intToBoolean(int x) {
		return x > 0;
	}

	public static int min(int a, int b, int c) {
		if (a < b)
			return Math.min(a, c);
		else
			return Math.min(b, c);
	}

	public static int editDistance_old(String s, String t) {
		int d[][]; // matrix
		int n; // length of s
		int m; // length of t
		int i; // iterates through s
		int j; // iterates through t
		char s_i; // ith character of s
		char t_j; // jth character of t
		int cost; // cost

		int k = 127;

		// Step 1

		n = s.length();
		m = t.length();
		if (n == 0) {
			return m;
		}
		if (m == 0) {
			return n;
		}
		d = new int[n + 1][m + 1];

		// Step 2

		for (i = 0; i <= n; i++) {
			d[i][0] = i;
		}

		for (j = 0; j <= m; j++) {
			d[0][j] = j;
		}

		// Step 3

		for (i = 1; i <= n; i++) {

			s_i = s.charAt(i - 1);

			// Step 4

			for (j = 1; j <= m; j++) {

				t_j = t.charAt(j - 1);

				// Step 5

				if (s_i == t_j) {
					cost = 0;
				} else {
					//					cost = 127/4 + 3 * Math.abs(s_i - t_j)/4;
					cost = 127;
				}

				// Step 6

				d[i][j] = min(d[i - 1][j] + k, d[i][j - 1] + k, d[i - 1][j - 1] + cost);

			}

		}

		// Step 7

		return d[n][m];
	}

	public static int editDistance(String s, String t) {
		//if (s == null || t == null) {
		//	throw new IllegalArgumentException("Strings must not be null");
		//}

		/*
		    The difference between this impl. and the previous is that, rather 
		     than creating and retaining a matrix of size s.length()+1 by t.length()+1, 
		     we maintain two single-dimensional arrays of length s.length()+1.  The first, d,
		     is the 'current working' distance array that maintains the newest distance cost
		     counts as we iterate through the characters of String s.  Each time we increment
		     the index of String t we are comparing, d is copied to p, the second int[].  Doing so
		     allows us to retain the previous cost counts as required by the algorithm (taking 
		     the minimum of the cost count to the left, up one, and diagonally up and to the left
		     of the current cost count being calculated).  (Note that the arrays aren't really 
		     copied anymore, just switched...this is clearly much better than cloning an array 
		     or doing a System.arraycopy() each time  through the outer loop.)

		     Effectively, the difference between the two implementations is this one does not 
		     cause an out of memory condition when calculating the LD over two very large strings.  		
		 */

		int n = s.length(); // length of s
		int m = t.length(); // length of t

		if (n == 0) {
			return m;
		} else if (m == 0) {
			return n;
		}

		int p[] = new int[n + 1]; //'previous' cost array, horizontally
		int d[] = new int[n + 1]; // cost array, horizontally
		int _d[]; //placeholder to assist in swapping p and d

		// indexes into strings s and t
		int i; // iterates through s
		int j; // iterates through t

		char t_j; // jth character of t

		int cost; // cost

		for (i = 0; i <= n; i++) {
			p[i] = i;
		}

		for (j = 1; j <= m; j++) {
			t_j = t.charAt(j - 1);
			d[0] = j;

			for (i = 1; i <= n; i++) {
				cost = s.charAt(i - 1) == t_j ? 0 : 1;
				// minimum of cell to the left+1, to the top+1, diagonally left and up +cost				
				d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
			}

			// copy current distance counts to 'previous row' distance counts
			_d = p;
			p = d;
			d = _d;
		}

		// our last action in the above loop was to switch d and p, so p now 
		// actually has the most recent cost counts
		return p[n];
	}

	public static int StringEquals(String first, Object second) {
		if (first.equals(second))
			return K; // Identical
		else {
			//System.out.println("Edit distance between " + first + " and " + second
			//       + " is " + -editDistance(first, second.toString()) + " / "
			//      + getLevenshteinDistance(first, (String) second));
			return -editDistance(first, second.toString());
			//return -getLevenshteinDistance(first, (String) second);
		}
	}

	public static int StringEqualsIgnoreCase(String first, String second) {
		return StringEquals(first.toLowerCase(), second.toLowerCase());
	}

	public static int StringStartsWith(String value, String prefix, int start) {
		int len = Math.min(prefix.length(), value.length());
		//System.out.println("StartsWith: " + start + ": " + value + " / " + prefix + ": "
		//        + value.substring(start, start + len) + " / " + prefix + " = "
		//        + StringEquals(value.substring(start, start + len), prefix));
		return StringEquals(value.substring(start, start + len), prefix);
	}

	public static int StringEndsWith(String value, String suffix) {
		int len = Math.min(suffix.length(), value.length());
		String val1 = value.substring(value.length() - len);
		return StringEquals(val1, suffix);
	}

	public static int StringIsEmpty(String value) {
		int len = value.length();
		if (len == 0) {
			return K;
		} else {
			return -len;
		}
	}

	public static int StringRegionMatches(String value, int thisStart, String string,
	        int start, int length, boolean ignoreCase) {
		if (value == null || string == null)
			throw new NullPointerException();

		if (start < 0 || string.length() - start < length) {
			return -K;
		}

		if (thisStart < 0 || value.length() - thisStart < length) {
			return -K;
		}
		if (length <= 0) {
			return K;
		}

		String s1 = value;
		String s2 = string;
		if (ignoreCase) {
			s1 = s1.toLowerCase();
			s2 = s2.toLowerCase();
		}

		return StringEquals(s1.substring(thisStart, length + thisStart), 
							s2.substring(start, length + start));
	}

	public static int instanceOf(Object o, Class<?> c) {
		if (o == null)
			return -K;
		//logger.info("Checking whether " + o.getClass().getName() + " can be assigned to "
		//        + c.getName());
		if (c.isAssignableFrom(o.getClass())) {
			//logger.info("Yes");
			return K;
		} else {
			//logger.info("No");
			return -K;
		}
	}

	public static int isNull(Object o, int opcode) {
		if (opcode == Opcodes.IFNULL)
			return o == null ? K : -K;
		else
			return o != null ? K : -K;
	}

	public static int IOR(int a, int b) {
		int ret = 0;
		if (a > 0 || b > 0) {
			// True

			ret = a;
			if (b > 0 && b < a)
				ret = b;
		} else {
			// False

			ret = a;
			if (b > a)
				ret = b;
		}

		return ret;
	}

	public static int IAND(int a, int b) {
		return Math.min(a, b);
	}

	public static int IXOR(int a, int b) {
		int ret = 0;
		if (a > 0 && b <= 0) {
			// True
			ret = a;
		} else if (b > 0 && a <= 0) {
			ret = b;
		} else {
			// False
			ret = -Math.abs(a - b);
		}

		return ret;
	}

	public static int isEqual(Object o1, Object o2, int opcode) {
		if (opcode == Opcodes.IF_ACMPEQ)
			return o1 == o2 ? K : -K;
		else
			return o1 != o2 ? K : -K;
	}

	private static Stack<Object> parametersObject = new Stack<Object>();
	private static Stack<Boolean> parametersBoolean = new Stack<Boolean>();
	private static Stack<Character> parametersChar = new Stack<Character>();
	private static Stack<Byte> parametersByte = new Stack<Byte>();
	private static Stack<Short> parametersShort = new Stack<Short>();
	private static Stack<Integer> parametersInteger = new Stack<Integer>();
	private static Stack<Float> parametersFloat = new Stack<Float>();
	private static Stack<Long> parametersLong = new Stack<Long>();
	private static Stack<Double> parametersDouble = new Stack<Double>();

	public static boolean popParameterBooleanFromInt() {
		int i = parametersInteger.pop();
		boolean result = i > 0;
		return result;
	}

	public static int popParameterIntFromBoolean() {
		boolean i = parametersBoolean.pop();
		if (i)
			return K;
		else
			return -K;
	}

	public static boolean popParameterBoolean() {
		return parametersBoolean.pop();
	}

	public static char popParameterChar() {
		return parametersChar.pop();
	}

	public static byte popParameterByte() {
		return parametersByte.pop();
	}

	public static short popParameterShort() {
		return parametersShort.pop();
	}

	public static int popParameterInt() {
		return parametersInteger.pop();
	}

	public static float popParameterFloat() {
		return parametersFloat.pop();
	}

	public static long popParameterLong() {
		return parametersLong.pop();
	}

	public static double popParameterDouble() {
		return parametersDouble.pop();
	}

	public static Object popParameterObject() {
		return parametersObject.pop();
	}

	public static Object popParameter(Object o) {
		return parametersObject.pop();
	}

	public static void pushParameter(boolean o) {
		parametersBoolean.push(o);
	}

	public static void pushParameter(char o) {
		parametersChar.push(o);
	}

	public static void pushParameter(byte o) {
		parametersByte.push(o);
	}

	public static void pushParameter(short o) {
		parametersShort.push(o);
	}

	public static void pushParameter(int o) {
		parametersInteger.push(o);
	}

	public static void pushParameter(float o) {
		parametersFloat.push(o);
	}

	public static void pushParameter(long o) {
		parametersLong.push(o);
	}

	public static void pushParameter(double o) {
		parametersDouble.push(o);
	}

	public static void pushParameter(Object o) {
		parametersObject.push(o);
	}
}
