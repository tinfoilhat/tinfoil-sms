import java.lang.reflect.Array;
import java.text.Collator;
import java.util.Comparator;

/**
 * A fast implementation of the quicksort algorithm that avoids the N^2 
 * worst-case scenario by picking better pivot values. Uses the median-of-three
 * approach to pick better partitions/pivot values.
 */
public abstract class FastQuickSort
{
	/**
	 * An optimized implementation of the quickSort which uses the median-of-three
	 * method for selecting the pivot value to avoid the N^2 worst-case scenario.
	 * Optimization idea came from the article on wikipedia, implementation is
	 * based on "Algorithms" and "Algorithms in C++" books by Robert Sedgewick
	 * 
	 * NOTE: CURRENTLY SUPPORTS GENERICS FOR ARRAYS OF OBJECTS THAT IMPLEMENT
	 * COMPARABLE. PRIMITIVE TYPES ARE NOT SUPPORTED AS IT IS IMPOSSIBLE USING
	 * GENERICS
	 * 
	 * @see http://en.wikipedia.org/wiki/Quicksort
	 * @see http://stackoverflow.com/questions/2071929/generics-and-sorting-in-java
	 * 
	 * @param array The array to sort, must be objects that implement Comparable
	 */
	public static <T extends Comparable<? super T>> void sort(T[] array) 
			throws Exception
	{
		quickSort(array, 0, array.length - 1);
	}
	
	/**
	 * An optimized implementation of the quickSort which uses the median-of-three
	 * method for selecting the pivot value to avoid the N^2 worst-case scenario.
	 * Optimization idea came from the article on wikipedia, implementation is
	 * based on "Algorithms" and "Algorithms in C++" books by Robert Sedgewick
	 * 
	 * NOTE: CURRENTLY SUPPORTS GENERICS FOR ARRAYS OF OBJECTS THAT IMPLEMENT
	 * COMPARABLE. PRIMITIVE TYPES ARE NOT SUPPORTED AS IT IS IMPOSSIBLE USING
	 * GENERICS
	 * 
	 * @see http://en.wikipedia.org/wiki/Quicksort
	 * @see http://stackoverflow.com/questions/2071929/generics-and-sorting-in-java
	 * 
	 * @param array The array to sort, must be objects that implement Comparable
	 * @param c		The comparator to use for performing comparisons on the object
	 * 
	 * @throws IllegalArgumentException	If the comparison object does not implement Comparator
	 */
	public static <T extends Comparable<? super T>> void sort(T[] array, Comparator<T> c) 
			throws Exception, IllegalArgumentException
	{
		if (! (c instanceof Comparator || c instanceof Collator))
		{
			throw new IllegalArgumentException("Only comparison objects that implement Comparator are supported!");
		}
		
		quickSort(array, c, 0, array.length - 1);
	}
	
	
	/**
	 * An optimized implementation of the quickSort which uses the median-of-three
	 * method for selecting the pivot value to avoid the N^2 worst-case scenario.
	 * Optimization idea came from the article on wikipedia, implementation is
	 * based on "Algorithms" and "Algorithms in C++" books by Robert Sedgewick
	 * 
	 * NOTE: CURRENTLY SUPPORTS GENERICS FOR ARRAYS OF OBJECTS THAT IMPLEMENT
	 * COMPARABLE. PRIMITIVE TYPES ARE NOT SUPPORTED AS IT IS IMPOSSIBLE USING
	 * GENERICS
	 * 
	 * @see http://en.wikipedia.org/wiki/Quicksort
	 * @see http://stackoverflow.com/questions/2071929/generics-and-sorting-in-java
	 * 
	 * @param array The array to sort, must be objects that implement Comparable
	 * @param l	 The left boundary of array
	 * @param r	 The right boundary of array
	 */
	private static <T extends Comparable<? super T>> void quickSort(T[] array, int l, int r) 
			throws Exception
	{
		T value;
		int M = 23;
		int i, j;

		
		if ((r - l) > M)
		{
			/*
			 * Apply the median-of-three method
			 */
			i = (r + l) / 2;
			if (array[l].compareTo(array[i]) > 0) {
				swap(array, l, i);	
			}
			if (array[l].compareTo(array[r]) > 0) {
				swap(array, l, r);
			}
			if (array[i].compareTo(array[r]) > 0) {
				swap(array, i, r);
			}
		
			/*
			 * Get the pivot value
			 */
			j = r - 1;
			swap(array, i, j);
			i = l;
			value = array[j];
			
			/*
			 * Reorder the values around the pivot value
			 */
			for(;;)
			{
				while(array[++i].compareTo(value) < 0);
				while(array[--j].compareTo(value) > 0);
				
				if (j < i)
				{
					break;
				}
				swap (array, i, j);
			}
			swap(array, i, r-1);
			
			/*
			 * Apply quicksort recursively to left and right partitions
			 */
			quickSort(array, l, j);
			quickSort(array, i+1, r);
		}
		/*
		 * Apply insertion sort for small partitions where it is faster than quicksort
		 * NOTE: Through repeated empirical testing of large random data sets this
		 * improved performance by ~10% 
		 */
		else
		{
			insertionSort(array, l, r);
		}
	}

	/**
	 * An optimized implementation of the quickSort which uses the median-of-three
	 * method for selecting the pivot value to avoid the N^2 worst-case scenario.
	 * Optimization idea came from the article on wikipedia, implementation is
	 * based on "Algorithms" and "Algorithms in C++" books by Robert Sedgewick
	 * 
	 * NOTE: CURRENTLY SUPPORTS GENERICS FOR ARRAYS OF OBJECTS THAT IMPLEMENT
	 * COMPARABLE. PRIMITIVE TYPES ARE NOT SUPPORTED AS IT IS IMPOSSIBLE USING
	 * GENERICS
	 * 
	 * @see http://en.wikipedia.org/wiki/Quicksort
	 * @see http://stackoverflow.com/questions/2071929/generics-and-sorting-in-java
	 * 
	 * @param array The array to sort, must be objects that implement Comparable
	 * @param c		The comparator to use for performing comparisons on the object
	 * @param l	 The left boundary of array
	 * @param r	 The right boundary of array
	 */
	@SuppressWarnings("unchecked")
	private static <T extends Comparable<? super T>> void quickSort(T[] array, Comparator<T> c, int l, int r) 
			throws Exception
	{
		T value;
		int M = 23;
		int i, j;

		
		if ((r - l) > M)
		{
			/*
			 * Apply the median-of-three method
			 */
			i = (r + l) / 2;

			if (c.compare(array[l], array[i]) > 0) {
				swap(array, l, i);	
			}
			if (c.compare(array[l], array[r]) > 0) {
				swap(array, l, r);
			}
			if (c.compare(array[i], array[r]) > 0) {
				swap(array, i, r);
			}
		
			/*
			 * Get the pivot value
			 */
			j = r - 1;
			swap(array, i, j);
			i = l;
			value = array[j];
			
			/*
			 * Reorder the values around the pivot value
			 */
			for(;;)
			{
				while(c.compare(array[++i], value) < 0);
				while(c.compare(array[--j], value) > 0);
				
				if (j < i)
				{
					break;
				}
				swap (array, i, j);
			}
			swap(array, i, r-1);
			
			/*
			 * Apply quicksort recursively to left and right partitions
			 */
			quickSort(array, c, l, j);
			quickSort(array, c, i+1, r);
		}
		/*
		 * Apply insertion sort for small partitions where it is faster than quicksort
		 * NOTE: Through repeated empirical testing of large random data sets this
		 * improved performance by ~10% 
		 */
		else
		{
			insertionSort(array, c, l, r);
		}
	}
	
	/**
	 * Swaps the values in the array at the indexes provided
	 * 
	 * @param  array The array, must be objects that implement Comparable
	 * @param i Index of the value to be swapped
	 * @param j Index of the value to be swapped
	 */
	private static <T extends Comparable<? super T>> void swap(T[] array, int i, int j)
	{
		T temp;
		temp = array[i]; 
		array[i] = array[j];
		array[j] = temp;
	}
	
	
	/**
	 * Insertion sort, it is used internally by the quicksort algorithm to sort
	 * the small partitions of values as insertion sort is faster when there is a
	 * relatively small amount of values to sort
	 * 
	 * NOTE: Through repeated empirical testing of large random data sets using
	 * insertion sort for small sorting operations improved performance by ~10% 
	 * 
	 * @param array The array to sort, must be objects that implement Comparable
	 * @param l	 The left boundary of array
	 * @param r	 The right boundary of array
	 */
	private static <T extends Comparable<? super T>> void insertionSort(T[] array, int l, int r)
			throws Exception
	{
		T value;
		int i, j;

		for (i = l + 1; i <= r; ++i)
		{
			value = array[i];
			j = i;
			
			while ((j > l) && (array[j-1].compareTo(value) > 0))
			{
				array[j] = array[j-1];
				--j;
			}
			array[j] = value;
	 	}
	}
	
	
	/**
	 * Insertion sort, it is used internally by the quicksort algorithm to sort
	 * the small partitions of values as insertion sort is faster when there is a
	 * relatively small amount of values to sort
	 * 
	 * NOTE: Through repeated empirical testing of large random data sets using
	 * insertion sort for small sorting operations improved performance by ~10% 
	 * 
	 * @param array The array to sort, must be objects that implement Comparable
	 * @param c		The comparator to use for performing comparisons on the object
	 * @param l	 The left boundary of array
	 * @param r	 The right boundary of array
	 */
	private static <T extends Comparable<? super T>> void insertionSort(T[] array, Comparator<T> c, int l, int r)
			throws Exception
	{
		T value;
		int i, j;

		for (i = l + 1; i <= r; ++i)
		{
			value = array[i];
			j = i;
			
			while ((j > l) && (c.compare(array[j-1], value) > 0))
			{
				array[j] = array[j-1];
				--j;
			}
			array[j] = value;
	 	}
	}
}
