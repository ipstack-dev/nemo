package io.ipstack.http;


import java.util.HashMap;


/** Maintains the standard IANA MIME types for common file types.
 */
public abstract class MimeTypes {

	/** Default type */
	public static String DEFAULT_TYPE="application/octet-stream";

	/** Mapping table */
	static HashMap<String,String> map=null;

	
	static void initMap() {
		map=new HashMap<String,String>();
		map.put("aac","audio/aac");
		map.put("abw","application/x-abiword");
		map.put("arc","application/x-freearc");
		map.put("avi","video/x-msvideo");
		map.put("azw","application/vnd.amazon.ebook");
		map.put("bin","application/octet-stream");
		map.put("bmp","image/bmp");
		map.put("bz","application/x-bzip");
		map.put("bz2","application/x-bzip2");
		map.put("csh","application/x-csh");
		map.put("css","text/css");
		map.put("csv","text/csv");
		map.put("doc","application/msword");
		map.put("docx","application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		map.put("eot","application/vnd.ms-fontobject");
		map.put("epub","application/epub+zip");
		map.put("gz","application/gzip");
		map.put("gif","image/gif");
		map.put("htm","text/html");
		map.put("html","text/html");
		map.put("ico","image/vnd.microsoft.icon");
		map.put("ics","text/calendar");
		map.put("jar","application/java-archive");
		map.put("jpeg","image/jpeg");
		map.put("jpg","image/jpeg");
		map.put("js","text/javascript");
		map.put("json","application/json");
		map.put("jsonld","application/ld+json");
		map.put("mid","audio/midi audio/x-midi");
		map.put("midi","audio/midi audio/x-midi");
		map.put("mjs","text/javascript");
		map.put("mp3","audio/mpeg");
		map.put("mpeg","audio/mpeg");
		map.put("mpkg","application/vnd.apple.installer+xml");
		map.put("odp","application/vnd.oasis.opendocument.presentation");
		map.put("ods","application/vnd.oasis.opendocument.spreadsheet");
		map.put("odt","application/vnd.oasis.opendocument.text");
		map.put("oga","audio/ogg");
		map.put("ogv","video/ogg");
		map.put("ogx","application/ogg");
		map.put("opus","audio/opus");
		map.put("otf","font/otf");
		map.put("png","image/png");
		map.put("pdf","application/pdf");
		map.put("php","application/x-httpd-php");
		map.put("ppt","application/vnd.ms-powerpoint");
		map.put("pptx","application/vnd.openxmlformats-officedocument.presentationml.presentation");
		map.put("rar","application/vnd.rar");
		map.put("rtf","application/rtf");
		map.put("sh","application/x-sh");
		map.put("svg","image/svg+xml");
		map.put("swf","application/x-shockwave-flash");
		map.put("tar","application/x-tar");
		map.put("tif","image/tiff");
		map.put("tiff","image/tiff");
		map.put("ts","video/mp2t");
		map.put("ttf","font/ttf");
		map.put("txt","text/plain");
		map.put("vsd","application/vnd.visio");
		map.put("wav","audio/wav");
		map.put("weba","audio/webm");
		map.put("webm","video/webm");
		map.put("webp","image/webp");
		map.put("woff","font/woff");
		map.put("woff2","font/woff2");
		map.put("xhtml","application/xhtml+xml");
		map.put("xls","application/vnd.ms-excel");
		map.put("xlsx","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		map.put("xml","application/xml"); // "text/xml" if readable from casual users (RFC 3023, section 3)
		map.put("xul","application/vnd.mozilla.xul+xml");
		map.put("zip","application/zip");
		map.put("3gp","video/3gpp"); // "audio/3gpp" if it doesn't contain video
		map.put("3gp2","video/3gpp2"); // "audio/3gpp2" if it doesn't contain video
		map.put("7z","application/x-7z-compressed");
	}

	
	/** gets the default MIME type for a given file.
	 * @param file the file name or file extension
	 * @return the MIME type */
	public static String getType(String file) {
		if (map==null) initMap();
		if (file!=null && file.indexOf('.')>=0) {
			for (int i=file.length()-1; i>=0; i--) {
				if (file.charAt(i)=='.') {
					file=file.substring(i+1);
					break;
				}
			}
		}
		if (file==null) return DEFAULT_TYPE;
		String type=map.get(file);
		return type!=null? type : DEFAULT_TYPE;
	}

}
