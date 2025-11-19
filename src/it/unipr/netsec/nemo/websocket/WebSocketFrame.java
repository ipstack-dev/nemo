package it.unipr.netsec.nemo.websocket;
/*
 * Copyright 2018 NetSec Lab - University of Parma
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */


import java.io.IOException;
import java.io.InputStream;

import org.zoolu.util.Bytes;


/** WebSocket frame (RFC 6455).
 */
public class WebSocketFrame{	

	/** WS UUID */
	public static final String WS_UUID= "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";


	public static final int OPCODE_CONTINUATION_FRAME= 0x0; // denotes a continuation frame

	public static final int OPCODE_TEXT_FRAME= 0x1; // denotes a text frame

	public static final int OPCODE_BINARY_FRAME= 0x2; // denotes a binary frame

	//public static final int OPCODE_RESERVED= 0x3-7; // are reserved for further non-control frames

	public static final int OPCODE_CONNECTION_CLOSE= 0x8; // denotes a connection close

    public static final int OPCODE_PING= 0x9; // denotes a ping

    public static final int OPCODE_PONG= 0xA; // denotes a pong

    //public static final int OPCODE_RESERVED= 0xB-F; // are reserved for further control frames

	
	boolean fin;
	int opcode;
	byte[] maskingkey; 
	byte[] payload; 
	
	
	/** Creates a new frame.
	 * @param opcode frame type
	 * @param maskingkey masking key
	 * @param payload payload data
	 */
	public WebSocketFrame(int opcode, byte[] maskingkey, byte[] payload) {
		this(opcode,maskingkey,payload,true);
	}

	
	/** Creates a new frame.
	 * @param opcode frame type
	 * @param payload payload data
	 * @param maskingkey masking key
	 * @param fin whether it is the final fragment
	 */
	public WebSocketFrame(int opcode, byte[] payload, byte[] maskingkey, boolean fin) {
		this.opcode= opcode;
		this.maskingkey= maskingkey;
		this.payload= payload;
		this.fin= fin;
	}

	
	/** Reads a frame from an input stream.
	 * @param is the input stream
	 * @throws IOException 
	 */
	public static WebSocketFrame parseWebsocketFrame(InputStream is) throws IOException {
		int b= is.read();
		if (b<0) throw new IOException("Error while reading from inptu stream ("+b+")");
		boolean fin= (b&0x80)!=0;
		int opcode= (b&0x0f);
		b= is.read();
		if (b<0) throw new IOException("Error while reading from inptu stream ("+b+")");
		boolean mask= (b&0x80)!=0;
		long payloadLen= b&0x7f;
		if (payloadLen==126) {
			byte[] buf= new byte[2];
			is.read(buf);
			payloadLen= Bytes.toInt16(buf);
		}
		else if (payloadLen==127) {
			byte[] buf= new byte[8];
			is.read(buf);
			payloadLen= Bytes.toInt32(buf);
			payloadLen<<=32;
			payloadLen+= Bytes.toInt32(buf,4);
		}	
		byte[] maskingkey= null;
		if (mask) {
			maskingkey= new byte[4];
			is.read(maskingkey);
		}
		byte[] payload= null;
		if (payloadLen>0) {
			if (payloadLen>0xffffffffL) throw new IOException("Payload lengh too big for this implementation ("+payloadLen+")");
			payload= new byte[(int)payloadLen];
			is.read(payload);
			if (maskingkey!=null) for (int i= 0; i<payloadLen; ++i) payload[i]^= maskingkey[i%4];
		}
		return new WebSocketFrame(opcode,payload,maskingkey,fin);
	}

		
	/**
	 * @return the Opcode
	 */
	public int getOpcode() {
		return opcode;
	}

	
	/**
	 * @return a text representing the Opcode
	 */
	public String getOpcodeString() {
		switch (opcode) {
			case OPCODE_CONTINUATION_FRAME : return "continue";
			case OPCODE_TEXT_FRAME : return "text";
			case OPCODE_BINARY_FRAME : return "binary";
			case OPCODE_CONNECTION_CLOSE : return "close";
			case OPCODE_PING : return "ping";
			case OPCODE_PONG : return "pong";
			default : return String.valueOf(opcode);
		}
	}

	
	/**
	 * @return the Masking-key
	 */
	public byte[] getMaskingkey() {
		return maskingkey;
	}

	
	/**
	 * @return the Payload
	 */
	public byte[] getPayload() {
		return payload;
	}

	
	/**
	 * @return true if it is the final fragment
	 */
	public boolean isFinal() {
		return fin;
	}

	
	/** Gets the frame length.
	 * @return the length
	 */
	public int getFrameLength() {
		int payloadLen= payload!=null? payload.length : 0;
		return 2+(payloadLen<126?0:payloadLen<65536?2:8)+(maskingkey!=null?4:0)+payloadLen;
	}

	
	/** Gets the frame in a byte array.
	 * @param buf the buffer where the frame has to be written
	 * @param off the offset within the buffer
	 * @return the frame length */
	public int getBytes(byte[] buf, int off) {
		int begin=off;
		int payloadLen= payload!=null? payload.length : 0;
		buf[off++]= (byte)((fin?0x80:0x00) | (opcode&0xf));
		buf[off++]= (byte)((maskingkey!=null?0x08:0x00)  | (payloadLen<126?payloadLen:payloadLen<65536?126:127));
		if (payloadLen>=126) {
			if (payloadLen<65536) {
				Bytes.fromInt16(payloadLen,buf,off);
				off+= 2;
			}
			else {
				Bytes.fromInt64(payloadLen,buf,off);
				off+= 8;
			}
		}
		if (maskingkey!=null) {
			Bytes.copy(maskingkey,0,buf,off,4);
			off+= 4;
		}
		if (payloadLen>0) {
			Bytes.copy(payload,buf,off);
			if (maskingkey!=null) for (int i= 0; i<payloadLen; ++i) buf[off+i]^= maskingkey[i%4];
		}
		return off-begin;
	}

	
	/** Gets the frame.
	 * @return a new byte array containing the frame */
	public byte[] getBytes() {
		byte[] data= new byte[getFrameLength()];
		getBytes(data,0);
		return data;
	}

	
	@Override
	public String toString() {
		StringBuffer sb=new StringBuffer();
		sb.append("opcode=").append(getOpcodeString());
		if (payload!=null) {
			sb.append(", payload=");
			if (opcode==OPCODE_TEXT_FRAME) sb.append('"').append(new String(payload)).append('"');
			else sb.append(Bytes.toHex(payload));
		}
		if (maskingkey!=null) sb.append(", masked");
		if (fin) sb.append(", fin");
		return sb.toString();	
	}
	
}
