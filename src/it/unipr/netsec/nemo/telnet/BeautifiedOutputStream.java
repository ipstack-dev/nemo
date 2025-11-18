package it.unipr.netsec.nemo.telnet;


import java.io.IOException;
import java.io.OutputStream;


/** Adds CR before LF, when is missing, before writing to an output stream.
 */
class BeautifiedOutputStream extends OutputStream {

	OutputStream out;
	int last=-1;
	byte[] buffer=null;
	
	
	public BeautifiedOutputStream(OutputStream out) {
		this.out=out;
	}
	
	@Override
	public synchronized void write(int b) throws IOException {
		if (b=='\n' && last!='\r') out.write('\r');
		out.write(b);
		last=b;
	}
	
	@Override
	public synchronized void write(byte[] buf, int off, int len) throws IOException {
		if (buffer==null || buffer.length<len*2) buffer=new byte[len*2];
		int num=0;
		int end=off+len;
		for (; off<end; off++) {
			int b=0xff&buf[off];
			if (b=='\n' && last!='\r') buffer[num++]=(byte)'\r';
			buffer[num++]=(byte)b;
			last=b;
		}
		out.write(buffer,0,num);
	}

}
