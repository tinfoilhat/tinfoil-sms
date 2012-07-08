/**
 *********************************** NOTICE ***********************************
 *
 * This is a derived work originally written by Frederic Langlet under the
 * Apache License, Version 2.0. The original work's copyright notice and
 * authorship are described below, for more information about the original
 * work see the included NOTICE file.
 *
 *********************************** NOTICE ***********************************
 */

/** 
 * Copyright (C) 2012 Tinfoilhat
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http: *www.gnu.org/licenses/>.
 */

/**
 * Copyright 2011, 2012 Frederic Langlet
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * you may obtain a copy of the License at
 * 
 *                 http: *www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * The Burrows-Wheeler Transform is a reversible transform based on
 * permutation of the data in the original message to reduce the entropy.
 *
 * The initial text can be found here:
 * Burrows M and Wheeler D, [A block sorting lossless data compression
 * algorithm]
 * Technical Report 124, Digital Equipment Corporation, 1994
 *
 * See also Peter Fenwick, [Block sorting text compression - final report]
 * Technical Report 130, 1996
 *
 * This implementation replaces the 'slow' sorting of permutation strings
 * with the construction of a suffix array (faster but more complex).
 * The suffix array contains the indexes of the sorted suffixes.
 *
 * This implementation is based on the SA_IS (Suffix Array Induction Sorting)
 * algorithm.
 * This is a port of sais.c by Yuta Mori
 * (http: *sites.google.com/site/yuta256/sais)
 * See original publication of the algorithm here:
 * [Ge Nong, Sen Zhang and Wai Hong Chan, Two Efficient Algorithms for
 * Linear Suffix Array Construction, 2008]
 * Another good read:
 * http: *labh-curien.univ-st-etienne.fr/~bellet/misc/SA_report.pdf
 *
 * Overview of the algorithm:
 * Step 1 - Problem reduction: the input string is reduced into a smaller
 * string.
 * Step 2 - Recursion: the suffix array of the reduced problem is recursively
 * computed.
 * Step 3 - Problem induction: based on the suffix array of the reduced problem,
 * that of the
 * unreduced problem is induced
 *
 * E.G.
 * Source: mississippi\0
 * Suffixes:
 * mississippi\0 0
 * ississippi\0 1
 * ssissippi\0 2
 * sissippi\0 3
 * issippi\0 4
 * ssippi\0 5
 * sippi\0 6
 * ippi\0 7
 * ppi\0 8
 * pi\0 9
 * i\0 10
 * Suffix array 10 7 4 1 0 9 8 6 3 5 2 => ipssm\0pissii (+ primary index 5)
 * The suffix array and permutation vector are equal when the input is 0
 * terminated
 * In this example, for a non \0 terminated string the permutation vector is
 * pssmipissii.
 * The insertion of a guard is done internally and is entirely transparent.
 */
public class BWT implements ByteTransform
{
    // Not thread safe
    @Override
    public byte[] forward(byte[] input, int blkptr)
    {
        final int len = input.length - blkptr;
        final int[] sa = new int[len];
        byte[] transform = new byte[len+1];
        int[] intArray = new int[len];
        
        if (len < 2)
           return input;

        // Copy input into an array of the equivalent integer representation
        for (int i = 0; i < len; ++i)
        	intArray[i] = input[blkptr+i] & 0xFF;

        // Compute the suffix array and get the primary index 
        final int pIdx = computeSuffixArray(new IndexedIntArray(intArray, 0), sa, 0, len, 256, true);
        transform[0] = (byte) intArray[len-1];

        for (int i = 0; i < pIdx; ++i)
        	transform[blkptr+i+1] = (byte) sa[i];

        for (int i = pIdx + 1; i < len; ++i)
        	transform[blkptr+i] = (byte) sa[i];

        // Add the primary index to the end of the transform
        transform[len] = (byte) (pIdx + 1);
        
        return transform;
    }


    // Not thread safe
    @Override
    public byte[] inverse(byte[] input, int blkptr)
    {
    	/*
    	 *  Set the length as one less than input length as last byte of the
    	 *  input should be ignored as it contains the primary index value
    	 */
    	int len = (input.length - 1) - blkptr;
        
    	final int[] buckets_ = new int[256];
        final int[] hist = new int[len];
        final byte[] buffer = new byte[len];
        byte[] invTransform = new byte[len];
        
        // The primary index is the last byte of the input
        final int pIdx = (int) input[len] & 0xFF;
        
        for (int i=0; i<256; i++)
           buckets_[i] = 0;

       // Create histogram
       for (int i=0; i<len; i++)
          hist[i] = buckets_[input[blkptr+i] & 0xFF]++;

       // Create cumulative histogram
       for (int i=0, sum=0; i<256; i++)
       {
          final int val = buckets_[i];
          buckets_[i] = sum;
          sum += val;
       }

       for (int i=len-1, val=0; i>=0; i--)
       {
          final byte idx = input[blkptr+val];
          buffer[i] = idx;
          val = hist[val] + buckets_[idx & 0xFF];
          val += ((val - pIdx) >>> 31);
       }

       System.arraycopy(buffer, 0, invTransform, blkptr, len);
       return invTransform;
     }


      // find the start or end of each bucket
      private static void getCounts(IndexedIntArray src, IndexedIntArray dst, int n, int k)
      {
        final int[] dstArray = dst.array;
        final int[] srcArray = src.array;
        final int dstIdx = dst.index;
        final int srcIdx = src.index;
        final int end1 = dstIdx + k;
        final int end2 = srcIdx + n;

        for (int i=dstIdx; i<end1; i++)
           dstArray[i] = 0;

        for (int i=srcIdx; i<end2; i++)
           dstArray[dstIdx+srcArray[i]]++;
      }


      private static void getBuckets(IndexedIntArray src, IndexedIntArray dst, int k, boolean end)
      {
        int sum = 0;
        final int[] dstArray = dst.array;
        final int[] srcArray = src.array;
        final int dstIdx = dst.index;
        final int srcIdx = src.index;

        if (end == true)
        {
           for (int i=0; i<k; i++)
           {
              sum += srcArray[srcIdx+i];
              dstArray[dstIdx+i] = sum;
           }
        }
        else
        {
           for (int i=0; i<k; i++)
           {
              // The temp variable is required if srcArray == dstArray
              final int tmp = srcArray[srcIdx+i];
              dstArray[dstIdx+i] = sum;
              sum += tmp;
           }
        }
      }


      // sort all type LMS suffixes
      private void sortLMSSuffixes(IndexedIntArray src, int[] sa, IndexedIntArray C,
              IndexedIntArray B, int n, int k)
      {
        // compute sal
        if (C == B)
           getCounts(src, C, n, k);

        // find starts of buckets
        getBuckets(C, B, k, false);

        int j = n - 1;
        final int[] srcArray = src.array;
        final int srcIdx = src.index;
        int c1 = srcArray[srcIdx+j];
        int b = B.array[B.index+c1];
        j--;
        sa[b++] = (srcArray[srcIdx+j] < c1) ? ~j : j;

        for (int i=0; i<n; i++)
        {
          j = sa[i];

          if (j > 0)
          {
            int c0 = srcArray[srcIdx+j];

            if (c0 != c1)
            {
               B.array[B.index+c1] = b;
               c1 = c0;
               b = B.array[B.index+c1];
            }

            j--;
            sa[b++] = (srcArray[srcIdx+j] < c1) ? ~j : j;
            sa[i] = 0;
          }
          else if (j < 0)
            sa[i] = ~j;
        }

        // compute sas
        if (C == B)
           getCounts(src, C, n, k);

        // find ends of buckets
        getBuckets(C, B, k, true);
        c1 = 0;
        b = B.array[B.index+c1];

        for (int i=n-1; i>=0; i--)
        {
          j = sa[i];

          if (j > 0)
          {
            int c0 = srcArray[srcIdx+j];

            if (c0 != c1)
            {
               B.array[B.index+c1] = b;
               c1 = c0;
               b = B.array[B.index+c1];
            }

            j--;
            b--;
            sa[b] = (srcArray[srcIdx+j] > c1) ? ~(j + 1) : j;
            sa[i] = 0;
          }
        }
      }


      private int postProcessLMS(IndexedIntArray src, int[] sa, int n, int m)
      {
        int i = 0;
        int j;
        final int index = src.index;
        final int[] array = src.array;

        // compact all the sorted substrings into the first m items of sa
        // 2*m must be not larger than n
        for (int p; (p=sa[i])<0; i++)
           sa[i] = ~p;

        if (i < m)
        {
          j = i;
          i++;

          while (true)
          {
            final int p = sa[i++];

            if (p >= 0)
               continue;

            sa[j++] = ~p;
            sa[i-1] = 0;

            if (j == m)
               break;
          }
        }

        // store the length of all substrings
        i = n - 1;
        j = n - 1;
        int c0 = array[index+n-1];
        int c1;

        do
        {
          c1 = c0;
          i--;
        }
        while ((i >= 0) && ((c0 = array[index+i]) >= c1));

        while (i >= 0)
        {
          do
          {
            c1 = c0;
            i--;
          }
          while ((i >= 0) && ((c0 = array[index+i]) <= c1));

          if (i < 0)
             break;

          sa[m+((i+1)>>1)] = j - i;
          j = i + 1;

          do
          {
            c1 = c0;
            i--;
          }
          while ((i >= 0) && ((c0 = array[index+i]) >= c1));
        }

        // find the lexicographic names of all substrings
        int name = 0;
        int q = n;
        int qlen = 0;

        for (int ii=0; ii<m; ii++)
        {
          final int p = sa[ii];
          final int plen = sa[m+(p>>1)];
          boolean diff = true;

          if ((plen == qlen) && ((q + plen) < n))
          {
            j = 0;

            while ((j<plen) && (array[index+p+j] == array[index+q+j]))
               j++;

            if (j == plen)
               diff = false;
          }

          if (diff == true)
          {
             name++;
             q = p;
             qlen = plen;
          }

          sa[m+(p>>1)] = name;
        }

        return name;
      }


      private void induceSuffixArray(IndexedIntArray src, int[] sa, IndexedIntArray buf1,
              IndexedIntArray buf2, int n, int k)
      {
        // compute sal
        if (buf1 == buf2)
           getCounts(src, buf1, n, k);

        // find starts of buckets
        getBuckets(buf1, buf2, k, false);

        final int srcIdx = src.index;
        final int[] srcArray = src.array;
        final int bufIdx = buf2.index;
        final int[] bufArray = buf2.array;
        int j = n - 1;
        int c1 = srcArray[srcIdx+j];
        int b = bufArray[bufIdx+c1];
        sa[b++] = ((j > 0) && (srcArray[srcIdx+j-1] < c1)) ? ~j : j;

        for (int i=0; i<n; i++)
        {
          j = sa[i];
          sa[i] = ~j;

          if (j > 0)
          {
            j--;
            final int c0 = srcArray[srcIdx+j];

            if (c0 != c1)
            {
               bufArray[bufIdx+c1] = b;
               c1 = c0;
               b = bufArray[bufIdx+c1];
            }

            sa[b++] = ((j > 0) && (srcArray[srcIdx+j-1] < c1)) ? ~j : j;
          }
        }

        // compute sas
        if (buf1 == buf2)
           getCounts(src, buf1, n, k);

        // find ends of buckets
        getBuckets(buf1, buf2, k, true);
        c1 = 0;
        b = bufArray[bufIdx+c1];

        for (int i=n-1; i>=0; i--)
        {
          j = sa[i];

          if (j > 0)
          {
            j--;
            final int c0 = srcArray[srcIdx+j];

            if (c0 != c1)
            {
               bufArray[bufIdx+c1] = b;
               c1 = c0;
               b = bufArray[bufIdx+c1];
            }

            b--;
            sa[b] = ((j == 0) || (srcArray[srcIdx+j-1] > c1)) ? ~j : j;
          }
          else
            sa[i] = ~j;
        }
      }


      private int computeBWT(IndexedIntArray data, int[] sa, IndexedIntArray iia1,
              IndexedIntArray iia2, int n, int k)
      {
        // compute sal
        if (iia1 == iia2)
           getCounts(data, iia1, n, k);

        // find starts of buckets
        getBuckets(iia1, iia2, k, false);
        int[] array = data.array;
        int[] buffer = iia2.array;
        int arrayIdx = data.index;
        int bufferIdx = iia2.index;
        int j = n - 1;
        int c1 = array[arrayIdx+j];
        int b = buffer[bufferIdx+c1];
        sa[b++] = ((j > 0) && (array[arrayIdx+j-1] < c1)) ? ~j : j;

        for (int i=0; i<n; i++)
        {
          j = sa[i];

          if (j > 0)
          {
            j--;
            final int c0 = array[arrayIdx+j];
            sa[i] = ~c0;

            if (c0 != c1)
            {
               buffer[bufferIdx+c1] = b;
               c1 = c0;
               b = buffer[bufferIdx+c1];
            }

            sa[b++] = ((j > 0) && (array[arrayIdx+j-1] < c1)) ? ~j : j;
          }
          else if (j != 0)
            sa[i] = ~j;
        }

        // compute sas
        if (iia1 == iia2)
           getCounts(data, iia1, n, k);

        // find ends of buckets
        getBuckets(iia1, iia2, k, true);
        c1 = 0;
        b = buffer[bufferIdx+c1];
        int pidx = -1;

        for (int i=n-1; i>=0; i--)
        {
          j = sa[i];

          if (j > 0)
          {
            j--;
            final int c0 = array[arrayIdx+j];
            sa[i] = c0;

            if (c0 != c1)
            {
               buffer[bufferIdx+c1] = b;
               c1 = c0;
               b = buffer[bufferIdx+c1];
            }

            b--;
            sa[b] = ((j > 0) && (array[arrayIdx+j-1] > c1)) ? ~(array[arrayIdx+j-1]) : j;
          }
          else if (j != 0)
            sa[i] = ~j;
          else
            pidx = i;
        }

        return pidx;
      }


      // find the suffix array sa of T[0..n-1] in {0..k-1}^n
      private int computeSuffixArray(IndexedIntArray data, int[] sa, int fs, int n, int k, boolean isbwt)
      {
        IndexedIntArray C, B;
        int flags;

        if (k <= 256)
        {
          C = new IndexedIntArray(new int[k], 0);

          if (k <= fs)
          {
             B = new IndexedIntArray(sa, n+fs-k);
             flags = 1;
          }
          else
          {
             B = new IndexedIntArray(new int[k], 0);
             flags = 3;
          }
        }
        else if (k <= fs)
        {
          C = new IndexedIntArray(sa, n+fs-k);

          if (k <= (fs-k))
          {
             B = new IndexedIntArray(sa, n+fs-(k+k));
             flags = 0;
          }
          else if (k <= 1024)
          {
             B = new IndexedIntArray(new int[k], 0);
             flags = 2;
          }
          else
          {
             B = C;
             flags = 8;
          }
        }
        else
        {
          B = new IndexedIntArray(new int[k], 0);
          C = B;
          flags = 12;
        }

        // stage 1: reduce the problem by at least 1/2, sort all the LMS-substrings
        // find ends of buckets
        getCounts(data, C, n, k);
        getBuckets(C, B, k, true);

        for (int ii=0; ii<n; ii++)
           sa[ii] = 0;

        int b = -1;
        int i = n - 1;
        int j = n;
        int m = 0;
        final int[] array = data.array;
        final int arrayIdx = data.index;
        int c0 = array[arrayIdx+n-1];
        int c1;
        int name = 0;

        do
        {
           c1 = c0;
           i--;
        }
        while ((i >= 0) && ((c0 = array[arrayIdx+i]) >= c1));

        final int[] buffer = B.array;
        final int bufferIdx = B.index;

        while (i >= 0)
        {
          do
          {
             c1 = c0;
             i--;
          }
          while ((i >= 0) && ((c0 = array[arrayIdx+i]) <= c1));

          if (i >= 0)
          {
            if (b >= 0)
               sa[b] = j;

            buffer[bufferIdx+c1]--;
            b = buffer[bufferIdx+c1];
            j = i;
            m++;

            do
            {
              c1 = c0;
              i--;
            }
            while((i >= 0) && ((c0 = array[arrayIdx+i]) >= c1));
          }
        }

        if (m > 1)
        {
          sortLMSSuffixes(data, sa, C, B, n, k);
          name = postProcessLMS(data, sa, n, m);
        }
        else if (m == 1)
        {
          sa[b] = j + 1;
          name = 1;
        }
        else
          name = 0;

        // stage 2: solve the reduced problem recurse if names are not yet unique
        if (name < m)
        {
          int newfs = (n+fs) - (m+m);

          if ((flags & (13)) == 0)
          {
            if ((k + name) <= newfs)
              newfs -= k;
            else
              flags |= 8;
          }

          j = m + m + newfs - 1;

          for (int ii=m+(n>>1)-1; ii>=m; ii--)
          {
            if (sa[ii] != 0)
              sa[j--] = sa[ii] - 1;
          }

          computeSuffixArray(new IndexedIntArray(sa, m + newfs), sa, newfs, m, name, false);

          i = n - 1;
          j = m + m - 1;
          c0 = array[arrayIdx+n-1];

          do
          {
            c1 = c0;
            i--;
          }
          while ((i >= 0) && ((c0 = array[arrayIdx+i]) >= c1));

          while (i >= 0)
          {
            do
            {
              c1 = c0;
              i--;
            }
            while ((i >= 0) && ((c0 = array[arrayIdx+i]) <= c1));

            if (i >= 0)
            {
              sa[j--] = i + 1;

              do
              {
                c1 = c0;
                i--;
              }
              while ((i >= 0) && ((c0 = array[arrayIdx+i]) >= c1));
            }
          }

          for (int ii=0; ii<m; ii++)
             sa[ii] = sa[m+sa[ii]];

          if ((flags & 4) != 0)
          {
            B = new IndexedIntArray(new int[k], 0);
            C = B;
          }
          else if((flags & 2) != 0)
            B = new IndexedIntArray(new int[k], 0);
        }

        // stage 3: induce the result for the original problem
        if ((flags & 8) != 0)
           getCounts(data, C, n, k);

        // put all left-most S characters into their buckets
        if (m > 1)
        {
          // find ends of buckets
          getBuckets(C, B, k, true);
          i = m - 1;
          j = n;
          int p = sa[m-1];
          c1 = array[arrayIdx+p];

          do
          {
            c0 = c1;
            int q = B.array[B.index+c0];

            while (q < j)
               sa[--j] = 0;

            do
            {
              sa[--j] = p;

              if (--i < 0)
                 break;

              p = sa[i];
              c1 = array[arrayIdx+p];
            }
            while(c1 == c0);
          }
          while (i >= 0);

          while (j > 0)
             sa[--j] = 0;
        }

        int pidx = 0;

        if (isbwt == false)
           induceSuffixArray(data, sa, C, B, n, k);
        else
           pidx = computeBWT(data, sa, C, B, n, k);

        return pidx;
     }
}
