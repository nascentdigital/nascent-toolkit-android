package com.nascentdigital.util;

import android.content.Context;
import android.util.Base64InputStream;
import android.util.Base64OutputStream;
import android.util.Log;

import com.nascentdigital.util.EncryptionHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/***
 * Utility class that allows you to read and write serializable objects to
 * internal storage. For added security, all content is encrypted.
 */
public class StorageHelper
{
    //region static fields

    private static EncryptionHelper _encryptionHelper = null;

    //endregion

    //region static public methods

    /**
     * Deletes a file from internal storage
     * @param filename filename of the file to delete
     * @param ctx application context
     */
    public static void deleteFromInternalStorage(String filename, Context ctx)
    {
        try
        {
            ctx.deleteFile(filename);
        }
        catch (Exception ex)
        {
            Log.e("StorageHelper", "Error deleting file: " + filename, ex);
        }
    }

    /**
     * Writes a serializable object to internal storage.
     * @param filename Filename for the file that the serialzed object will be stored in.
     * @param object Object to be saved. Must extend serializable.
     * @param ctx Application context
     * @param <T> Class type of the object being saved. Must extend Serializable.
     */
    public static <T extends Serializable> void saveToInternalStorage(String filename, T object, Context ctx)
    {
        try
        {
            //serialize to string
            String objectString = objectToString(object);
            if (objectString == null)
            {
                Log.e("StorageHelper", "Error, seralized object is null");
                return;
            }
            //encrypt
            objectString = getEncryptionHelper(ctx).encrypt(objectString);

            //write to file
            byte[] objectBytes = objectString.getBytes();
            FileOutputStream fos = ctx.openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(objectBytes, 0, objectBytes.length);
            fos.close();
        }
        catch (Exception ex)
        {
            Log.e("StorageHelper", "Error saving file: " + filename, ex);
        }
    }

    /***
     * Loads a serializable object from internal storage.
     * @param filename Filename of the file that contains the serialized object.
     * @param ctx Application Context
     * @param <T> Type of the object that will be read from storage.
     * @return The deserialized object; or null if the file does not exist or there was an error
     *          reading or deserializing the object.
     */
    public static <T extends Serializable> T loadFromInternalStorage(String filename, Context ctx)
    {
        T object = null;

        try {
            //read from file
            FileInputStream fis = ctx.openFileInput(filename);
            StringBuilder builder = new StringBuilder();
            int ch;
            while((ch = fis.read()) != -1){
                builder.append((char)ch);
            }
            String objectString = builder.toString();
            fis.close();
            //decrypt
            objectString = getEncryptionHelper(ctx).decrypt(objectString);
            //deserialize
            object = (T)stringToObject(objectString);


        }
        catch (Exception ex)
        {
            Log.w("StorageHelper", "Error loading file: " + filename, ex);
        }

        return object;
    }

    //endregion

    //region private static methods

    private static EncryptionHelper getEncryptionHelper (Context ctx)
    {
        if (_encryptionHelper == null)
        {
            _encryptionHelper = new EncryptionHelper(ctx);
        }
        return _encryptionHelper;
    }

    private static <T extends Serializable> String objectToString(T object) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            new ObjectOutputStream(out).writeObject(object);
            byte[] data = out.toByteArray();
            out.close();

            out = new ByteArrayOutputStream();
            Base64OutputStream b64 = new Base64OutputStream(out,0);
            b64.write(data);
            b64.close();
            out.close();

            return new String(out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Object stringToObject(String encodedObject) {
        try {
            return new ObjectInputStream(new Base64InputStream(new ByteArrayInputStream(encodedObject.getBytes()), 0)).readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    //endregion

}
