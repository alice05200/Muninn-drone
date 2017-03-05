package studio.bachelor.utility;

import android.graphics.Bitmap;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by User on 2017/3/4.
 */
public class SavePicture implements Runnable {
    String filename;
    Bitmap bitmap;
    ZipOutputStream zipOutputStream;
    int i;
    File file;
    public SavePicture(String filename, Bitmap bitmap, ZipOutputStream zip_stream, int BUFFER, File zip_directory){
        this.filename = filename;
        this.bitmap = bitmap;
        zipOutputStream = zip_stream;
        i = BUFFER;
        file = zip_directory;
    }
    @Override
    public void run() {
        WriteBitmapToZIP("birdview", bitmap, zipOutputStream, i, file);
    }
    private void WriteBitmapToZIP(String filename, Bitmap bitmap, ZipOutputStream zip_stream, int BUFFER, File zip_directory) {
        byte data[] = new byte[BUFFER];
        if (bitmap != null) {
            try {
                File image_file;
                if(filename.lastIndexOf(".png") == -1)
                    image_file = new File(zip_directory, filename + ".png");
                else
                    image_file = new File(zip_directory, filename);
                FileOutputStream bitmap_file = new FileOutputStream(image_file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bitmap_file);
                FileInputStream file_input = new FileInputStream(image_file);
                BufferedInputStream origin = new BufferedInputStream(file_input, BUFFER);
                ZipEntry entry;
                if(filename.lastIndexOf(".png") == -1)
                    entry = new ZipEntry(filename + ".png");
                else
                    entry = new ZipEntry(filename);
                zip_stream.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    zip_stream.write(data, 0, count);
                }
                origin.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
