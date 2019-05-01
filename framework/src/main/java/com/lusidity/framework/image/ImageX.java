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

import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.internet.http.HttpClientX;
import com.lusidity.framework.text.StringX;
import org.apache.commons.codec.binary.Base64;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;

public
class ImageX
{
// ------------------------------ FIELDS ------------------------------

	private static final Pattern SPLITTER_CONTENT_TYPE = Pattern.compile("/");
	private static final int MAX_SIZE_DIFFERENCE = 10;

// -------------------------- STATIC METHODS --------------------------

	/**
	 * Get an image from the web, scale it to size, and return as a byte[].
	 *
	 * @param url
	 * 	The url to the image on the web.
	 * @param maxSquared
	 * 	The max width or height of the image.
	 * @param enforce
	 * 	The original image must at least be equal to or greater than the maxSquared value.
	 *
	 * @return A scaled byte[]. If the image size is not greater than the max squared size then the original image is
	 *         returned.
	 */
	@SuppressWarnings("MethodWithMultipleReturnPoints")
	public static
	ImageData getImageData(URL url, String referer, int maxSquared, boolean enforce)
	{
		ImageData imageData = ImageX.getImageFromWeb(url, referer);
		return ImageX.getImageData(imageData, maxSquared, enforce);
	}

	/**
	 * Convert a buffered image to a a byte[].
	 *
	 * @param bufferedImage
	 * 	Buffered image to convert.
	 *
	 * @return An image as a byte[].
	 */
	@SuppressWarnings("TypeMayBeWeakened")
	public static
	byte[] getBits(BufferedImage bufferedImage, String contentType)
	{
		byte[] results = null;
		String fileExt = ImageX.getFileExtensionFromContentType(contentType);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
		{
			ImageIO.write(bufferedImage, fileExt, baos);
			baos.flush();
			results = baos.toByteArray();
		}
		catch (IOException ignored)
		{
		}
		finally
		{
			try
			{
				baos.close();
			}
			catch (IOException ignored)
			{
			}
		}

		return results;
	}

	public static
	String getFileExtensionFromContentType(CharSequence contentType)
	{
		String result = null;
		String[] split = ImageX.SPLITTER_CONTENT_TYPE.split(contentType);
		if (split.length == 2)
		{
			result = split[1];
		}
		return result;
	}

	/**
	 * Get an image using the specified URL and convert it to a String that can be used by an HTML img element.
	 *
	 * @param url
	 * 	The URL reference to the image. * @param contentType Will be set as the content type.
	 *
	 * @return A string that can be rendered by an HTML img element.
	 */
	public static
	String getAsHTMLSource(URL url, String referer)
		throws ApplicationException
	{
		String result;

		ImageData imageData = ImageX.getImageFromWeb(url, referer);

		if (null != imageData)
		{
			result = ImageX.toHTMLSource(imageData.getImageBits(), imageData.getContentType());
		}
		else
		{
			throw new ApplicationException("URL did not return an image.");
		}

		return result;
	}

	/**
	 * Get an image using the specified URL as a byte array.
	 *
	 * @param url
	 * 	The URL reference to the image.
     * 	@param referer The source of the request.
	 *
	 * @return A byte array.
	 */
	public static
	ImageData getImageFromWeb(URL url, String referer)
	{
		ImageData result = null;
		BufferedImage bufferedImage;
		InputStream inputStream = null;

		try
		{
			URLConnection uc = url.openConnection();
            if(!StringX.isBlank(referer)) {
                uc.addRequestProperty("Referer", referer);
            }
			inputStream = uc.getInputStream();
			bufferedImage = ImageIO.read(inputStream);

			if (null != bufferedImage)
			{
				String contentType = uc.getContentType();
				result = new ImageData(contentType, bufferedImage);
			}
		}
		catch (Exception exception)
		{
			result = null;
		}
		finally
		{
			if (null != inputStream)
			{
				try
				{
					inputStream.close();
				}
				catch (IOException ignored)
				{
				}
			}
		}

        if(url.getProtocol().equals("http") && ((null==result) || (null == result.getImageBits())))
        {
            //URLConnection will not follow http to https redirect.
            URL secureUrl = HttpClientX.goSecure(url);
            result = ImageX.getImageFromWeb(secureUrl, referer);
        }

		return result;
	}

	/**
	 * Convert a byte{} to a string that can be used by an HTML img element.
	 *
	 * @param bits
	 * 	A byte[] that represents an image.
	 * @param contentType
	 * 	The byte[] content type.
	 *
	 * @return A string that can be rendered by an HTML img element.
	 */
	public static
	String toHTMLSource(byte[] bits, String contentType)
	{
		String result = null;
		if (null != bits)
		{
			result = String.format("data:%s;base64,%s", contentType, ImageX.toBase64(bits));
		}
		return result;
	}

	/**
	 * Convert a byte{} to a string that can be used by an HTML img element.
	 *
	 * @param bits
	 * 	A byte[] that represents an image.
	 *
	 * @return A string that can be rendered by an HTML img element.
	 */
	public static
	String toBase64(byte[] bits)
	{
		String result = null;
		if (null != bits)
		{
			result = Base64.encodeBase64String(bits);
		}
		return result;
	}

	/**
	 * Convert a byte[] to a buffered image.
	 *
	 * @param imageBits
	 * 	Image as a byte[];
	 *
	 * @return A buffered image.
	 */
	public static
	BufferedImage getBufferedImage(byte[] imageBits)
	{
		ByteArrayInputStream stream = new ByteArrayInputStream(imageBits);
		try
		{
			return ImageIO.read(stream);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static
	ImageData getImageData(ImageData imageData, int maxSquared, boolean enforce)
	{
		boolean imageAccepted=(imageData!=null);

        if(imageAccepted && enforce)
        {
            int imgMax = imageData.isPortrait() ? imageData.getHeight() : imageData.getWidth();
            if (imgMax < maxSquared)
            {
                float dif = (100 - (((float) imgMax / (float) maxSquared) * 100));

                if (dif > ImageX.MAX_SIZE_DIFFERENCE)
                {
                    imageAccepted=false;
                }
            }
        }

		ImageData result=null;
		if (imageAccepted)
		{
			BufferedImage scaledImage = ImageUtils.resizeImage(
				imageData.getBufferedImage(), imageData.getType(), maxSquared, maxSquared
			);
			result=new ImageData(imageData.getContentType(), scaledImage);
		}

		return result;
	}

// --------------------------- CONSTRUCTORS ---------------------------

	private
	ImageX()
	{
		super();
	}
}
