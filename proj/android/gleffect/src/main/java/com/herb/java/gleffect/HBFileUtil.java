package com.herb.java.gleffect;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by zj-db0519 on 2018/5/14.
 */

public class HBFileUtil {

    public static String readShaderResource(Context context, int resourceId) {
        StringBuilder builder = new StringBuilder();
        InputStream inStream = null;
        InputStreamReader iStreamReader = null;
        BufferedReader bufferReader = null;
        try {
            inStream = context.getResources().openRawResource(resourceId);
            iStreamReader = new InputStreamReader(inStream);
            bufferReader = new BufferedReader(iStreamReader);
            String line;
            while ((line = bufferReader.readLine()) != null) {
                builder.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                    inStream = null;
                }
                if (iStreamReader != null) {
                    iStreamReader.close();
                    iStreamReader = null;
                }
                if (bufferReader != null) {
                    bufferReader.close();
                    bufferReader = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return builder.toString();
    }

}
