/*
 *  Copyright (C) 2017 Bilibili
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.bilibili.boxing.impl;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.bilibili.boxing.loader.IBoxingCrop;
import com.bilibili.boxing.model.config.BoxingCropOption;
import com.bilibili.boxing.utils.BoxingFileHelper;
import com.yalantis.ucrop.UCrop;

import java.util.Locale;

/**
 * use Ucrop(https://github.com/Yalantis/uCrop) as the implement for {@link IBoxingCrop}
 *
 * @author ChenSL
 */
public class BoxingUcrop implements IBoxingCrop {

    @Override
    public void onStartCrop(Context context, Fragment fragment, @NonNull BoxingCropOption cropConfig,
                            @NonNull String path, int requestCode) {
        Uri uri = new Uri.Builder()
                .scheme("file")
                .appendPath(path)
                .build();

        String fileSuffix = path.substring(path.lastIndexOf("."), path.length());
        Uri destUri = cropConfig.getDestination();
        if (destUri == null) {
            destUri =  new Uri.Builder()
                    .scheme("file")
                    .appendPath(BoxingFileHelper.getCacheDir(context))
                    .appendPath(String.format(Locale.US, "%s" + fileSuffix, System.currentTimeMillis()))
                    .build();
        }
        UCrop.Options crop = new UCrop.Options();
        // do not copy exif information to crop pictures
        // because png do not have exif and png is not Distinguishable
        if (".png".equalsIgnoreCase(fileSuffix)) {
            crop.setCompressionFormat(Bitmap.CompressFormat.PNG);
        } else if (".webp".equalsIgnoreCase(fileSuffix)) {
            crop.setCompressionFormat(Bitmap.CompressFormat.WEBP);
        } else {
            crop.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        }
        crop.withMaxResultSize(cropConfig.getMaxWidth(), cropConfig.getMaxHeight());
        crop.withAspectRatio(cropConfig.getAspectRatioX(), cropConfig.getAspectRatioY());

        UCrop.of(uri, destUri)
                .withOptions(crop)
                .start(context, fragment, requestCode);
    }

    @Override
    public Uri onCropFinish(int resultCode, Intent data) {
        if (data == null) {
            return null;
        }
        Throwable throwable = UCrop.getError(data);
        if (throwable != null) {
            return null;
        }
        return UCrop.getOutput(data);
    }
}
