package com.game.awesa.utils;

import android.webkit.MimeTypeMap;

public class UrlUtils {

    /**
     * see http://stackoverflow.com/a/8591230/1673548
     */
    public static String getUrlMimeType(final String urlString) {
        if (urlString == null) {
            return null;
        }

        String extension = MimeTypeMap.getFileExtensionFromUrl(urlString);
        if (extension == null) {
            return null;
        }

        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String mimeType = mime.getMimeTypeFromExtension(extension);
        if (mimeType == null) {
            return null;
        }

        return mimeType;
    }
}
