#include <string.h>
#include <jni.h>
#include "it_unipr_netsec_tuntap_TuntapSocket.h"

#include <fcntl.h>  // O_RDWR
#include <string.h> // memset(), memcpy()
#include <stdio.h> // perror(), printf(), fprintf()
#include <stdlib.h> // exit(), malloc(), free()
#include <stdbool.h> // bool
#include <sys/ioctl.h> // ioctl()

// includes for struct ifreq, etc.
#include <sys/types.h>
#include <sys/socket.h>
#include <linux/if.h>
#include <linux/if_tun.h>


#define __PNAME "TunSocketImp: "


/** Logs an error message. */
void printerr(const char* msg)
{  // append a header to the message
   char* hdr=__PNAME;
   int hdr_len=strlen(hdr);
   int msg_len=strlen(msg);
   char str[hdr_len+msg_len+1];
   strncpy(str,hdr,hdr_len);
   strncpy(str+hdr_len,msg,msg_len);
   str[hdr_len+msg_len]='\0';
   
   #ifdef _WIN32
      int err=WSAGetLastError();
      fprintf(stderr,"%s: %d",str,err);
   #else
      perror(str);
   #endif
}


JNIEXPORT jint JNICALL Java_it_unipr_netsec_tuntap_TuntapSocket_open(JNIEnv* env, jobject obj, jboolean tun_j, jstring name_j)
{
	return Java_it_unipr_netsec_tuntap_TuntapSocket_openDev(env,obj,tun_j,name_j,NULL);
}


JNIEXPORT jint JNICALL Java_it_unipr_netsec_tuntap_TuntapSocket_openDev(JNIEnv* env, jobject obj, jboolean tun_j, jstring name_j, jstring dev_file_j)
{
	struct ifreq ifr;
	long fd, err;
    const char* devname;
	const char* dev_file = "/dev/net/tun";
	bool iff_vnet_hdr=false;
	
	memset(&ifr, 0, sizeof(ifr));

	// set name
	if (name_j!=NULL)
	{	// ioctl use ifr_name as the name of TUN interface to open, e.g. "tun0"
		devname=(*env)->GetStringUTFChars(env,name_j,0); // devname BEGIN
		//printf("DEBUG: tuntap: open device %s\n", devname);
		strncpy(ifr.ifr_name, devname, IFNAMSIZ);  
		if (strncmp(devname,"macvtap",7)==0) iff_vnet_hdr=true;
		(*env)->ReleaseStringUTFChars(env,name_j,devname); // devname END
	}

	// set flags
	if (tun_j==JNI_TRUE) ifr.ifr_flags = IFF_TUN;
	else
	{	ifr.ifr_flags = IFF_TAP | IFF_NO_PI;
		if (iff_vnet_hdr) ifr.ifr_flags |= IFF_VNET_HDR; 
	}

	// open
	if (dev_file_j!=NULL) dev_file=(*env)->GetStringUTFChars(env,dev_file_j,0); // dev_file BEGIN
	//printf("DEBUG: tuntap: open file %s\n", dev_file);
	if ((fd=open(dev_file, O_RDWR))==-1)
	{	fprintf(stderr, "error trying to open '%s' on device file '%s'\n", devname, dev_file);
		printerr("open()");
		exit(1);
	}
	if (dev_file_j!=NULL) (*env)->ReleaseStringUTFChars(env,dev_file_j,dev_file); // dev_file END	
	
	
	// init
	if ((err=ioctl(fd, TUNSETIFF, (void *)&ifr))==-1)
	{	printerr("ioctl TUNSETIFF");
		close(fd);
		exit(1);
	}

	return fd;
}


JNIEXPORT void JNICALL Java_it_unipr_netsec_tuntap_TuntapSocket_close(JNIEnv* env, jobject obj, jint fd)
{
	close(fd);
}


JNIEXPORT jint JNICALL Java_it_unipr_netsec_tuntap_TuntapSocket_write(JNIEnv* env, jobject obj, jint fd, jbyteArray data_j, jint off, jint len)
{
   jbyte* data=(*env)->GetByteArrayElements(env,data_j,0);
   int nbytes=write(fd,(char*)(data+off),len);
   if (nbytes<0) 
   {	printerr("write()");
		exit(EXIT_FAILURE);
   }
   (*env)->ReleaseByteArrayElements(env,data_j,data,0);
 
   //printf("DEBUG: tuntap: sent %d bytes\n", nbytes);
 
   return nbytes;
}


JNIEXPORT jint JNICALL Java_it_unipr_netsec_tuntap_TuntapSocket_read(JNIEnv* env, jobject obj, jint fd, jbyteArray data_j, jint off)
{
   jsize len=(*env)->GetArrayLength(env,data_j);
   jbyte* data=(*env)->GetByteArrayElements(env,data_j,0);

	//printf("DEBUG: reading from fd %llu\n", fd);
 	int nbytes=read(fd, (char*)(data+off), len);
   if (nbytes<0) 
   {  printerr("read()");
      exit(EXIT_FAILURE);
   }
   (*env)->ReleaseByteArrayElements(env,data_j,data,0);

   //printf("DEBUG: tuntap: received %d bytes\n", nbytes);
   
   return nbytes;
}
