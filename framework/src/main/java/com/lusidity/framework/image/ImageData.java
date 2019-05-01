/*
 * Copyright 2018 lusidity inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.lusidity.framework.image;

import java.awt.image.BufferedImage;

public class ImageData {

    private String contentType = null;
    @SuppressWarnings("FieldMayBeFinal")
    private byte[] imageBits = null;
    private BufferedImage bufferedImage = null;

    public ImageData(String contentType, BufferedImage bufferedImage) {
        super();
        this.contentType = contentType;
        this.bufferedImage = bufferedImage;
    }

    public String getContentType() {
        return this.contentType;
    }

    public int getWidth() {
        return this.bufferedImage.getWidth();
    }

    public int getHeight() {
        return this.bufferedImage.getHeight();
    }

    public boolean isPortrait()
    {
        return (this.getHeight()>this.getWidth());
    }
    public boolean isSquare()
    {
        return (this.getHeight() == this.getWidth());
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public byte[] getImageBits() {

        if(((null == this.imageBits) || (0==this.imageBits.length)) && (null!=this.bufferedImage))
        {
            this.imageBits = ImageX.getBits(this.bufferedImage, this.contentType);
        }
        return this.imageBits;
    }

    public BufferedImage getBufferedImage()
    {
        if((null == this.bufferedImage) && (null != this.imageBits))
        {
            this.bufferedImage = ImageX.getBufferedImage(this.imageBits);
        }
        return this.bufferedImage;
    }

    public int getType() {
        return this.bufferedImage.getType();
    }
}
