package com.gusta.wakemehome.utilities;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.gusta.wakemehome.utilities.Constants.MAPS_DIR;
import static com.gusta.wakemehome.utilities.Constants.TEMP_IMAGE_FILE;

public class fileUtils {

    // Constant for logging
    private static final String TAG = fileUtils.class.getSimpleName();

    //locate to hold the maps images in our appData
    private static String mapImageFileDir;

    /*
        set the local map directory in app data for image saving
        should call when app first loaded to set the dir path
     */
    public static void setMapsDir(Context context){

        ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
        // path to /data/data/wakemehome/app_data/mapsDir
        mapImageFileDir = cw.getDir(MAPS_DIR, Context.MODE_PRIVATE).getAbsolutePath();

    }

    /*
        save map image - will change the temp file by id.png file
     */
    public static void saveMapImage(int id){
        changeFilePath(getTempPath(),getMapImagePath(id));
    }

    /*
        delete image which connected to specific alarm
     */
    public static void deleteMapImage(int id){
        deleteFileFromPath(getMapImagePath(id));
    }

    /*
        delete temp file which saved for alarm that didn't save yet
     */
    public static void deleteTempImage(){
        deleteFileFromPath(getTempPath());
    }

    /*
        create temp file for alarm which didn't save yet
     */
    public static void createTempMapImage(Bitmap bitmap){
        saveImgToPath(bitmap,getTempPath());
    }

    /*
        get image path from directory
     */
    public static String getMapImagePath(int id) {
        return mapImageFileDir + "/" + id + ".png";
    }

    /*
        get temp image path from directory
    */
    public static String getTempPath() {
        return mapImageFileDir + "/" + TEMP_IMAGE_FILE;
    }

    /*
        file exist in this path
     */
    public static boolean isExistPath(String path){
        File source = new File(path);
        if (source.exists()) {
            return true;
        }
        return false;
    }

    /*
        save image to path
    */
    private static String saveImgToPath(Bitmap bitmapImage, String path){

        File file = new File(path);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            Log.w(TAG, "error while file saved to " + path + " error -> " + e.getMessage());
            return null;
        } finally {
            try {
                assert fos != null;
                fos.flush();
                fos.close();
            } catch (IOException e) {
                Log.w(TAG, "error while closing file stream,  error -> " + e.getMessage());
            }
        }
        return file.getAbsolutePath();

    }

    /*
        delete existing file
     */
    private static void deleteFileFromPath(String path){
        File file = new File(path);

        if (file.exists()) {
            if (!file.delete()) {
                //TODO handle error
                Log.w(TAG, "image delete failed");
            }
        }
    }

    /*
        change file path if exist
     */
    private static void changeFilePath(String sourcePath, String destPath){
        File source = new File(sourcePath);

        // File (or directory) with new name
        File dest = new File(destPath);

        if (!source.exists())
            return;

        deleteFileFromPath(destPath);

        // Rename file
        if(!source.renameTo(dest)){
            //TODO handle error
            Log.w(TAG, "rename from " + sourcePath + " to " + destPath + " failed");
        }
    }
}
