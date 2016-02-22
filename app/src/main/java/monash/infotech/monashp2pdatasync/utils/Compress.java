package monash.infotech.monashp2pdatasync.utils;

import android.os.Environment;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import monash.infotech.monashp2pdatasync.messaging.Message;


public class Compress {
    private static final int BUFFER = 2048;


    public static byte[] zip(Message msg) {
        try {
            BufferedInputStream origin = null;

            ByteArrayOutputStream dest = new ByteArrayOutputStream();

            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            byte data[] = new byte[BUFFER];
            //add the msg body
            String jsonMsg = msg.toJson();
            byte[] byteMsg = jsonMsg.getBytes();
            ByteArrayInputStream byteArrayInputStream=new ByteArrayInputStream(byteMsg);
            origin = new BufferedInputStream(byteArrayInputStream, BUFFER);
            ZipEntry entry = new ZipEntry("msgBody");
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            origin.close();
            //add the files
            for (String fileName:msg.getFiles()) {
                FileInputStream fi = new FileInputStream(fileName);
                origin = new BufferedInputStream(fi, BUFFER);
                entry = new ZipEntry(fileName.substring(fileName.lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();
            return dest.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Message unZip(byte[] data)
    {
        Message msg=null;
        Gson gson=new Gson();
        InputStream is;
        ZipInputStream zis;
        try
        {
            String filename;
            is = new ByteArrayInputStream(data);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[2048];
            int count;

            while ((ze = zis.getNextEntry()) != null)
            {
                // zapis do souboru
                filename = ze.getName();

                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    continue;
                }

                if(filename.equals("msgBody"))
                {
                    ByteArrayOutputStream fout = new ByteArrayOutputStream();

                    // cteni zipu a zapis
                    while ((count = zis.read(buffer)) != -1)
                    {
                        fout.write(buffer, 0, count);
                    }
                    fout.close();

                    String temp = new String(fout.toByteArray(), StandardCharsets.UTF_8);
                    msg=gson.fromJson(temp,Message.class);
                }
                else
                {
                    String dir="";
                    if(filename.endsWith(".jpg"))
                    {
                        dir = Environment.getExternalStorageDirectory().
                                getAbsolutePath() + "/monashP2P/image";
                    }
                    else
                    {
                        if(filename.endsWith(".mp4"))
                        {
                            dir = Environment.getExternalStorageDirectory().
                                    getAbsolutePath() + "/monashP2P/video";
                        }
                        else {
                            if(filename.endsWith(".3gpp"))
                            {
                                dir = Environment.getExternalStorageDirectory().
                                        getAbsolutePath() + "/monashP2P/sound";
                            }
                        }
                    }
                    File fmd = new File(dir);
                    fmd.mkdirs();
                    FileOutputStream fout = new FileOutputStream(dir+"/" + filename);

                    // cteni zipu a zapis
                    while ((count = zis.read(buffer)) != -1)
                    {
                        fout.write(buffer, 0, count);
                    }

                    fout.close();
                }

                zis.closeEntry();
            }

            zis.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return msg;
    }
}