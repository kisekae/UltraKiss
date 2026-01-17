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



/* LHarc Encoding/Decoding module */
/* LZSS Algorithm	       Haruhiko.Okumura */
/* Adaptic Huffman Encoding    1989.05.27  Haruyasu.Yoshizaki */
/* V1.00  Fixed	               1989.09.22  Y.Tagawa */
/* Porting to Java             1997.05.11  Todo Software */


import java.io.*;

final class Lzhuf 
{
	private static final int N = 4096;  /* buffer size */
	private static final int F = 60;  /* pre-sence buffer size */
	private static final int THRESHOLD = 2;
	private static final int NIL = N;  /* term of tree */
	private static final int EOF = -1;
	private int  lson[] = new int[N + 1], rson[] = new int[N + 1 + N], dad[] = new int[N + 1];
	private int  same[] = new int[N + 1];
	byte  text_buf[] = new byte[N + F - 1];

   private int bytes = 0 ;         /* bytes processed */
   private int lastbytes = 0 ;     /* last bytes processed */
   private FileWriter fw = null ;  /* FileWriter to track progress */

	private final int text_buf(int idx) 
	{
		return text_buf[idx] & 0xff;
	}

	private static final int N_CHAR = (256 - THRESHOLD + F); /* {code : 0 .. N_CHAR-1} */
	private static final int T = (N_CHAR * 2 - 1);	/* size of table */
	private static final int R = (T - 1);			/* root position */
	private static final int MAX_FREQ =	0x8000;	/* tree update timing from frequency */
	private final int freq[] = new int[T + 1];	/* frequency table */

	private final int prnt[] = new int[T + N_CHAR];	/* points to parent node */
	/* notes :
		prnt[T .. T + N_CHAR - 1] used by
		indicates leaf position that corresponding to code */

	private final int son[] = new int[T];		/* points to son node (son[i],son[i+]) */
	private final int textsize = 0;

	private final void startHuff ()
	{
	   int i, j;
      for (i = 0; i < N_CHAR; i++) 
		{
			freq[i] = 1;
			son[i] = i + T;
			prnt[i + T] = i;
		}
		i = 0; j = N_CHAR;
		while (j <= R) 
		{
			freq[j] = freq[i] + freq[i + 1];
			son[j] = i;
			prnt[i] = prnt[i + 1] = j;
			i += 2; j++;
		}
		freq[T] = 0xffff;
		prnt[R] = 0;
		putlen = getlen = 0;
		putbuf = getbuf = 0;
	}

	private int putlen, getlen;
	private int putbuf, getbuf;

	/* reconstruct tree */
	private final void reconst ()
	{
		int i, j, k;
		int f;

		/* correct leaf node into of first half,
		and set these freqency to (freq+1)/2       */
		j = 0;
		for (i = 0; i < T; i++) 
		{
			if (son[i] >= T) 
			{
				freq[j] = (freq[i] + 1) / 2;
				son[j] = son[i];
				j++;
			}
		}
		/* build tree.  Link sons first */
		for (i = 0, j = N_CHAR; j < T; i += 2, j++) 
		{
			k = i + 1;
			f = freq[j] = freq[i] + freq[k];
			for (k = j - 1; f < freq[k]; k--);
			k++;
			{  
				for (int p = j, e = k; p > e; p--)
					freq[p] = freq[p-1];
				freq[k] = f;
			}
			{ 
				for (int p = j, e = k; p > e; p--)
					son[p] = son[p-1];
				son[k] = i;
			}
		}
		/* link parents */
		for (i = 0; i < T; i++) 
		{
			if ((k = son[i]) >= T) 
			{
				prnt[k] = i;
			} 
			else 
			{
				prnt[k] = prnt[k + 1] = i;
			}
		}
	}


	/* update given code's frequency, and update tree */

	private final void update (int c)
	{
		int p, i, j, k, l;

		if (freq[R] == MAX_FREQ) 
		{
			reconst();
		}
		c = prnt[c + T];
		do 
		{
			k = ++freq[c];

			/* swap nodes when become wrong frequency order. */
			if (k > freq[l = c + 1]) 
			{
				for (p = l+1; k > freq[p++]; ) ;
				l = p - 2;
				freq[c] = freq[p-2];
				freq[p-2] = k;

				i = son[c];
				prnt[i] = l;
				if (i < T) prnt[i + 1] = l;

				j = son[l];
				son[l] = i;

				prnt[j] = c;
				if (j < T) prnt[j + 1] = c;
				son[c] = j;

				c = l;
			}
		} while ((c = prnt[c]) != 0);  /* loop until reach to root */
	}

	private InputStream in;
	private OutputStream out;

	private int	match_position, match_length;

	private final void insertNode (int r)
	{
		int p;
		int cmp;
		int key;
		int c;
		int i, j;

		//	PrintLn.println("INSERTNODE=" + r);
		cmp = 1;
		key = r;
		i = (text_buf(key+1)) ^ (text_buf(key+2));
		i ^= (i >>> 4);
		p = N + 1 + (text_buf(key)) + ((i & 0x0f) << 8);
		rson[r] = lson[r] = NIL;
		match_length = 0;
		i = j = 1;
		for ( ; ; ) 
		{
			if (cmp >= 0) 
			{
				if (rson[p] != NIL) 
				{
					p = rson[p];
					j = same[p];
				} 
				else 
				{
					rson[p] = r;
					dad[r] = p;
					same[r] = i;
					return;
				}
			} 
			else 
			{
				if (lson[p] != NIL) 
				{
					p = lson[p];
					j = same[p];
				} 
				else 
				{
					lson[p] = r;
					dad[r] = p;
					same[r] = i;
					return;
				}
			}

			if (i > j) 
			{
				i = j;
				cmp = text_buf(key+i) - text_buf(p + i);
			} 
			else 
			{
				if (i == j) 
				{
					for (; i < F; i++)
						if ((cmp = text_buf(key+i) - text_buf(p + i)) != 0)
							break;
				}
			}

			if (i > THRESHOLD) 
			{
				if (i > match_length) 
				{
					match_position = ((r - p) & (N - 1)) - 1;
					if ((match_length = i) >= F)
						break;
				} 
				else 
				{
					if (i == match_length) 
					{
						if ((c = ((r - p) & (N - 1)) - 1) < match_position) 
						{
							match_position = c;
						}
					}
				}
			}
		}
		same[r] = same[p];
		dad[r] = dad[p];
		lson[r] = lson[p];
		rson[r] = rson[p];
		dad[lson[p]] = r;
		dad[rson[p]] = r;
		if (rson[dad[p]] == p)
			rson[dad[p]] = r;
		else
			lson[dad[p]] = r;
		dad[p] = NIL;  /* remove p */
	}


	private final void link (int n, int p, int q)
	{
		if (p >= NIL) 
		{
			same[q] = 1;
			return;
		}
		int s1, s2, s3;   
		s1 = p + n;
		s2 = q + n;
		s3 = p + F;
		while (s1 < s3) 
		{
			if (text_buf(s1++) != text_buf(s2++)) 
			{
				same[q] = s1 - 1 - p;
				return;
			}
		}
		same[q] = F;
	}


	private final void linknode (int p, int q, int r)
	{
		int cmp;
		if ((cmp = same[q] - same[r]) == 0) 
		{
			link(same[q], p, r);
		} 
		else if (cmp < 0) 
		{
			same[r] = same[q];
		}
	}

	private final void deleteNode (int p)
	{
		int  q;

		//	PrintLn.println("DELETENODE=" + p);

		if (dad[p] == NIL)
			return;      /* has no linked */
		if (rson[p] == NIL) 
		{
			if ((q = lson[p]) != NIL)
				linknode(dad[p], p, q);
		} 
		else if (lson[p] == NIL) 
		{
			q = rson[p];
			linknode(dad[p], p, q);
		} 
		else 
		{
			q = lson[p];
			if (rson[q] != NIL) 
			{
				do 
				{
					q = rson[q];
				} while (rson[q] != NIL);
				if (lson[q] != NIL)
					linknode(dad[q], q, lson[q]);
				link(1, q, lson[p]);
				rson[dad[q]] = lson[q];
				dad[lson[q]] = dad[q];
				lson[q] = lson[p];
				dad[lson[p]] = q;
			}
			link(1, dad[p], q);
			link(1, q, rson[p]);
			rson[q] = rson[p];
			dad[rson[p]] = q;
		}
		dad[q] = dad[p];
		if (rson[dad[p]] == p)
			rson[dad[p]] = q;
		else
			lson[dad[p]] = q;
		dad[p] = NIL;
	}

	/* TABLE OF ENCODE/DECODE for upper 6bits position information */

	/* for encode */
	private final static byte p_len[] = {
    0x03, 0x04, 0x04, 0x04, 0x05, 0x05, 0x05, 0x05,
    0x05, 0x05, 0x05, 0x05, 0x06, 0x06, 0x06, 0x06,
    0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
    0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07,
    0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07,
    0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07,
    0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
    0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08
	};

	private final static int p_code[] = {
    0x00, 0x20, 0x30, 0x40, 0x50, 0x58, 0x60, 0x68,
    0x70, 0x78, 0x80, 0x88, 0x90, 0x94, 0x98, 0x9C,
    0xA0, 0xA4, 0xA8, 0xAC, 0xB0, 0xB4, 0xB8, 0xBC,
    0xC0, 0xC2, 0xC4, 0xC6, 0xC8, 0xCA, 0xCC, 0xCE,
    0xD0, 0xD2, 0xD4, 0xD6, 0xD8, 0xDA, 0xDC, 0xDE,
    0xE0, 0xE2, 0xE4, 0xE6, 0xE8, 0xEA, 0xEC, 0xEE,
    0xF0, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7,
    0xF8, 0xF9, 0xFA, 0xFB, 0xFC, 0xFD, 0xFE, 0xFF
	};

	private int codesize = 0;

	/* output C bits */
	private final void putcode (int l, int c) throws IOException
	{
		int len = putlen;
		int b = putbuf;
		b |= c >>> len;
		if ((len += l) >= 8) 
		{
			out.write (b >>> 8);
			// PrintLn.println(l + "," + c + "=" + ((b>>8)&0xff) + "-");
			if ((len -= 8) >= 8) 
			{
				out.write (b);
				//	  PrintLn.println("+" + (b&0xff) + "-");
				codesize += 2;
				len -= 8;
				b = c << (l - len);
			} 
			else 
			{
				b <<= 8;
				codesize++;
			}
		}
		putbuf = b;
		putlen = len;
	}



	/* static unsigned code, len; */

	private final void encodeChar (int c)  throws IOException
	{
		long i;
		int j, k;

		i = 0;
		j = 0;
		k = prnt[c + T];

		//      PrintLn.println("k="+k);
		/* trace links from leaf node to root */
		do 
		{
			i >>= 1;

			/* if node index is odd, trace larger of sons */
			if ((k & 1) != 0) i += 0x80000000;

			j++;
		} while ((k = prnt[k]) != R) ;
		i = -i;
		//      PrintLn.println("i="+(int)i+"k="+k);
		if (j > 16) 
		{
			putcode(16, (int)(i >>> 16));
			putcode(j - 16, (int)i);
		} 
		else 
		{
			putcode(j, (int)(i >>> 16));
		}
		update(c);
	}

	private final void encodePosition (int c)  throws IOException
	{
		/* output upper 6bit from table */
		int i = c >>> 6;
		putcode((p_len[i]), (p_code[i]) << 8);

      /* output lower 6 bit */
      putcode(6, (c & 0x3f) << 10);
    }

	private final void encodeEnd ()   throws IOException
	{
		//      PrintLn.println(putlen);
		if (putlen > 0) 
		{
			out.write(putbuf >>> 8);
			codesize++;
		}
	}

	private final void initTree () 
	{
		for (int p = N + 1, e = N + N; p <= e; )
			rson[p++] = NIL;
		for (int p = 0, e = N; p < e; )
			dad[p++] = NIL;
	}

	final void encode (InputStream in, OutputStream out) throws IOException
	{
		this.in = in;
		this.out = out;

		int  i, c, len, r, s, last_match_length;

		startHuff();
		initTree();
		s = 0;
		r = N - F;
		for (i = s; i < r; i++)
			text_buf[i] = (byte) ' ';
		for (len = 0; len < F && (c = in.read()) != EOF; len++)
			text_buf[r + len] = (byte)c;
      bytes += len ;
      int progress = bytes - lastbytes ;
      if (fw != null && progress > 1024) 
      {
          fw.updateProgress(bytes-lastbytes) ;
          lastbytes = bytes ;
      }
		for (i = 1; i <= F; i++)
			insertNode(r - i);
		insertNode(r);
		do 
		{
			if (match_length > len)
				match_length = len;
			if (match_length <= THRESHOLD) 
			{
				match_length = 1;
				encodeChar(text_buf(r));
			} 
			else 
			{
				encodeChar(255 - THRESHOLD + match_length);
				encodePosition(match_position);
			}
			last_match_length = match_length;
			for (i = 0; i < last_match_length &&
				(c = in.read()) != EOF; i++) 
			{
            progress = ++bytes - lastbytes ;
            if (fw != null && progress > 1024) 
            {
                fw.updateProgress(progress) ;
                lastbytes = bytes ;
            }
				deleteNode(s);
				text_buf[s] = (byte)c;
				if (s < F - 1)
					text_buf[s + N] = (byte)c;
				s = (s + 1) & (N - 1);
				r = (r + 1) & (N - 1);
				insertNode(r);
			}
			//        textsize += i;
			while (i++ < last_match_length) 
			{
				deleteNode(s);
				s = (s + 1) & (N - 1);
				r = (r + 1) & (N - 1);
				if (--len != 0) insertNode(r);
			}
		} while (len > 0);
		encodeEnd();
		//      in.close();
		//      out.close();
      if (fw != null) fw.updateProgress(bytes-lastbytes) ;
	}

	/* for decode */
	private final static byte d_code[] = {
    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
    0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
    0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
    0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
    0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
    0x03, 0x03, 0x03, 0x03, 0x03, 0x03, 0x03, 0x03,
    0x03, 0x03, 0x03, 0x03, 0x03, 0x03, 0x03, 0x03,
    0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04,
    0x05, 0x05, 0x05, 0x05, 0x05, 0x05, 0x05, 0x05,
    0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
    0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07,
    0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
    0x09, 0x09, 0x09, 0x09, 0x09, 0x09, 0x09, 0x09,
    0x0A, 0x0A, 0x0A, 0x0A, 0x0A, 0x0A, 0x0A, 0x0A,
    0x0B, 0x0B, 0x0B, 0x0B, 0x0B, 0x0B, 0x0B, 0x0B,
    0x0C, 0x0C, 0x0C, 0x0C, 0x0D, 0x0D, 0x0D, 0x0D,
    0x0E, 0x0E, 0x0E, 0x0E, 0x0F, 0x0F, 0x0F, 0x0F,
    0x10, 0x10, 0x10, 0x10, 0x11, 0x11, 0x11, 0x11,
    0x12, 0x12, 0x12, 0x12, 0x13, 0x13, 0x13, 0x13,
    0x14, 0x14, 0x14, 0x14, 0x15, 0x15, 0x15, 0x15,
    0x16, 0x16, 0x16, 0x16, 0x17, 0x17, 0x17, 0x17,
    0x18, 0x18, 0x19, 0x19, 0x1A, 0x1A, 0x1B, 0x1B,
    0x1C, 0x1C, 0x1D, 0x1D, 0x1E, 0x1E, 0x1F, 0x1F,
    0x20, 0x20, 0x21, 0x21, 0x22, 0x22, 0x23, 0x23,
    0x24, 0x24, 0x25, 0x25, 0x26, 0x26, 0x27, 0x27,
    0x28, 0x28, 0x29, 0x29, 0x2A, 0x2A, 0x2B, 0x2B,
    0x2C, 0x2C, 0x2D, 0x2D, 0x2E, 0x2E, 0x2F, 0x2F,
    0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37,
    0x38, 0x39, 0x3A, 0x3B, 0x3C, 0x3D, 0x3E, 0x3F,
	};

	private final static byte d_len[] = {
    0x03, 0x03, 0x03, 0x03, 0x03, 0x03, 0x03, 0x03,
    0x03, 0x03, 0x03, 0x03, 0x03, 0x03, 0x03, 0x03,
    0x03, 0x03, 0x03, 0x03, 0x03, 0x03, 0x03, 0x03,
    0x03, 0x03, 0x03, 0x03, 0x03, 0x03, 0x03, 0x03,
    0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04,
    0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04,
    0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04,
    0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04,
    0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04,
    0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04,
    0x05, 0x05, 0x05, 0x05, 0x05, 0x05, 0x05, 0x05,
    0x05, 0x05, 0x05, 0x05, 0x05, 0x05, 0x05, 0x05,
    0x05, 0x05, 0x05, 0x05, 0x05, 0x05, 0x05, 0x05,
    0x05, 0x05, 0x05, 0x05, 0x05, 0x05, 0x05, 0x05,
    0x05, 0x05, 0x05, 0x05, 0x05, 0x05, 0x05, 0x05,
    0x05, 0x05, 0x05, 0x05, 0x05, 0x05, 0x05, 0x05,
    0x05, 0x05, 0x05, 0x05, 0x05, 0x05, 0x05, 0x05,
    0x05, 0x05, 0x05, 0x05, 0x05, 0x05, 0x05, 0x05,
    0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
    0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
    0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
    0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
    0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
    0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
    0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07,
    0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07,
    0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07,
    0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07,
    0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07,
    0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07,
    0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
    0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
	};


	/* get one bit */
	/* returning in Bit 0 */
	private final int getBit () throws IOException
	{
		int dx = getbuf;
		int c;

		if (getlen <= 8)
		{
			c = in.read();
         int progress = ++bytes - lastbytes ;
         if (fw != null && progress > 1024) 
         {
             fw.updateProgress(progress) ;
             lastbytes = bytes ;
         }
			if ((int)c < 0) { c = 0; eof = true; }
			dx |= c << (8 - getlen);
			getlen += 8;
		}
		getbuf = dx << 1;
		getlen--;
		return (dx & 0x8000) != 0 ? 1 : 0;
	}

	/* get one byte */
	/* returning in Bit7...0 */
	private final int getByte () throws IOException
	{
		int dx = getbuf;
		int c;

		if (getlen <= 8) 
		{
			c = in.read();
         int progress = ++bytes - lastbytes ;
         if (fw != null && progress > 1024) 
         {
             fw.updateProgress(progress) ;
             lastbytes = bytes ;
         }
			if ((int)c < 0) { c = 0; eof = true; }
			dx |= c << (8 - getlen);
			getlen += 8;
		}
		getbuf = dx << 8;
		getlen -= 8;
		return (dx >>> 8) & 0xff;
	}

	/* get N bit */
	/* returning in Bit(N-1)...Bit 0 */
	private final static int mask[] = {
    0x0000,
    0x0001, 0x0003, 0x0007, 0x000f,
    0x001f, 0x003f, 0x007f, 0x00ff,
    0x01ff, 0x03ff, 0x07ff, 0x0fff,
    0x1fff, 0x3fff, 0x0fff, 0xffff };
	
	private final static int shift[] = {
    16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 };
	
	private final int getNBits (int n)  throws IOException
	{
		int dx = getbuf;
		int c;
		if (getlen <= 8)
		{
			c = in.read();
         int progress = ++bytes - lastbytes ;
         if (fw != null && progress > 1024) 
         {
             fw.updateProgress(progress) ;
             lastbytes = bytes ;
         }
			if ((int)c < 0) { c = 0; eof = true; }
			dx |= c << (8 - getlen);
			getlen += 8;
		}
		getbuf = dx << n;
		getlen -= n;
		return (dx >>> shift[n]) & mask[n];
	}
	
	private final int decodeChar ()  throws IOException
	{
		int c;

		c = son[R];

		/* trace from root to leaf,
		got bit is 0 to small(son[]), 1 to large (son[]+1) son node */
		while (c < T) 
		{
			c += getBit();
			c = son[c];
		}
		c -= T;
		update(c);
		return c;
	}
	
	private final int decodePosition ()  throws IOException
	{
		int i, j, c;

		/* decode upper 6bit from table */
		i = getByte();
		c = d_code[i] << 6;
		j = d_len[i];

		/* get lower 6bit */
		j -= 2;
		return c | (((i << j) | getNBits (j)) & 0x3f);
	}
	
	private boolean eof;
	
	final void decode (InputStream in, OutputStream out) throws IOException
	{
		this.in = in;
		this.out = out;

		int  i, j, k, r, c, count;

		startHuff();
		for (i = 0; i < N - F; i++)
			text_buf[i] = (byte) ' ';
		r = N - F;
		for (count = 0;; ) 
		{
			c = decodeChar();
			if (eof) break;
			if (c < 256) 
			{
				out.write (c);
				text_buf[r++] = (byte)c;
				r &= (N - 1);
				count++;
			} 
			else 
			{
				i = (r - decodePosition() - 1) & (N - 1);
				j = c - 255 + THRESHOLD;
				for (k = 0; k < j; k++) 
				{
					c = text_buf((i + k) & (N - 1));
					out.write (c);
					text_buf[r++] = (byte)c;
					r &= (N - 1);
					count++;
				}
			}
		}
	}
   
   void setFileWriter(FileWriter f) { fw = f ; }
}
