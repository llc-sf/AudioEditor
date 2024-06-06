package dev.android.player.framework.utils;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class IOUtils {
    public static final int BUFFER_SIZE = 1024 * 8;
    public static final int MAX_STRING_LENGTH = 4096000;// 超过这个值的string，就不解析了，防止oom

    public static String getFacebookPhotoUrl(String url) {
        try {
            URI uri = new URI(url); //去除url链接中的参数，如：http://photogrid.org/picture1.jpg?time=1024
            return uri.getScheme() + "://" + uri.getHost() + uri.getPath();
        } catch (URISyntaxException ignore) {
        }
        return "";
    }

    /**
     * 将url变成图片的地址
     */
    public static String changeUrlToName(String url) {
        String name = url.replaceAll(":", "_");
        name = name.replaceAll("//", "_");
        name = name.replaceAll("/", "_");
        name = name.replaceAll("=", "_");
        name = name.replaceAll(",", "_");
        name = name.replaceAll("&", "_");
        name = name.replaceAll("\\?", "_");
        // linux 文件名长度限制255，这里最长取200个字符
        if (name.length() > 200) {
            return name.substring(name.length() - 200);
        }
        return name;
    }

    public static String addFileImageExtensionIfNecessary(String fileName) {
        if (fileName == null)
            return null;
        String tmpName = fileName.toLowerCase(Locale.ENGLISH);

        if (tmpName.endsWith(".png")
                || tmpName.endsWith(".jpg")
                || tmpName.endsWith(".gif")
                || tmpName.endsWith(".bmp")
                || tmpName.endsWith(".jpeg")
                || tmpName.endsWith(".webp")
                || tmpName.endsWith(".mpo")) {
            return fileName;
        }

        if (tmpName.contains(".png"))
            return fileName.concat(".png");

        // 其他当jpg处理
        return fileName.concat(".jpg");
    }

    /**
     * 根据文件或者文件夹路径，检测文件或文件夹是否存在
     *
     * @param path  文件或者文件夹路径
     * @param isDir 检测文件夹是否存在
     * @return true表示文件或者文件夹存在
     */
    public static boolean checkFileIsExist(String path, boolean isDir) {
        if (path == null) return false;
        File file = new File(path);
        return file.exists() && isDir == file.isDirectory();
    }

    public static String getParentPath(String path) {
        if (path == null) return null;
        File file = new File(path);
        return file.getParent();
    }

    public static String getParentPath2(String path) {
        if (path != null) {
            int i = path.lastIndexOf('/');
            if (i >= 0)
                return path.substring(0, i);
        }
        return "";
    }

    /**
     * 获得字符串的MD5值，取8-24位
     *
     * @param str 字符串
     * @return 字符串的MD5值
     */
    public static String getMd5Value(String str) {
        return getMd5Value(str, 8);
    }

    public static String getMd5Value(String str, int start) {
        String md5Str = null;
        if (str != null && str.length() != 0) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(str.getBytes());
                byte b[] = md.digest();

                String hexString = bytesToHexString(b);

                // 取16位
                md5Str = hexString.substring(start, start + 16);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                md5Str = changeUrlToName(str);
            }
        }
        return md5Str;
    }

    public static String encodeBase64(String str) {
        byte[] encode = Base64.encode(str.getBytes(), Base64.DEFAULT);
        return new String(encode);
    }

    public static String decodeBase64(String str) {
        byte[] encode;
        try {
            encode = Base64.decode(str.getBytes(), Base64.DEFAULT);
        } catch (IllegalArgumentException e) {
            return null;
        }
        return new String(encode);
    }

    public static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    private static PublicKey loadPublicKey(String publicKeyStr) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] buffer = Base64.decode(publicKeyStr.getBytes(), Base64.DEFAULT);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
        return keyFactory.generatePublic(keySpec);
    }

    public static String getRSAString(String str) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, loadPublicKey("MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBALuCRM5wqGt5Ehhq/pdGHVFZ5kemv5NSSaOf6wc0GBJZ4ZBRGsg0SnYsdbb3p3/a9L5IU66b72tWDb89jZur4jUCAwEAAQ=="));
            byte[] encryptedBytes = cipher.doFinal(str.getBytes("utf-8"));
            encryptedBytes = Base64.encode(encryptedBytes, Base64.NO_WRAP);
            return new String(encryptedBytes, "utf-8");
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * A hashing method that changes a string (like a URL) into a hash suitable for using as a
     * disk filename.
     */
    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    /**
     * 递归删除文件夹及文件夹下的文件，也可以删除一个单独的文件
     *
     * @param file 要删除的文件路径
     */
    public static boolean deleteAllFile(File file) {
        if (file == null)
            return false;

        if (file.isDirectory()) {
            File[] fileList = file.listFiles();
            if (fileList != null && fileList.length > 0) {
                for (File f : fileList) {
                    deleteAllFile(f);
                }
            }
            return file.delete();
        } else {
            return file.delete();
        }
    }

    /**
     * 读取文件到字符串，大文件勿用
     *
     * @param file
     * @param charset
     * @return
     */
    public static String readFileToString(File file, String charset) {
        if (file == null)
            return null;
        String data = null;
        byte[] buffer = new byte[BUFFER_SIZE];
        ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            for (int len = -1; (len = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, len);
                if (out.size() > MAX_STRING_LENGTH) {
                    Log.e("IoUtils", "File too large, maybe not a string. " + file.getAbsolutePath());
                    return null;
                }
            }
            data = out.toString(charset);
        } catch (IOException e) {
            e.printStackTrace();
            data = null;
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                }
            try {
                out.close();
            } catch (IOException e) {
            }
            in = null;
            out = null;
            buffer = null;
        }
        return data;
    }

    /**
     * 字符串保存到文件
     *
     * @param data
     * @param file
     * @param charset
     * @return 是否保存成功
     */
    public static boolean saveStringToFile(String data, File file, String charset) {
        if (file == null || data == null)
            return false;
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(data.getBytes(charset));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null)
                try {
                    out.close();
                } catch (IOException e) {
                }
        }
        return false;
    }

    /**
     * InputStream 保存到文件
     *
     * @param in
     * @param file
     * @return 是否保存成功
     */
    public static boolean saveInputStreamToFile(InputStream in, File file, boolean closeInputStream) {
        if (file == null || in == null)
            return false;
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            byte[] data = new byte[BUFFER_SIZE];
            int count = -1;
            while ((count = in.read(data)) >= 0) {
                out.write(data, 0, count);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignore) {
                }
            }
            if (closeInputStream) {
                try {
                    in.close();
                } catch (IOException ignore) {
                }
            }
        }
        return false;
    }

    public static boolean saveReaderToFile(Reader in, File file, boolean closeReader) {
        if (file == null || in == null)
            return false;
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
            char[] data = new char[BUFFER_SIZE];
            int count = -1;
            while ((count = in.read(data)) >= 0) {
                if (BuildConfig.DEBUG) System.out.println(data);
                out.write(data, 0, count);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (closeReader) {
                try {
                    in.close();
                } catch (IOException ignore) {
                }
            }
        }
        return false;
    }

    /**
     * 拼接http url
     *
     * @param url
     * @param name
     * @param params
     * @param values
     * @return url/name?params[0]=values[0]&params[1]=values[1]
     */
    public static String createUrl(String url, String name, String[] params, String[] values) {
        if (url == null)
            return null;
        StringBuilder sb = new StringBuilder(url);
        if (name != null) {
            if (!url.endsWith("/"))
                sb.append('/');
            sb.append(name);
        }
        if (params != null && values != null) {
            sb.append("?");
            for (int i = 0, len = Math.min(params.length, values.length); i < len; i++) {
                if (i != 0)
                    sb.append("&");
                sb.append(params[i]);
                sb.append("=");
                sb.append(values[i]);
            }
        }
        return sb.toString();
    }

    /**
     * Zip解压缩
     *
     * @param zipFileString
     * @param outPathString
     * @return 是否解压缩成功
     */
    public static boolean unZipFolder(String zipFileString, String outPathString) {
        ZipInputStream inZip = null;
        ZipEntry zipEntry;
        String szName;
        try {
            String outCanonicalPathString = new File(outPathString).getCanonicalPath();
            inZip = new ZipInputStream(new FileInputStream(zipFileString));
            while ((zipEntry = inZip.getNextEntry()) != null) {
                szName = zipEntry.getName();
                if (zipEntry.isDirectory()) {
                    //获取部件的文件夹名
                    szName = szName.substring(0, szName.length() - 1);
                    File folder = new File(outPathString + File.separator + szName);
                    if (!folder.getCanonicalPath().startsWith(outCanonicalPathString)) return false;
                    folder.mkdirs();
                } else {
                    File file = new File(outPathString + File.separator + szName);
                    if (!file.getCanonicalPath().startsWith(outCanonicalPathString)) return false;
                    File parent = file.getParentFile();
                    if (!parent.exists()) parent.mkdirs();
                    // 获取文件的输出流
                    FileOutputStream out = new FileOutputStream(file);
                    int len;
                    byte[] buffer = new byte[BUFFER_SIZE];
                    while ((len = inZip.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                        out.flush();
                    }
                    out.close();
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (inZip != null) {
                try {
                    inZip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * InputStream 读取到String
     *
     * @param in 方法中会调用close方法
     * @return
     * @throws IOException
     */
    public static String inputStreamToString(InputStream in, String charset) throws IOException {
        if (in == null)
            return null;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try {
            byte[] data = new byte[BUFFER_SIZE];
            int count = -1;
            while ((count = in.read(data)) >= 0) {
                outStream.write(data, 0, count);
                if (outStream.size() > MAX_STRING_LENGTH) {
                    Log.e("IoUtils", "Data too large, maybe not a string. ");
                    return null;
                }
            }
            data = null;
            return new String(outStream.toByteArray(), charset);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * 字节转MB，保留小数点后两位
     *
     * @param size
     * @return
     */
    public static float byteToMb(long size) {
        float mb = size / 1024.0f / 1024.0f;
        return Math.round(mb * 100) / 100.0f;
    }

    /**
     * 获得本地存储的可用空间，以字节为单位
     * 该方法不能用于4.4以上获取外置SD卡的空间大小，只能获得默认路径下的空间大小
     *
     * @param path
     * @return
     */
    public static long getUsableSpace(File path) {
        if (!path.exists()) {
            path.mkdirs();
        }
        //fix online bug 3342
        try {
            final StatFs stats = new StatFs(path.getPath());
            return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    /**
     * assetFile 移动到 SD 卡
     *
     * @param am
     * @param destinationfile
     * @param assetsfileName
     */
    public static void writeAssetFileToSD(AssetManager am, File destinationfile, String assetsfileName) {
        FileOutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            inputStream = am.open(assetsfileName);
            destinationfile.createNewFile();
            if (!destinationfile.isFile() || !destinationfile.canWrite()) {
                throw new IOException();
            }
            outputStream = new FileOutputStream(destinationfile);
            byte buf[] = new byte[2048];
            int count;
            while ((count = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    /**
     * byte[]转Int
     *
     * @param bytes
     * @return
     */
    public static int byteArrayToInt(byte[] bytes) {
        int value = 0;
        //由高位到低位
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (bytes[i] & 0x000000FF) << shift;//往高位游
        }
        return value;
    }

    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Get a default usable cache directory (external if available, internal otherwise).
     *
     * @param context The context to use
     * @return The cache dir
     */
    public static File getCacheDir(Context context) {
        File cacheFile = context.getExternalCacheDir();
        if (cacheFile != null && cacheFile.exists()) return cacheFile;
        return context.getCacheDir();
    }

    /**
     * 复制单个文件
     *
     * @param oldfile String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public static boolean copyFile(File oldfile, File newPath) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            int bytesum = 0;
            int byteread = 0;
            if (oldfile.exists()) { //文件存在时
                inputStream = new FileInputStream(oldfile); //读入原文件
                outputStream = new FileOutputStream(newPath);
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((byteread = inputStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    if (BuildConfig.DEBUG) System.out.println(bytesum);
                    outputStream.write(buffer, 0, byteread);
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
        return false;
    }

    public static void writeStringToDisk(String str, File file) throws IOException {
        if (str == null || file == null)
            return;
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(str.getBytes());
        fos.close();
    }

    public static String readStr(File file) throws IOException {
        if (file != null && file.exists()) {
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buf = new byte[1024];
            int len = 0;
            StringBuffer stringBuffer = new StringBuffer();
            while ((len = fileInputStream.read(buf)) != -1) {
                stringBuffer.append(new String(buf, 0, len));
            }
            fileInputStream.close();
            return stringBuffer.toString();
        }
        return null;
    }

    public static boolean copyFileWithCheckFreeSpace(File oldFile, File newFile) {
        if (oldFile == null || newFile == null) return false;
        long bytesAvailable = getUsableSpace(newFile.getParentFile());
        if (oldFile.length() > bytesAvailable) {
            return false;
        }
        return copyFile(oldFile, newFile);
    }

    public static String getExtension(String fileName) {
        if (fileName == null) return null;
        String extension = null;

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }
        return extension;
    }

    public static String getName(String path) {
        if (path == null) return null;
        int start = path.lastIndexOf('/');
        if (start < 0) start = 0;
        else start++;
        return path.substring(start);
    }

    public static String getNameWithoutExtension(String path) {
    /*
     /sdcard/123.png -> 123
     /sdcard/123 -> 123
     /sdcard/123. -> 123. (.后没东西，认为不含后缀名)
     123.png -> 123
     */
        if (path == null) return null;
        int start = path.lastIndexOf('/');
        int end = path.lastIndexOf('.');
        if (start < 0) start = 0;
        else start++;
        if (end < start || end + 1 >= path.length()) end = path.length();
        return path.substring(start, end);
    }

    public static String getNameWithExtension(String path) {
    /*
     /sdcard/123.png -> 123.png
     */
        if (path == null) return null;
        int start = path.lastIndexOf('/');
        if (start < 0) start = 0;
        else start++;
        return path.substring(start);
    }

    public static boolean deleteViaContentProvider(Context context, String fullname) {
        Uri uri = getFileUri(context, fullname);

        if (uri == null) {
            return false;
        }

        try {
            ContentResolver resolver = context.getContentResolver();

            // change type to image, otherwise nothing will be deleted
            ContentValues contentValues = new ContentValues();
            int media_type = 1;
            contentValues.put("media_type", media_type);
            resolver.update(uri, contentValues, null, null);

            return resolver.delete(uri, null, null) > 0;
        } catch (Throwable e) {
            return false;
        }
    }

    public static Uri getFileUri(Context context, String fullname) {
        // Note: check outside this class whether the OS version is >= 11
        Uri uri = null;
        Cursor cursor = null;
        ContentResolver contentResolver = null;

        try {
            contentResolver = context.getContentResolver();
            if (contentResolver == null)
                return null;

            uri = MediaStore.Files.getContentUri("external");
            String[] projection = new String[2];
            projection[0] = "_id";
            projection[1] = "_data";
            String selection = "_data = ? ";    // this avoids SQL injection
            String[] selectionParams = new String[1];
            selectionParams[0] = fullname;
            String sortOrder = "_id";
            cursor = contentResolver.query(uri, projection, selection, selectionParams, sortOrder);

            if (cursor != null) {
                try {
                    if (cursor.getCount() > 0) // file present!
                    {
                        cursor.moveToFirst();
                        int dataColumn = cursor.getColumnIndex("_data");
                        String s = cursor.getString(dataColumn);
                        if (!s.equals(fullname))
                            return null;
                        int idColumn = cursor.getColumnIndex("_id");
                        long id = cursor.getLong(idColumn);
                        uri = MediaStore.Files.getContentUri("external", id);
                    } else // file isn't in the media database!
                    {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("_data", fullname);
                        uri = MediaStore.Files.getContentUri("external");
                        uri = contentResolver.insert(uri, contentValues);
                    }
                } catch (Throwable e) {
                    uri = null;
                } finally {
                    cursor.close();
                }
            }
        } catch (Throwable e) {
            uri = null;
        }
        return uri;
    }

    private static final String VIDEO_DEFUALT_MIME_TYPE = "video/*";

    public static String getVideoMime(String suffix) {
        if (suffix.contains(".")) {
            suffix = getExtension(suffix);
        }
        String mime = null;
        if ("rm".equalsIgnoreCase(suffix)) {
            mime = VIDEO_DEFUALT_MIME_TYPE;
        } else {
            mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
        }
        if (mime == null || "*/*".equals(mime) || !mime.startsWith("video/")) {
            mime = VIDEO_DEFUALT_MIME_TYPE;
        }
        return mime;
    }

}
