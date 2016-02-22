package monash.infotech.monashp2pdatasync.connectivity.rest;

import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;


public class RestResponse {

    private ByteArrayOutputStream os;
    private String contentType = "text/plain";
    private String contentEncoding;
    private int responseCode;
    private String responseMsg;
    private long lastModified;

    public RestResponse() {
        os = new ByteArrayOutputStream();
    }

    public RestResponse(byte[] bytes) throws IOException {
        this();

        byte[] buffer = new byte[1024];
        int count = 0;
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        while ((count = bis.read(buffer)) != -1) {
            write(buffer, 0, count);
        }
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public void setResponseMessage(String msg) {
        this.responseMsg = msg;
    }

    public String getResponseMessage() {
        return responseMsg;
    }

    public void setResponseCode(int code) {
        this.responseCode = code;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void write(byte[] bytes, int start, int length) {
        os.write(bytes, start, length);
    }

    public byte[] getDataAsByteArray() {
        return os.toByteArray();
    }

    public String getDataAsString() {
        try {
            return os.toString("UTF-8");
        } catch (Exception ex) {
            Logger.getLogger(RestConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public OutputStream getOutputStream() {
        return os;
    }


    public <T> T getDataAsObject(Class<T> clazz) {
        Gson gson = new Gson();
        Object obj = gson.fromJson(getDataAsString(), clazz);
        return (T) obj;
    }
    public <T> T getDataAsObject(Type type) {
        Gson gson = new Gson();
        Object obj = gson.fromJson(getDataAsString(), type);
        return (T) obj;
    }
}

