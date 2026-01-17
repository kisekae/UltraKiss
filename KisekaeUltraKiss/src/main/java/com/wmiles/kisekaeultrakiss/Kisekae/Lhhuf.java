package com.wmiles.kisekaeultrakiss.Kisekae ;

// Title:        Kisekae UltraKiss
// Version:      3.4  (May 11, 2023)
// Copyright:    Copyright (c) 2002-2023
// Author:       William Miles
// Description:  Kisekae Set System
//
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

/*
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%  This copyright notice and this permission notice shall be included in      %
%  all copies or substantial portions of UltraKiss.                           %
%                                                                             %
%  The software is provided "as is", without warranty of any kind, express or %
%  implied, including but not limited to the warranties of merchantability,   %
%  fitness for a particular purpose and noninfringement.  In no event shall   %
%  William Miles be liable for any claim, damages or other liability,         %
%  whether in an action of contract, tort or otherwise, arising from, out of  %
%  or in connection with Kisekae UltraKiss or the use of UltraKiss.           %
%                                                                             %
%  William Miles                                                              %
%  144 Oakmount Rd. S.W.                                                      %
%  Calgary, Alberta                                                           %
%  Canada  T2V 4X4                                                            %
%                                                                             %
%  w.miles@wmiles.com                                                         %
%                                                                             %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
*/


/* Ver. 0.00 Original                      1988.05.23  Y.Tagawa */
/* Ver. 0.01 Alpha Version (for 4.2BSD)    1989.05.28  Y.Tagawa */
/* Ver. 0.02 Alpha Version Rel.2           1989.05.29  Y.Tagawa */
/* Ver. 0.03 Release #3  Beta Version      1989.07.02  Y.Tagawa */
/* Ver. 0.03a Debug                        1989.07.03  Y.Tagawa */
/* Ver. 0.03b Modified                     1989.07.13  Y.Tagawa */
/* Ver. 0.03c Debug (Thanks to void@rena.dit.junet) 1989.08.09  Y.Tagawa */
/* Ver. 0.03d Modified (quiet and verbose)    1989.09.14  Y.Tagawa */
/* V1.00 Fixed                             1989.09.22  Y.Tagawa */
/* V1.01 Bug Fixed                         1989.12.25  Y.Tagawa */
/* DOS-Version Original LHx V C2.01     (C) H.Yoshizaki */
/* V2.00  UNIX Lharc + DOS LHx -> OSK LHx  1990.11.01  Momozou */
/* V2.01  Minor Modified                   1990.11.24  Momozou */
/* Ver. 0.02  LHx for UNIX                 1991.11.18  M.Oki */
/* Ver. 0.03  LHa for UNIX                 1991.12.17  M.Oki */
/* Ver. 0.04  LHa for UNIX  beta version   1992.01.20  M.Oki */
/* Ver. 1.00  LHa for UNIX  Fixed          1992.03.19  M.Oki */
/* Ver. 1.10  for Symblic Link             1993.06.25  N.Watazaki */
/* Ver. 1.11  for Symblic Link  Bug Fixed  1993.08.18  N.Watazaki */
/* Ver. 1.12  for File Date Check          1993.10.28  N.Watazaki */
/* Ver. 1.13  Bug Fixed (Idicator calcurate)  1994.02.21  N.Watazaki */
/* Ver. 1.13a Bug Fixed (Sym. Link delete)    1994.03.11  N.Watazaki */
/* Ver. 1.13b Bug Fixed (Sym. Link delete)    1994.07.29  N.Watazaki */
/* Ver. 1.14  Source All chagned           1995.01.14  N.Watazaki */
/* Ver. 1.14b,c  Bug Fixed                 1996.03.07  t.okamoto */
/* Java 1.0   Porting to Java 1997.05.15 Todo Software */

/* support method */
/* -lh5- 8k sliding dictionary(max 256 bytes) + static Huffman
   + improved encoding of position and trees */
/* -lh6- 32k sliding dictionary(max 256 bytes) + static Huffman
   + improved encoding of position and trees */
/* -lh7- 64k sliding dictionary(max 256 bytes) + static Huffman
   + improved encoding of position and trees */

import java.io.*;

final class Lhhuf 
{
	private static final int USHRT_BIT = 16;
	private static final int CHAR_BIT = 8;
	private static final int UCHAR_MAX = 255;
	private static final int SHRT_MIN = 0x8000;
	private static final int SHRT_MAX = 0x7fff;
	private static final int USHRT_MIN = 0x0;
	private static final int USHRT_MAX = 0xffff;
	private static final int NIL = 0;
	private static final int MAX_DICBIT = 16;
	private static final int MAX_DICSIZ = (1 << MAX_DICBIT);
	private static final int MATCHBIT = 8; /* bits for MAXMATCH - THRESHOLD */
	private static final int MAXMATCH = 256; /* formerly F (not more than UCHAR_MAX + 1) */
	private static final int THRESHOLD =  3; /* choose optimal value */
	/* alphabet = {0, 1, 2, ..., NC - 1} */
	private static final int CBIT =  9; /* $\lfloor \log_2 NC \rfloor + 1$ */

   private int bytes = 0 ;          /* bytes processed */
   private int lastbytes = 0 ;      /* last bytes processed */
   private FileWriter fw = null ;   /* FileWriter to track progress */
  
	private InputStream in;

	public int CRC() { return crc; }
	
	public void setCRC(int crc) { this.crc = crc; }

	private final void updatecrc(int ch) 
	{
		crc = LhaCrc16.crctable[(crc ^ (ch)) & 0xFF] ^ (crc >> CHAR_BIT);
	}
	
	private final  int fread_crc(byte b[], int start, int count, InputStream in)
		throws IOException 
	{
		int i = 0;
		while (count-- != 0) 
		{
			int ch = in.read();
			if (ch < 0) { eof = true; return i; }
         int progress = ++bytes - lastbytes ;
         if (fw != null && progress > 1024) 
         {
             fw.updateProgress(progress) ;
             lastbytes = bytes ;
         }
			b[start++] = (byte)ch;
			//	updatecrc(ch);
			crc = LhaCrc16.crctable[(crc ^ (ch)) & 0xFF] ^ (crc >> CHAR_BIT);

			i++;
		}
		size += i;
		return i;
	}
	
	private final  int fwrite_crc(byte b[], int start, int count, OutputStream out)
		throws IOException 
	{
		int i = 0;
		int ch;
		while (count-- != 0) 
		{
			// PrintLn.println(">" + b[start]);
			out.write(ch = b[start++]);
			//	updatecrc(ch);
			crc = LhaCrc16.crctable[(crc ^ (ch)) & 0xFF] ^ (crc >> CHAR_BIT);

			i++;
		}
		size += i;
		return i;
	}
	
	int size;
	
	long length() { return (long) compsize; }
	
	long size() { return (long) size; }
  
	private final  void memset(byte b[], int start, int ch, int count)
	{
		while (count-- != 0) 
		{
			b[start++] = (byte)ch;
		}
	}
  
	private int pos, matchpos, avail;
	private int remainder, matchlen;
	private int maxmatch;
	private int dicsiz;
	private int textbuf(int i) { return text[i] & 0xff; }
	private int max_hash_val;
	private int hash1, hash2;
	private int dicbit ;
	
	// Working storage.  As this is static storage, only one LHHUF
	// object can be used at one time.
	
	private static byte text[] = new byte[(1 << MAX_DICBIT - 2) * 2 + MAXMATCH] ;
	private static int level[] = new int[((1 << MAX_DICBIT - 2) + UCHAR_MAX + 1)] ;
	private static int childcount[] = new int[((1 << MAX_DICBIT - 2) + UCHAR_MAX + 1)] ;
	private static short position[] = new short[((1 << MAX_DICBIT - 2) + UCHAR_MAX + 1)] ;
	private static int parent[] = new int[(1 << MAX_DICBIT - 2) * 2] ;
	private static int prev[] = new int[(1 << MAX_DICBIT - 2) * 2] ;
	private static int next[] = new int[3 * (1 << MAX_DICBIT - 2) + ((1 << MAX_DICBIT - 2) / 512 + 1) * UCHAR_MAX + 1] ;
  
	private int encoded_origsize, compsize, count, crc;
	private boolean unpackable;
	
	public Lhhuf(int origsize, int method)
	{
		init(origsize,method);
	}
	
	// There is a large memory requirement for the LHHUF arrays.
	// This takes some time to allocate.  We use static arrays to 
	// reduce the memory management overhead.
	
	private final  void init(int origsize, int method)
	{
		dicbit = 13;
      if (method == LhaEntry.LH6) dicbit = 15 ;
      if (method == LhaEntry.LH7) dicbit = 16 ;
		this.origsize = origsize ;
		maxmatch = MAXMATCH;
		dicsiz = 1 << dicbit;
		//    PrintLn.println(dicsiz);
    
		max_hash_val = 3 * dicsiz + (dicsiz / 512 + 1) * UCHAR_MAX;
		//    PrintLn.println(max_hash_val);
    
		text = new byte[dicsiz * 2 + maxmatch];
		level = new int[(dicsiz + UCHAR_MAX + 1)];
		childcount = new int[(dicsiz + UCHAR_MAX + 1)];
		position = new short[(dicsiz + UCHAR_MAX + 1)];
		parent = new int[dicsiz * 2];
		prev = new int[dicsiz * 2];
		next = new int[max_hash_val + 1];
	}
  
	private final  void init_slide()
	{
		int i;
      
		for (i = dicsiz; i <= dicsiz + UCHAR_MAX; i++) 
		{
			level[i] = 1;
			position[i] = (short)NIL; /* sentinel */
		}
		for (i = dicsiz; i < dicsiz * 2; i++)
			parent[i] = NIL;
		avail = 1;
		for (i = 1; i < dicsiz - 1; i++)
			next[i] = i + 1;
		next[dicsiz - 1] = NIL;
		for (i = dicsiz * 2; i <= max_hash_val; i++)
			next[i] = NIL;
		hash1 = dicbit - 9;
		hash2 = dicsiz * 2;
	}
  
	private final  int HASH(int p, int c) 
	{ 
		return (((p) + ((c) << hash1) + hash2)) & 0xffff; 
	}
  
	private final  int child(int q, int c)
	{
		int   h,  r;
      
		//    h = HASH(q, c);
		h = (((q) + ((c) << hash1) + hash2)) & 0xffff; 
      
		r = next[h];
		parent[NIL] = q; /* sentinel */
		while (parent[r] != q)
			r = next[r];
		return r;
	}
  
	private int k;
  
	private final  void makechild(int q, int c, int r)
	{
		int h, t;
      
		//      h = HASH(q, c);
		h = (((q) + ((c) << hash1) + hash2)) & 0xffff; 
      
		//      PrintLn.println(k++ + "makechild:(" + q + "," + c + "," + r + ")" + h + "," + childcount[q]);
      
		t = next[h];
		next[h] = r;
		next[r] = t;
		prev[t] = r;
		prev[r] = h;
		parent[r] = q;
		childcount[q]++;
	}
  
	private final  void split(int old)
	{
		int newval, t;
      
		//      PrintLn.println("split(" + old + ") " + avail);
      
		newval = avail;
		avail = next[newval];
		childcount[newval] = 0;
		t = prev[old];
		prev[newval] = t;
		next[t] = newval;
		t = next[old];
		next[newval] = t;
		prev[t] = newval;
		parent[newval] = parent[old];
		level[newval] = matchlen;
		position[newval] = (short)pos;
		makechild(newval, textbuf(matchpos + matchlen), old);
		makechild(newval, textbuf(pos + matchlen), pos);
	}
  
	private final  void insert_node()
	{
		int q, r, j, t;
		int c;
      
		if (matchlen >= 4) 
		{
			matchlen--;
			r = (matchpos + 1) | dicsiz;
			while ((q = parent[r]) == NIL)
         {
				r = next[r];
            if (r == next[r]) break ; // infinite loop bug?  Sep 21 2008
         }
			while (level[q] >= matchlen) 
			{
				r = q;
				q = parent[q];
            if (q == r) break ; // infinite loop? FileWriter write daidar.lzh encoding daish9a.cel from OtakuWorld Jul 28 2025
			}
			t = q;
			while (position[t] < 0) 
			{
				position[t] = (short)pos;
				t = parent[t];
			}
			if (t < dicsiz)
				position[t] = (short)(pos | SHRT_MIN);
		}
		else 
		{
			q = textbuf(pos) + dicsiz;
			c = textbuf(pos + 1);
			if ((r = child(q, c)) == NIL) 
			{
				makechild(q, c, pos);
				matchlen = 1;
				return;
			}
			matchlen = 2;
		}
		
		for (;;) 
		{
			if (r >= dicsiz) 
			{
				j = maxmatch;
				matchpos = r;
			}
			else 
			{
				j = level[r];
				matchpos = position[r] & SHRT_MAX;
			}	
			
			if (matchpos >= pos)
				matchpos -= dicsiz;
			int t1 = pos + matchlen;
			int t2 = matchpos + matchlen;
			
			while (matchlen < j) 
			{
				if (text[t1] != text[t2]) 
				{
					split(r);
					return;
				}
				matchlen++;
				t1++;
				t2++;
			}
			
			if (matchlen == maxmatch)
				break;
			position[r] = (short)pos;
			q = r;
			
			if ((r = child(q, textbuf(t1))) == NIL) 
			{
				makechild(q, textbuf(t1), pos);
				return;
			}
			matchlen++;
		}
		
		t = prev[r];
		prev[pos] = t;
		next[t] = pos;
		t = next[r];
		next[pos] = t;
		prev[t] = pos;
		parent[pos] = q;
		parent[r] = NIL;
		next[r] = pos;  /* special use of next[] */
	}
  
	private final  void delete_node()
	{
		int q, r, s, t, u;
      
		if (parent[pos] == NIL)	return;
		r = prev[pos];
		s = next[pos];
		next[r] = s;
		prev[s] = r;
		r = parent[pos];
		parent[pos] = NIL;
		if (r >= dicsiz || --childcount[r] > 1) return;
		t = position[r] & SHRT_MAX;
		if (t >= pos) t -= dicsiz;
		s = t;
		q = parent[r];
		
		while ((u = position[q]) < 0) 
		{
			u &= SHRT_MAX;
			if (u >= pos) u -= dicsiz;
			if (u > s) s = u;
			position[q] = (short)(s | dicsiz);
			q = parent[q];
		}
		
		if (q < dicsiz) 
		{
			if (u >= pos) u -= dicsiz;
			if (u > s) s = u;
			position[q] = (short)((s | dicsiz) | SHRT_MIN);
		}
      
		s = child(r, textbuf(t + level[r]));
		t = prev[s];
		u = next[s];
		next[t] = u;
		prev[u] = t;
		t = prev[r];
		next[t] = s;
		prev[s] = t;
		t = next[r];
		prev[t] = s;
		next[s] = t;
		parent[s] = parent[r];
		parent[r] = NIL;
		next[r] = avail;
		avail = r;
	}
  
	private final  void get_next_match() throws IOException
	{
		int n;
		remainder--;
		if (++pos == dicsiz * 2) 
		{
		//        PrintLn.println("get_next_match");
	
			System.arraycopy(text, dicsiz, text, 0, dicsiz + maxmatch);
			n = fread_crc(text, dicsiz + maxmatch, dicsiz, in);
			encoded_origsize += n;
			remainder += n;
			pos = dicsiz;
			if (n == 0) return ;
		}
		delete_node();
		insert_node();
	}
  
	final  void encode(InputStream in, OutputStream out) throws IOException
	{
		this.in = in;
		this.out = out;
      
		int lastmatchlen, dicsiz1;
		int lastmatchpos;

		compsize = count = 0;
		crc = 0; unpackable = false;
		init_slide();
		encode_start();
		dicsiz1 = dicsiz - 1;
		pos = dicsiz + maxmatch;
		memset(text, pos, ' ', dicsiz);
		remainder = fread_crc(text, pos, dicsiz, in);
		encoded_origsize = remainder;
		matchlen = 0;
		insert_node();
//		      PrintLn.println("Lhhuf: remainder="+remainder + unpackable);
		while (remainder > 0 && !unpackable) 
		{
			lastmatchlen = matchlen;
			lastmatchpos = matchpos;
			get_next_match();
//				PrintLn.println("Lhhuf: here");
	
			int x, y;
			if (matchlen > remainder)
				matchlen = remainder;
			if (matchlen > lastmatchlen || lastmatchlen < THRESHOLD) 
			{
				encode_output(x = textbuf(pos - 1), y = 0);
//				          PrintLn.println("Lhhuf: -x=" + x + "y=" + y);
				count++;
			}
			else 
			{
				//        encode_output
				output_st1(x = lastmatchlen + (UCHAR_MAX + 1 - THRESHOLD),
				y = (pos - lastmatchpos - 2) & dicsiz1);
//				          PrintLn.println("Lhhuf: +x=" + x + "y=" + y);
	  
				while (--lastmatchlen > 0) 
				{
               if (lastmatchlen == 30)
                  lastmatchlen = lastmatchlen ;
					get_next_match();
					count++;
				}
				if (matchlen > remainder)
					matchlen = remainder;
			}
//				PrintLn.println("Lhhuf: remainder=" + remainder);
            if (remainder == 479)
               remainder = remainder ;
		}
		encode_end();
		//      in.close();
		//      out.close();
      if (fw != null) fw.updateProgress(bytes-lastbytes) ;
	}
  
  
	private final void encode_start() throws IOException
	{ encode_start_st1(); }
	
	private final void encode_output(int x, int y) throws IOException 
	{ output_st1(x, y); }
	
	private final void encode_end() throws IOException 
	{ encode_end_st1(); }
  
  
	private static final int NP =	(MAX_DICBIT + 1);
	private static final int NT =	(USHRT_BIT + 3);
	private static final int PBIT	= 5;	/* smallest integer such that (1 << PBIT) > * NP */
	private static final int TBIT =5;		/* smallest integer such that (1 << TBIT) > * NT */
	private static final int NC =	(UCHAR_MAX + MAXMATCH + 2 - THRESHOLD);
  
	private int n, heapsize, heap[] = new int[NC + 1];
	private int freq[]; int sorta[]; int sort;
	private int len[];
	private int len_cnt[] = new int[17];
  
	private final void make_code(int n, int len[], int code[])
	{
		int  weight[] = new int[17];	/* 0x10000ul >> bitlen */
		int  start[] = new int[17];	/* start code */
		int  j, k;
		int             i;
		//        PrintLn.println("make_code(" + n + ")");
      
      j = 0;
      k = 1 << (16 - 1);
      for (i = 1; i <= 16; i++) 
		{
			start[i] = j;
			j += (weight[i] = k) * len_cnt[i];
			k >>>= 1;
      }
      for (i = 0; i < n; i++) 
		{
			j = len[i];
			code[i] = start[j];
			start[j] += weight[j];
      }
	}
  
	private int depth = 0;
  
	private final void count_len(int i)			/* call with i = root */
	{
		//        PrintLn.println("count_len("+i+")");
		if (i < n)
			len_cnt[depth < 16 ? depth : 16]++;
		else 
		{
			depth++;
			count_len(left[i]);
			count_len(right[i]);
			depth--;
		}
	}
  
	private final void make_len(int root)
	{
		int i, k;
		int cum;
		//        PrintLn.println("make_len(" + root + ")");
      
		for (i = 0; i <= 16; i++)
			len_cnt[i] = 0;
		count_len(root);
		cum = 0;
		for (i = 16; i > 0; i--) 
		{
			cum += len_cnt[i] << (16 - i);
		}
      
		cum &= 0xffff;
      
		/* adjust len */
		if (cum != 0) 
		{
			len_cnt[16] -= cum;	/* always len_cnt[16] > cum */
			do 
			{
				for (i = 15; i > 0; i--) 
				{
					if (len_cnt[i] != 0) 
					{
						len_cnt[i]--;
						len_cnt[i + 1] += 2;
						break;
					}
				}
			} while (--cum != 0);
		}
		
		/* make len */
      for (i = 16; i > 0; i--) 
		{
			k = len_cnt[i];
			while (k > 0) 
			{
				len[sorta[sort++]] = i;
				k--;
			}
		}
	}
  
	private final void downheap(int i)  /* priority queue; send i-th entry down heap */
	{
		int j, k;
      
		k = heap[i];
		//      PrintLn.println("downheap(" + i + ")->"+k);
      
		while ((j = 2 * i) <= heapsize) 
		{
			if (j < heapsize && freq[heap[j]] > freq[heap[j + 1]])
				j++;
			if (freq[k] <= freq[heap[j]])
				break;
			heap[i] = heap[j];
			i = j;
		}
		heap[i] = k;
		//      PrintLn.println("downheap(" + i + ")<-"+k);
	}
  
	private final int make_tree(int nparm, int freqparm[], int lenparm[], int codeparm[])
	/* make tree, calculate len[], return root */
	{
		int  i, j, k, available;
      
		//      PrintLn.println("nparm=" + nparm);
		n = nparm;
		freq = freqparm;
		len = lenparm;
		available = n;
		heapsize = 0;
		heap[1] = 0;
		for (i = 0; i < n; i++) 
		{
			len[i] = 0;
			if (freq[i] != 0)
			heap[++heapsize] = i;
		}
		if (heapsize < 2) 
		{
			codeparm[heap[1]] = 0;
			return heap[1];
		}
		
		for (i = heapsize / 2; i >= 1; i--)
			downheap(i);	/* make priority queue */
		
		sorta = codeparm; sort = 0;
		
		do 
		{			/* while queue has at least two entries */
			i = heap[1];	/* take out least-freq entry */
			if (i < n)
				sorta[sort++] = i;
			heap[1] = heap[heapsize--];
			//        PrintLn.println("heapsize=" + heapsize + "," + heap[heapsize+1]);
			downheap(1);
			//        PrintLn.println("=" + heap[1]);
			j = heap[1];	/* next least-freq entry */
			if (j < n)
				sorta[sort++] = j;
			k = available++;	/* generate new node */
			freq[k] = freq[i] + freq[j];
			heap[1] = k;
			downheap(1);	/* put into queue */
			//        PrintLn.println("k="+k+"i="+i+"j="+j);
			left[k] = i;
			right[k] = j;
		} while (heapsize > 1);
		sort = 0;
		make_len(k);
		make_code(nparm, lenparm, codeparm);
		return k;		/* return root */
	}
  
	private int left[] = new int[2 * NC - 1], right[] = new int[2 * NC - 1];
  
	private final void make_table(int nchar, int bitlen[], int tablebits, int table[])
		throws IOException
	{
		int count[] = new int[17];	/* count of bitlen */
		int weight[] = new int[17];	/* 0x10000ul >> bitlen */
		int start[] = new int[17];	/* first code of bitlen */
		int total;
		int i;
		int j, k, l, m, n, available;
	
		available = nchar;

      try
      {
   		/* initialize */
   		for (i = 1; i <= 16; i++)
   		{
   			count[i] = 0;
   			weight[i] = 1 << (16 - i);
   		}
	
   		/* count */
   		for (i = 0; i < nchar; i++)
   			count[bitlen[i]]++;
	
   		/* calculate first code */
   		total = 0;
   		for (i = 1; i <= 16; i++)
   		{
   			start[i] = total;
   			total += weight[i] * count[i];
   		}
   		if ((total & 0xffff) != 0)
   			throw new IOException("lhhuf: bad huffman table");
	
   		/* shift data for make table. */
   		m = 16 - tablebits;
   		for (i = 1; i <= tablebits; i++)
   		{
   			start[i] >>>= m;
   			weight[i] >>>= m;
   		}
	
   		/* initialize */
   		j = start[tablebits + 1] >>> m;
   		k = 1 << tablebits;
   		if (j != 0)
   			for (i = j; i < k; i++)
   				table[i] = 0;
	
   		/* create table and tree */
   		for (j = 0; j < nchar; j++)
   		{
   			k = bitlen[j];
   			if (k == 0)
   				continue;
   			l = start[k] + weight[k];
   			if (k <= tablebits)
   			{
   				/* code in table */
   				for (i = start[k]; i < l; i++)
   					table[i] = j;
   			}
   			else
   			{
   				/* code not in table */
   				int tbl[] = table;
   				int p = ((i = start[k]) >>> m);
   				i <<= tablebits;
   				n = k - tablebits;
   				/* make tree (n length) */
   				while (--n >= 0)
   				{
   					if (tbl[p] == 0)
   					{
   						right[available] = left[available] = 0;
   						tbl[p] = available++;
   					}
   					if ((i & 0x8000) != 0)
   					{
   						p = tbl[p];
   						tbl = right;
   					}
   					else
   					{
   						p = tbl[p];
   						tbl = left;
   					}
   					i <<= 1;
   				}
   				tbl[p] = j;
   			}
   			start[k] = l;
   		}
      }

      // Exceptions indicate a bad Huffman code.

      catch (ArrayIndexOutOfBoundsException e)
      {
         throw new IOException("lhhuf: bad huffman code") ;
      }
	}
  
  
	private static final int NPT = 0x80;
  
	private int   c_len[] = new int[NC], pt_len[] = new int[NPT];
	private int c_freq[] = new int[2 * NC - 1], c_table[] = new int[4096], c_code[] = new int[NC], p_freq[] = new int[2 * NP - 1],
		pt_table[] = new int[256], pt_code[] = new int[NPT], t_freq[] = new int[2 * NT - 1];
  
	private int buffer(int idx) { return buf[idx] & 0xff; }
	private byte[] buf;
	private int bufsiz;
	private int blocksize;
	private int output_pos, output_mask;
	private int	pbit;
	private int	np;
  
  /* encode */
  
	private final void count_t_freq(/*void*/)
	{
		int  i, k, n, count;
      
		//      PrintLn.println("count_t_freq>");
      
		for (i = 0; i < NT; i++)
			t_freq[i] = 0;
		n = NC;
		while (n > 0 && c_len[n - 1] == 0)
			n--;
		i = 0;
		while (i < n) 
		{
			k = c_len[i++];
			if (k == 0) 
			{
				count = 1;
				while (i < n && c_len[i] == 0) 
				{
					i++;
					count++;
				}
				if (count <= 2)
					t_freq[0] += count;
				else if (count <= 18)
					t_freq[1]++;
				else if (count == 19) 
				{
					t_freq[0]++;
					t_freq[1]++;
				}
				else
					t_freq[2]++;
			}
			else
				t_freq[k + 2]++;
		}
		//      PrintLn.println("count_t_freq<");
	}
  
	private final void write_pt_len(int n, int nbit, int i_special) throws IOException
	{
		//      PrintLn.println("write_pt_len("+n+","+nbit+","+i_special+")");
      
		int i, k;
      
		while (n > 0 && pt_len[n - 1] == 0)
			n--;
		putbits(nbit, n);
		i = 0;
		while (i < n) 
		{
			k = pt_len[i++];
			if (k <= 6)
				putbits(3, k);
			else
				putbits(k - 3, USHRT_MAX << 1);
			if (i == i_special) 
			{
				while (i < 6 && pt_len[i] == 0)
					i++;
				putbits(2, i - 3);
			}
		}
      
		//      PrintLn.println("write_pt_len<");
	}
  
	private final void write_c_len(/*void*/) throws IOException
	{
		//      PrintLn.println("write_c_len>");
      
		int i, k, n, count;
      
		n = NC;
		while (n > 0 && c_len[n - 1] == 0)
			n--;
		putbits(CBIT, n);
		i = 0;
		while (i < n) 
		{
			k = c_len[i++];
			if (k == 0) 
			{
				count = 1;
				while (i < n && c_len[i] == 0) 
				{
					i++;
					count++;
				}
				if (count <= 2) 
				{
					for (k = 0; k < count; k++)
						putcode(pt_len[0], pt_code[0]);
				}
				else if (count <= 18) 
				{
					putcode(pt_len[1], pt_code[1]);
					putbits(4, count - 3);
				}
				else if (count == 19) 
				{
					putcode(pt_len[0], pt_code[0]);
					putcode(pt_len[1], pt_code[1]);
					putbits(4, 15);
				}
				else 
				{
					putcode(pt_len[2], pt_code[2]);
					putbits(CBIT, count - 20);
				}
			}
			else 
			{
				putcode(pt_len[k + 2], pt_code[k + 2]);
			}
		}
		//      PrintLn.println("write_c_len<");
	}
  
	private final void encode_c(int c) throws IOException
	{
		//      PrintLn.println("encode_c>");
		putcode(c_len[c], c_code[c]);
		//      PrintLn.println("encode_c<");
	}
  
	private final void encode_p(int p) throws IOException
	{
		//      PrintLn.println("encode_p>" + p);
		int c, q;
      
		c = 0;
		q = p;
		while (q != 0) 
		{
			q >>>= 1;
			c++;
		}
		putcode(pt_len[c], pt_code[c]);
		if (c > 1)
			putbits(c - 1, p);
      
		//      PrintLn.println("encode_p<");
	}
  
	private final void send_block() throws IOException
	{
		//      PrintLn.println("send_block>");
      
		int flags = 0;
		int i, k, root, pos, size;
      
		root = make_tree(NC, c_freq, c_len, c_code);
		//      PrintLn.println("root="+root);
      
		size = c_freq[root];
		putbits(16, size);
		if (root >= NC) 
		{
			count_t_freq();
			root = make_tree(NT, t_freq, pt_len, pt_code);
			if (root >= NT) 
			{
				write_pt_len(NT, TBIT, 3);
			}
			else 
			{
				putbits(TBIT, 0);
				putbits(TBIT, root);
			}
			write_c_len();
		}
		else 
		{
			putbits(TBIT, 0);
			putbits(TBIT, 0);
			putbits(CBIT, 0);
			putbits(CBIT, root);
		}
		root = make_tree(NP, p_freq, pt_len, pt_code);
		if (root >= NP) 
		{
			write_pt_len(NP, pbit, -1);
		}
		else 
		{
			putbits(pbit, 0);
			putbits(pbit, root);
		}
		pos = 0;
		for (i = 0; i < size; i++) 
		{
			if (i % CHAR_BIT == 0)
				flags = buffer(pos++);
			else
				flags <<= 1;
			flags &= 0xff;
			if ((flags & (1 << (CHAR_BIT - 1))) != 0) 
			{
				encode_c(buffer(pos++) + (1 << CHAR_BIT));
				k = buffer(pos++) << CHAR_BIT;
				k += buffer(pos++);
				encode_p(k);
			}
			else
				encode_c(buffer(pos++));
			if (unpackable)
				return;
		}
		for (i = 0; i < NC; i++)
			c_freq[i] = 0;
		for (i = 0; i < NP; i++)
			p_freq[i] = 0;
		//      PrintLn.println("send_block<");
	}

	private int cpos;

	private final void output_st1(int c, int p)  throws IOException
	{
		output_mask >>>= 1;
		if (output_mask == 0) 
		{
			output_mask = 1 << (CHAR_BIT - 1);
			if (output_pos >= bufsiz - 3 * CHAR_BIT) 
			{
				send_block();
				if (unpackable)
				   return;
				output_pos = 0;
			}
			cpos = output_pos++;
			buf[cpos] = 0;
		}
		buf[output_pos++] = (byte) c;
		c_freq[c]++;
		//      PrintLn.println("c_freq[" + c + "]=" + (c_freq[c]-1));

		if (c >= (1 << CHAR_BIT)) 
		{
			buf[cpos] |= output_mask;
			buf[output_pos++] = (byte) (p >>> CHAR_BIT);
			buf[output_pos++] = (byte) p;
			c = 0;
			while (p != 0) 
			{
				p >>>= 1;
				c++;
			}
			p_freq[c]++;
		}
	}


	private final byte [] alloc_buf()
	{
		return new byte[bufsiz = 16 * 1024];	/* 65408U; */
	}

	private final void  encode_start_st1()
	{
		int i;

		if (dicbit <= 13)	/* 13 ... Changed N.Watazaki */
      {
		   pbit = 4;	/* lh4,5 etc. */
         np = 14 ;
      }
      else
      {
		   pbit = 5;	/* lh6,7 */
         if (dicbit == 16)
            np = 17 ;
         else
            np = 16 ;
      }

		for (i = 0; i < NC; i++)
			c_freq[i] = 0;
		for (i = 0; i < NP; i++)
			p_freq[i] = 0;
		output_pos = output_mask = 0;
		init_putbits();

		buf = alloc_buf();
		buf[0] = 0;
	}

	private final void encode_end_st1() throws IOException
	{
		if (!unpackable) 
		{
			send_block();
			putbits(CHAR_BIT - 1, 0);/* flush remaining bits */
		}
	}


	/* decode */
	private final void read_pt_len(int nn, int nbit, int i_special) throws IOException
	{
		int i, c, n;

		n = getbits(nbit);
		if (n == 0) 
		{
			c = getbits(nbit);
			for (i = 0; i < nn; i++)
				pt_len[i] = 0;
			for (i = 0; i < 256; i++)
				pt_table[i] = c;
		}
		else 
		{
			i = 0;
			while (i < n) 
			{
				c = bitbuf >>> (16 - 3);
				if (c == 7) 
				{
					int mask = 1 << (16 - 4);
					while ((mask & bitbuf) != 0) 
					{
						mask >>>= 1;
						c++;
					}
				}
				fillbuf((c < 7) ? 3 : c - 3);
				//	    PrintLn.println(pt_len.length + " " + i);
				pt_len[i++] = c;
				if (i == i_special) 
				{
					c = getbits(2);
					while (--c >= 0)
						pt_len[i++] = 0;
				}
			}
			while (i < nn)
				pt_len[i++] = 0;
			make_table(nn, pt_len, 8, pt_table);
		}
	}

	private final void read_c_len() throws IOException
	{
		int i, c, n;

		n = getbits(CBIT);
		if (n == 0) 
		{
			c = getbits(CBIT);
			for (i = 0; i < NC; i++)
				c_len[i] = 0;
			for (i = 0; i < 4096; i++)
				c_table[i] = c;
		}
		else 
		{
			i = 0;
			while (i < n) 
			{
				c = pt_table[bitbuf >>> (16 - 8)];
				if (c >= NT) 
				{
					int  mask = 1 << (16 - 9);
					do 
					{
						if ((bitbuf & mask)!=0)
							c = right[c];
						else
							c = left[c];
						mask >>>= 1;
					} while (c >= NT);
				}
				fillbuf(pt_len[c]);
				if (c <= 2) 
				{
					if (c == 0)
						c = 1;
					else if (c == 1)
						c = getbits(4) + 3;
					else
						c = getbits(CBIT) + 20;
					while (--c >= 0)
						c_len[i++] = 0;
				}
				else
					c_len[i++] = c - 2;
			}
			while (i < NC)
				c_len[i++] = 0;
			make_table(NC, c_len, 12, c_table);
		}
	}

	private final int decode_c_st1()  throws IOException
	{
		int j, mask;

		if (blocksize == 0) 
		{
			blocksize = getbits(16);
			read_pt_len(NT, TBIT, 3);
			read_c_len();
			read_pt_len(np, pbit, -1);
		}
		blocksize--;
		j = c_table[bitbuf >>> 4];
		if (j < NC) 
		{
			fillbuf(c_len[j]);
		} 
		else 
		{
			fillbuf(12);
			mask = 1 << (16 - 1);
			do 
			{
				if ((bitbuf & mask)!=0)
					j = right[j];
				else
					j = left[j];
				mask >>>= 1;
			} while (j >= NC);
			fillbuf(c_len[j] - 12);
		}
		return j;
	}


	private final int decode_p_st1() throws IOException
	{
		int  j, mask;

		j = pt_table[bitbuf >>> (16 - 8)];
		if (j < np)
			fillbuf(pt_len[j]);
		else 
		{
			fillbuf(8);
			mask = 1 << (16 - 1);
			do 
			{
				if ((bitbuf & mask)!=0)
					j = right[j];
				else
					j = left[j];
				mask >>>= 1;
			} while (j >= np);
			fillbuf(pt_len[j] - 8);
		}
		if (j != 0) 
		{
			j = (1 << (j - 1)) + getbits(j - 1);
			j &= 0xffff;
		}
		return j;
	}

	private final void  decode_start_st1() throws IOException
	{
		if (dicbit <= 13) /* 13 ... Changed N.Watazaki */
      {
			np = 14;
			pbit = 4;
      } 
		else 
		{
         if (dicbit == 16)
         {
            np = 17 ;  /* for -lh7- */
         }
         else
         {
			   np = 16;
         }
			pbit = 5;
		}
		init_getbits();
		blocksize = 0;
	}

	private int subbitbuf, bitcount;

	/* Shift bitbuf n bits left, read n bits */
	private final void fillbuf(int n) throws IOException
	{
		while (n > bitcount) 
		{
			n -= bitcount;
			bitbuf = (bitbuf << bitcount) + (subbitbuf >>> (CHAR_BIT - bitcount));
			if (compsize != 0)
				compsize--;
			int ch = in.read();
			if (ch < 0) { eof = true; ch = 0; }
         int progress = ++bytes - lastbytes ;
         if (fw != null && progress > 1024) 
         {
             fw.updateProgress(progress) ;
             lastbytes = bytes ;
         }
			//	    PrintLn.println(ch);
			subbitbuf = ch;
	//	  }
	//	  else {
	//	    subbitbuf = 0;
	//	  }
			bitcount = CHAR_BIT;
		}
		bitcount -= n;
		bitbuf = (bitbuf << n) + (subbitbuf >>> (CHAR_BIT - n));
		bitbuf &= 0xffff;
		subbitbuf <<= n;
		subbitbuf &= 0xff;
		//	PrintLn.println(bitbuf);
	}


	private final int getbits(int n)  throws IOException
	{
		int x;

		x = bitbuf >>> (2 * CHAR_BIT - n);
		//      x &= 0xffff;
		fillbuf(n);
		//      PrintLn.println("<-getbits " + x);
		return x;
	}

	/* Write rightmost n bits of x */
	private final void putcode(int n, int x) throws IOException	
	{
		//      PrintLn.println("putcode(" + n + "," + x + ")");
		//      PrintLn.println(bitcount);

		while (n >= bitcount) 
		{
			n -= bitcount;
			subbitbuf += (x >>> (USHRT_BIT - bitcount));
			x <<= bitcount;
			x &= 0xffff;

			//	if (compsize < origsize) {
			//          PrintLn.println("putcode(" + n + "," + x + ")->" + (subbitbuf&0xff));
			out.write(subbitbuf);
			compsize++;
			//	}
			//	else
			//	  unpackable = true;
			subbitbuf = 0;
			bitcount = CHAR_BIT;
		}
		subbitbuf += (x >>> (USHRT_BIT - bitcount));
		bitcount -= n;
	}

	/* Write rightmost n bits of x */
	private final void putbits(int n, int x) throws IOException
	{
		//      PrintLn.println("putbits(" + n + "," + x + ") "+bitcount);
		x <<= USHRT_BIT - n;
		x &= 0xffff;
		while (n >= bitcount) 
		{
			n -= bitcount;
			subbitbuf += (x >>> (USHRT_BIT - bitcount));
			x <<= bitcount;
			x &= 0xffff;
			//	if (compsize < origsize) {
			out.write(subbitbuf);
			//          PrintLn.println("putbits(" + n + "," + x + ")->"+(subbitbuf&0xff));
			compsize++;
			//	}
			//	else
			//	  unpackable = true;
			subbitbuf = 0;
			bitcount = CHAR_BIT;
		}
		subbitbuf += x >>> (USHRT_BIT - bitcount);
		bitcount -= n;
	}

	private int origsize;

	private final void init_getbits() throws IOException
	{
		bitbuf = 0;
		subbitbuf = 0;
		bitcount = 0;
		fillbuf(2 * CHAR_BIT);
	}

	private final void init_putbits()
	{
		bitcount = CHAR_BIT;
		subbitbuf = 0;
	}

	private int bitbuf;
	private OutputStream out;

	private int prev_char ;
	private final void decode_start() throws IOException 
	{
		decode_start_st1();
	}
	
	private final int decode_c()  throws IOException  
	{
		return decode_c_st1();
	}
	
	private final int decode_p() throws IOException  
	{
		return decode_p_st1();
	}
	
	private int loc;
  
	final void decode(InputStream in, OutputStream out) throws IOException
	{
		this.in = in;
		this.out = out;

		int i, j, k, c, dicsiz1, offset;
		//      PrintLn.println("decode");

		crc = 0;
		prev_char = -1;
		dicsiz = 1 << dicbit;
		text = new byte[dicsiz];
		memset(text, 0, ' ', dicsiz);
		decode_start();
		dicsiz1 = dicsiz - 1;
		offset = 0x100 - 3;
		count = 0;
		loc = 0;
//		for (;;) 
		while (count < origsize)
		{
			c = decode_c();
			//        PrintLn.println(loc + "=" + c);

			if (c <= UCHAR_MAX) 
			{
//				if (eof) break;
				text[loc++] = (byte)c;
				if (loc == dicsiz) 
				{
					fwrite_crc(text, 0, dicsiz, out);
					loc = 0;
				}
				count++;
			}
			else 
			{
				j = c - offset;
				i = (loc - decode_p() - 1) & dicsiz1;
				count += j;
				for (k = 0; k < j; k++) 
				{
					c = textbuf((i + k) & dicsiz1);
					text[loc++] = (byte)c;
					if (loc == dicsiz) 
					{
						fwrite_crc(text, 0, dicsiz, out);
						loc = 0;
					}
				}
//				if (eof) break;
			}
		}
		if (loc != 0) 
		{
			fwrite_crc(text, 0, loc, out);
		}
	}
	
	private boolean eof;
   
   void setFileWriter(FileWriter f) { fw = f ; }
}


