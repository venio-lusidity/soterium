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

package com.lusidity.framework.text;

import com.lusidity.framework.math.MathX;
import com.lusidity.framework.regex.RegExHelper;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.FormatUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

@SuppressWarnings({
	"UnusedDeclaration",
	"NonFinalUtilityClass"
	,
	"OverlyComplexClass"
})
public class StringX
{

	@SuppressWarnings("WeakerAccess")
	public static class LevenshteinDistance
	{
		private LevenshteinDistance()
		{
		}

		// Methods
		public static boolean isSimilar(String str1, String str2, float threshold)
		{
			boolean result=false;
			if (!StringUtils.isBlank(str1) && !StringUtils.isBlank(str2))
			{
				@SuppressWarnings("DynamicRegexReplaceableByCompiledPattern")
				String clean1=StringX.clean(str1.replace(" ", "")).toLowerCase();
				@SuppressWarnings("DynamicRegexReplaceableByCompiledPattern")
				String clean2=StringX.clean(str2.replace(" ", "")).toLowerCase();
				int dif1=StringX.LevenshteinDistance.computeDistance(str1, str2);
				result=(dif1<=threshold) || ((StringUtils.contains(clean1, clean2) || StringUtils.contains(clean2, clean1)));
			}
			return result;
		}

		public static int computeDistance(String str1, String str2)
		{
			String s1=str1.toLowerCase();
			String s2=str2.toLowerCase();

			int[] costs=new int[s2.length()+1];
			for (int i=0; i<=s1.length(); i++)
			{
				int lastValue=i;
				for (int j=0; j<=s2.length(); j++)
				{
					if (i==0)
					{
						costs[j]=j;
					}
					else
					{
						if (j>0)
						{
							int newValue=costs[j-1];
							if (s1.charAt(i-1)!=s2.charAt(j-1))
							{
								newValue=Math.min(Math.min(newValue, lastValue), costs[j])+1;
								costs[j-1]=lastValue;
								lastValue=newValue;
							}
						}
					}
				}
				if (i>0)
				{
					costs[s2.length()]=lastValue;
				}
			}
			return costs[s2.length()];
		}
	}

	// ------------------------------ FIELDS ------------------------------
// Fields
	public static final String EMPTY=StringUtils.EMPTY;
	private static final Pattern SPECIAL_CHARACTERS=Pattern.compile("[^\\w\\s]");
	// -------------------------- STATIC METHODS --------------------------
	private static final Map<Integer, String> NUMBERS=new HashMap<>();
	private static final Pattern NUMERIC_PATTERN=Pattern.compile("[^0-9]");
	public static final int BYTE_FACTOR=256;

	static
	{
		StringX.NUMBERS.put(0, "zero");
		StringX.NUMBERS.put(1, "one");
		StringX.NUMBERS.put(2, "two");
		StringX.NUMBERS.put(3, "three");
		StringX.NUMBERS.put(4, "four");
		StringX.NUMBERS.put(5, "five");
		StringX.NUMBERS.put(6, "six");
		StringX.NUMBERS.put(7, "seven");
		StringX.NUMBERS.put(8, "eight");
		StringX.NUMBERS.put(9, "nine");
	}

	private StringX()
	{
		super();
	}

	// Methods
	public static int countWords(String s)
	{
		String t=s.trim();
		int nWords=t.isEmpty() ? 0 : 1;

		char[] chs=s.toCharArray();
		for (char ch : chs)
		{
			if (Character.isWhitespace(ch))
			{
				nWords++;
			}
		}

		return nWords;
	}

	/**
	 * Get the remainder of a string, after a matching substring.
	 *
	 * @param entire Entire string.
	 * @param after  Substring to match.
	 * @return Remainder of string, after substring; may be empty if substring was not found or was found at end of string.
	 */
	public static String getAfter(String entire, String after)
	{
		StringBuilder sb=new StringBuilder();

		int matchIndex=entire.indexOf(after);
		if (matchIndex!=-1)
		{
			int matchLength=after.length();
			int afterIndex=matchIndex+matchLength;
			if (afterIndex<entire.length())
			{
				sb.append(entire.substring(afterIndex));
			}
		}

		return sb.toString();
	}

	/**
	 * Get a sub-string between two other sub-strings.
	 *
	 * @param str   String from which sub-string is to be extracted.
	 * @param start Starting sub-string.
	 * @param end   Ending sub-string.
	 * @return Sub-string between the specified sub-strings.
	 */
	public static String getBetween(String str, String start, String end)
	{
		//	From http://projectnimbus.org/2010/11/03/consuming-windows-azure-marketplace-data-market-datasets/
		StringBuilder sb=new StringBuilder();
		int startIdx=str.indexOf(start)+start.length();
		int endIdx=str.indexOf(end);
		while (startIdx<endIdx)
		{
			sb.append(str.charAt(startIdx));
			startIdx++;
		}
		return sb.toString();
	}

	public static String padLeft(String in, int targetLength, String paddingChar)
	{
		StringBuilder sb=new StringBuilder(in);
		while (sb.length()<targetLength)
		{
			sb.insert(0, paddingChar);
		}
		return sb.toString();
	}

	/**
	 * Quote the specified string with the specified quote character.
	 *
	 * @param str   String to quote.
	 * @param quote Quote character (usually a single or double quote).
	 * @return Quoted string.
	 */
	public static String quote(String str, String quote)
	{
		return String.format("%s%s%s", quote, str, quote);
	}

	public static String removeQuotes(String str)
	{
		String result=str;
		if (!StringUtils.isBlank(result) && result.startsWith("\""))
		{
			result=StringUtils.stripEnd(result, "\"");
			result=StringUtils.stripStart(result, "\"");
			result=result.trim();
		}
		return result;
	}

// --------------------------- CONSTRUCTORS ---------------------------

	public static String toFirstUpper(String in)
	{
		return StringUtils.isBlank(in) ? "" : String.format("%s%s", in.substring(0, 1).toUpperCase(), in.substring(1));
	}

	public static String toFirstLower(String in)
	{
		return StringUtils.isBlank(in) ? "" : String.format("%s%s", in.substring(0, 1).toLowerCase(), in.substring(1));
	}

	public static String toLowerCase(String str)
	{
		String result=null;
		if (!StringUtils.isBlank(str))
		{
			result=str.toLowerCase();
		}
		return result;
	}

	public static String urlEncode(String str)
	{
		String result=str;
		if (!StringUtils.isBlank(result))
		{
			try
			{
				result=URLEncoder.encode(str, "UTF-8");
				result=StringUtils.replace(result, "%3A%2f%2f", "://");
				result=StringUtils.replace(result, "%2f", "/");
			}
			catch (Exception ignored)
			{
			}
		}
		return result;  //To change body of created methods use File | Settings | File Templates.
	}

	/**
	 * Get a search-friendly form of the title of a Resource.
	 *
	 * @param title Title.
	 * @return Search-friendly form of a Resource title.
	 */
	public static String getSearchableTitle(String title)
	{
		String result=title.toLowerCase();

		//  If the title begins with an article, remove it
		if (result.startsWith("a "))
		{
			result=StringUtils.substringAfter(result, "a ");
		}
		if (result.startsWith("an "))
		{
			result=StringUtils.substringAfter(result, "an ");
		}
		if (result.startsWith("the "))
		{
			result=StringUtils.substringAfter(result, "the ");
		}


		//  If the title includes a parenthesis or colon (e.g., "My Book: Volume I"), remove any text after
		result=StringUtils.substringBefore(result, "(");
		result=StringUtils.substringBefore(result, ":");

		return result;
	}

	/**
	 * Normalize a title.
	 *
	 * @param in Title to normalize.
	 * @return Normalized title.
	 */
	public static String normalizeTitle(String in)
	{
		String out=in;

		if (out.endsWith(", The"))
		{
			out="The "+StringUtils.substringBeforeLast(out, ", The");
		}
		if (out.endsWith(", A"))
		{
			out="A "+StringUtils.substringBeforeLast(out, ", A");
		}
		if (out.endsWith(", An"))
		{
			out="An "+StringUtils.substringBeforeLast(out, ", An");
		}

		return out;
	}

	public static String getTitleForMatching(String title)
	{
		String result=StringX.getMainTitle(title);
		if (!StringUtils.isBlank(result))
		{
			result=RegExHelper.SPECIAL_CHARACTERS_PATTERN.matcher(result).replaceAll("");
		}
		return result;
	}

	public static String getMainTitle(String title)
	{
		String result=title;

		if (!StringX.isBlank(result))
		{
			Matcher matcher=RegExHelper.SUBTITLE_PATTERN.matcher(result);
			if (matcher.matches())
			{
				result=matcher.group(1);
				result=result.trim();
			}
		}
		return result;
	}

	public static boolean isBlank(String str)
	{
		return StringUtils.isBlank(str);
	}

	public static boolean isAnyBlank(String... args)
	{
		boolean result=false;
		for (String str : args)
		{
			result=StringUtils.isBlank(str);
			if (result)
			{
				break;
			}
		}
		return result;
	}

	public static String toTitle(String text)
	{
		StringBuilder cased=new StringBuilder();
		String[] parts=text.toLowerCase().trim().split(RegExHelper.WHITE_SPACE.toString());
		for (String part : parts)
		{
			if (!StringUtils.isBlank(part))
			{
				cased.append(part.substring(0, 1).toUpperCase());
				cased.append(part.substring(1).toLowerCase());
				cased.append(' ');
			}
		}
		String result=null;
		if (cased.length()>0)
		{
			result=cased.toString().trim();
		}
		return result;
	}

	public static String enumToString(Class<? extends Enum> en)
	{
		String result="";
		//noinspection unchecked
		for (Object o : EnumSet.allOf(en))
		{
			result=String.format("%s%s%s", result, (result.isEmpty()) ? "" : ", ", o.toString());
		}
		return result;
	}

	public static String removeJSONMarkup(String text)
	{
		String result=text;
		if (!StringUtils.isBlank(result))
		{
			result=result.trim();

			if (result.startsWith("[") && result.endsWith("]"))
			{
				result=StringUtils.stripStart(result, "[");
				result=StringUtils.stripEnd(result, "]");
			}

			if (result.startsWith("{") && result.endsWith("}"))
			{
				result=StringUtils.stripStart(result, "{");
				result=StringUtils.stripEnd(result, "}");
			}
		}

		return result;
	}

	public static String allUpper(String text)
	{
		String result=text;
		if (!StringUtils.isEmpty(text))
		{
			//noinspection DynamicRegexReplaceableByCompiledPattern
			result=text.replace("%", "percent").replace("#", "number").replace("/", "_").replace(" ", "_");
			//noinspection DynamicRegexReplaceableByCompiledPattern
			result=StringX.clean(result.toLowerCase(), "_").replace("__", "_");

			result=RegExHelper.SPECIAL_CHARACTERS_PATTERN.matcher(result).replaceAll("_");

			//noinspection DynamicRegexReplaceableByCompiledPattern
			result=StringX.clean(result.toLowerCase(), "_").replace("__", "_");
			result=StringUtils.stripStart(result, "_");
			result=StringUtils.stripEnd(result, "_");

			// Remove numbers from front of string.
			char first=result.charAt(0);
			if (Character.isDigit(first))
			{
				String name=StringX.NUMBERS.get(Character.getNumericValue(first));
				if (!StringUtils.isBlank(name))
				{
					result=StringUtils.removeStart(result, Character.toString(first));
					result=name+'_'+result;
				}
			}
			result=result.toUpperCase();
		}

		return result;
	}

	public static String clean(String str, String replaceWith)
	{
		String cleaned=str;
		if (!StringUtils.isEmpty(str))
		{
			cleaned=StringX.clean(str);
			String temp = StringUtils.isBlank(replaceWith) ? "" : replaceWith;
			cleaned=RegExHelper.SPECIAL_CHARACTERS_PATTERN.matcher(cleaned).replaceAll(temp);
		}
		return cleaned;
	}

	/**
	 * Clean-up text, removing any HTML mark-up that may be present.
	 *
	 * @param in Input string to clean.
	 * @return Cleaned-up text, with any HTML mark-up removed.
	 */
	public static String clean(String in)
	{
		String out;

		if (!StringUtils.isBlank(in) && RegExHelper.PATTERN_HTML.matcher(in).matches())
		{
			try
			{
				out=ArticleExtractor.getInstance().getText(in);
			}
			catch (BoilerpipeProcessingException ignored)
			{
				out=in;
			}
		}
		else
		{
			out=in;
		}

		if (!StringUtils.isBlank(out))
		{
			//noinspection DynamicRegexReplaceableByCompiledPattern
			out=StringUtils.replace(in, "&amp;", "and").replace("&", "and");
			out=out.trim();
		}

		return out;
	}

	/**
	 * CamelCase will start with lower then upper case, words must be separated by a space.
	 *
	 * @param text The text to case.
	 * @return "Camel Case" --> "camelCase"
	 */
	public static String toCamelCase(String text)
	{
		StringBuilder cased=new StringBuilder();
		String str=StringX.toPascalCase(text);
		cased.append(str.substring(0, 1).toLowerCase());
		cased.append(str.substring(1));
		return cased.toString();
	}

	/**
	 * PascalCase words will all start with upper then lowercase.
	 *
	 * @param text The text to case.
	 * @return "pascal Case" --> "PascalCase"
	 */
	public static String toPascalCase(String text)
	{
		StringBuilder cased=new StringBuilder();
		String result=text;
		if (!StringUtils.isEmpty(result))
		{
			result=StringUtils.replace(result, "_", " ");
			result=StringX.insertSpaceAtCapitol(result);
			//noinspection DynamicRegexReplaceableByCompiledPattern
			result=result.toLowerCase().replace("%", "percent").replace("#", "number");
			String str=result.toLowerCase();
			str=StringX.clean(str, " ");
			String[] parts=str.split(RegExHelper.WHITE_SPACE.toString());

			if (parts.length>0)
			{
				for (String part : parts)
				{
					if (!StringUtils.isEmpty(part))
					{
						cased.append(part.substring(0, 1).toUpperCase());
						cased.append(part.substring(1).toLowerCase());
					}
				}
			}
			else
			{
				cased.append(result);
			}

			// Remove numbers from front of string.
			result=cased.toString();
			char first=result.charAt(0);
			if (Character.isDigit(first))
			{
				String name=StringX.NUMBERS.get(Character.getNumericValue(first));
				if (!StringUtils.isBlank(name))
				{
					result=StringUtils.removeStart(result, Character.toString(first));
					result=String.format("%s%s", StringX.toPascalCase(name), result);
				}
			}
		}

		return result;
	}

	public static String insertStringAtCapitol(String str, String insert)
	{
		return StringX.replace(StringX.insertSpaceAtCapitol(str), " ", insert);
	}

	public static String replace(String text, String searchString, String replacement)
	{
		return StringUtils.replace(text, searchString, replacement);
	}

	public static String insertSpaceAtCapitol(String str)
	{
		String result="";
		if (!StringX.isBlank(str))
		{
			//noinspection DynamicRegexReplaceableByCompiledPattern
			result=str.replaceAll("(\\p{Ll})(\\p{Lu})", "$1 $2");
		}
		return result;
	}

	public static String pluralize(String str)
	{
		String result=str;
		if (!StringUtils.isBlank(result))
		{
			result=result.trim();
			if (StringUtils.endsWithIgnoreCase(result, "y"))
			{
				result=StringUtils.removeEnd(result, "y");
				result = String.format("%s%s", result, "ies");
			}
			else if (!StringUtils.endsWith(result, "es") && StringUtils.endsWith(result, "ss"))
			{
				result= String.format("%s%s", result, "es");
			}
			else if (!StringUtils.endsWithIgnoreCase(result, "s"))
			{
				result= String.format("%s%s", result, "s");
			}
		}
		return result;
	}

	public static String urlDecode(String str, String encoding)
	{
		String result=str;
		String enc = StringUtils.isBlank(encoding) ? TextEncoding.UTF8 : encoding;
		try
		{
			result=URLDecoder.decode(str, enc);
		}
		catch (Exception ignored)
		{
		}
		return result;
	}

	public static String removeLast(String str, String delimiter)
	{
		String last=StringX.getLast(str, delimiter);
		return StringUtils.equals(str, last) ? str : StringUtils.replaceOnce(str, String.format("%s%s", delimiter, last), "");
	}

	public static String getLast(String str, String delimiter)
	{
		String result=null;
		if (!StringUtils.isBlank(str) && (!StringUtils.isBlank(delimiter) || StringX.equals(delimiter, " ")))
		{
			int start=StringUtils.lastIndexOf(str, delimiter);
			result=StringUtils.substring(str, (start+1));
		}
		return result;
	}

	public static boolean equals(String cs1, String cs2)
	{
		return StringUtils.equals(cs1, cs2);
	}

	public static String removeFirst(String str, String delimiter)
	{
		String last=StringX.getFirst(str, delimiter);
		return StringUtils.equals(str, last) ? str : StringUtils.replaceOnce(str, String.format("%s%s", last, delimiter), "");
	}

	public static String getFirst(String str, String delimiter)
	{
		String result=null;
		if (!StringUtils.isBlank(str) && (!StringUtils.isBlank(delimiter) || StringX.equals(delimiter, " ")))
		{
			String[] paths=str.split(Pattern.quote(delimiter));
			result=paths[0];
		}
		return result;
	}

	public static String stringOrBlank(Object object)
	{
		@SuppressWarnings("UnusedAssignment")
		String result="";
		try
		{
			result=(null!=object) ? object.toString() : "(OBJECT NULL)";
		}
		catch (Exception ignored)
		{
			result="";
		}
		return result;
	}

	public static String[] split(String targetedPath, char delimiter)
	{
		return (StringUtils.isBlank(targetedPath)) ? null : StringUtils.split(targetedPath, delimiter);
	}

	public static String[] splitCapitals(String str)
	{
		Collection<String> items=new ArrayList<>();
		String working="";
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<str.length(); i++)
		{
			char c=str.charAt(i);
			if (Character.isUpperCase(c))
			{
				if (!StringUtils.isBlank(working))
				{
					items.add(working);
					working="";
				}
			}
			sb.append(Character.toString(c));
		}
		working = sb.toString();
		if (!StringUtils.isBlank(working))
		{
			items.add(working);
		}
		String[] results=new String[items.size()];
		int on=0;
		for (String item : items)
		{
			results[on]=item;
			on++;
		}
		return results;
	}

	public static String onlyNumbers(String str, String... exceptions)
	{
		StringBuilder sb = new StringBuilder();
		if (!StringUtils.isBlank(str))
		{
			String temp = str;
			temp=StringUtils.replace(temp, "%20", "");
			temp=StringUtils.replace(temp, " ", "");
			String lexicon="12345674890";
			if (null!=exceptions)
			{
				for (String exception : exceptions)
				{
					lexicon=String.format("%s%s", lexicon, exception);
				}
			}
			String lowered=temp.toLowerCase();
			for (int i=0; i<lowered.length(); i++)
			{
				String c=StringUtils.substring(lowered, i, i+1);
				if (StringUtils.contains(lexicon, c) || StringUtils.isEmpty(c))
				{
					sb.append(c);
				}
			}
		}
		return (sb.length()==0) ? "" : sb.toString();
	}

	public static boolean isNumerical(String str)
	{
		boolean result=false;
		if (!StringUtils.isBlank(str))
		{
			result=true;
			String lexicon="12345674890";
			String lowered=StringX.replace(str.toLowerCase(), ".", ",");
			for (int i=0; i<lowered.length(); i++)
			{
				String item=StringUtils.substring(lowered, i, i+1);
				if (!StringUtils.contains(lexicon, item))
				{
					result=false;
					break;
				}
			}
		}
		return result;
	}
	public static boolean hasAlphabetical(String str)
	{
		boolean result=false;
		if (!StringUtils.isBlank(str))
		{
			result=false;
			String lexicon="abcdefghijklmnopqrstuvwxyz";
			String lowered=str.toLowerCase();
			for (int i=0; i<lowered.length(); i++)
			{
				String item=StringUtils.substring(lowered, i, i+1);
				if (StringUtils.contains(lexicon, item))
				{
					result=true;
					break;
				}
			}
		}
		return result;
	}
	public static boolean hasNumbers(String str)
	{
		boolean result=false;
		if (!StringUtils.isBlank(str))
		{
			result=false;
			String lexicon="12345674890";
			String lowered=str.toLowerCase();
			for (int i=0; i<lowered.length(); i++)
			{
				String item=StringUtils.substring(lowered, i, i+1);
				if (StringUtils.contains(lexicon, item))
				{
					result=true;
					break;
				}
			}
		}
		return result;
	}
	//Removes non alphanumeric characters but keeps original case
	public static String removeNonAlphaNumericIgnoreCase(String str, String... exceptions)
	{
		StringBuilder sb = new StringBuilder();
		if (!StringUtils.isBlank(str))
		{
			String temp = str.trim();
			temp=StringUtils.replace(temp, "%20", " ");
			String lexicon="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ12345674890";
			if (null!=exceptions)
			{
				for (String exception : exceptions)
				{
					lexicon=String.format("%s%s", lexicon, exception);
				}
			}
			for (int i=0; i<temp.length(); i++)
			{
				String c=StringUtils.substring(temp, i, i+1);
				if (StringUtils.contains(lexicon, c) || StringUtils.isEmpty(c))
				{
					sb.append(c);
				}
			}
		}
		return (sb.length()==0) ? "" : sb.toString();
	}

	public static String stripUnicodeCharacters(String text)
	{
		StringBuilder sb = new StringBuilder();
		String match=text;

		Matcher unicode=RegExHelper.UNICODE_PATTERN.matcher(match);
		if (unicode.matches())
		{
			match=unicode.group(1);
			if (!StringUtils.isBlank(match))
			{
				sb.append(" ");
			}
			sb.append(unicode.group(3));
		}

		return (sb.length()==0) ? "" : sb.toString();
	}

	public static String combine(String[] split, String delimiter)
	{
		StringBuilder sb = new StringBuilder();
		if (null!=split)
		{
			for (String s : split)
			{
				if (sb.length()>0)
				{
					sb.append(delimiter);
				}
				sb.append(s);
			}
		}
		return (sb.length()==0) ? "" : sb.toString();
	}

	public static boolean match(String name, String... filters)
	{
		Boolean result=false;
		for (String filter : filters)
		{
			String temp = filter;
			if (StringUtils.startsWith(temp, "^!"))
			{
				temp=StringUtils.stripStart(temp, "^A");
				result=!StringUtils.equals(name, temp);
			}
			else if (StringUtils.endsWith(temp, "^*"))
			{
				temp=StringUtils.stripEnd(temp, "^*");
				result=StringUtils.startsWith(name, temp);
			}
			else if (StringUtils.startsWith(temp, "^*"))
			{
				temp=StringUtils.stripStart(temp, "^*");
				result=StringUtils.endsWith(name, temp);
			}
			else
			{
				result=StringUtils.equals(name, temp);
			}

			if (!result)
			{
				break;
			}
		}
		return result;
	}

	public static String insertPeriodically(
		String text, String insert, int period)
	{
		StringBuilder builder=new StringBuilder(
			text.length()+(insert.length()*(text.length()/period))+1);

		int index=0;
		String prefix="";
		while (index<text.length())
		{
			// Don't put the insert in the very first iteration.
			// This is easier than appending it *after* each substring
			builder.append(prefix);
			prefix=insert;
			builder.append(text.substring(index,
				Math.min(index+period, text.length())
			));
			index+=period;
		}
		return builder.toString();
	}

	public static String insertPeriodicallyFromEnd(String txt, String insert, int period)
	{
		String result="";
		if (!StringX.isBlank(txt) && (null!=insert))
		{
			int on=0;
			for (int len=(txt.length()-1); len>=0; len--)
			{
				char c=txt.charAt(len);
				if (((on%period)==0) && (on>0))
				{
					result=String.format("%s%s", insert, result);
				}
				result=String.format("%s%s", c, result);
				on++;
			}
		}
		return result;
	}

	public static String insertPeriodicallyFromStart(String txt, String insert, int period)
	{
		String result="";
		if (!StringX.isBlank(txt) && (null!=insert))
		{
			for (int i=0; i<txt.length(); i++)
			{
				char c=txt.charAt(i);
				if (((i%period)==0) && (i>0))
				{
					result=String.format("%s%s", result, insert);
				}
				result=String.format("%s%s", result, c);
			}
		}
		return result;
	}

	public static String getAt(String[] parts, int index)
	{
		return ((null!=parts) && (index<parts.length)) ? parts[index] : null;
	}

	public static String getNumeric(String value)
	{
		String result=null;
		if (!StringUtils.isBlank(value))
		{
			try
			{
				String working=StringX.removeNonAlphaNumericCharacters(value, "");
				working=StringX.removeAll(working.toLowerCase(), "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z");
				Long test=Long.parseLong(working.trim());
				if (test>0)
				{
					result=test.toString();
				}
			}
			catch (Exception ignored){}
		}
		return result;
	}

	public static String removeNonAlphaCharacters(String str, String... exceptions)
	{
		StringBuilder sb = new StringBuilder();
		if (!StringUtils.isBlank(str))
		{
			String temp = str.trim();
			temp=StringUtils.replace(temp, "%20", "");
			temp=StringUtils.replace(temp, " ", "");
			String lexicon="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
			if (null!=exceptions)
			{
				for (String exception : exceptions)
				{
					lexicon=String.format("%s%s", lexicon, exception);
				}
			}
			String lowered=temp.toLowerCase();
			for (int i=0; i<lowered.length(); i++)
			{
				String c=StringUtils.substring(lowered, i, i+1);
				if (StringUtils.contains(lexicon, c) || StringUtils.isEmpty(c))
				{
					sb.append(c);
				}
			}
		}
		return (sb.length()==0) ? "" : sb.toString();
	}

	public static String removeNonAlphaNumericCharacters(String str, String... exceptions)
	{
		StringBuilder sb = new StringBuilder();
		if (!StringUtils.isBlank(str))
		{
			String temp = str.trim();
			temp=StringUtils.replace(temp, "%20", "");
			temp=StringUtils.replace(temp, " ", "");
			String lexicon="abcdefghijklmnopqrstuvwxyz12345674890";
			if (null!=exceptions)
			{
				for (String exception : exceptions)
				{
					lexicon=String.format("%s%s", lexicon, exception);
				}
			}
			String lowered=temp.toLowerCase();
			for (int i=0; i<lowered.length(); i++)
			{
				String c=StringUtils.substring(lowered, i, i+1);
				if (StringUtils.contains(lexicon, c) || StringUtils.isEmpty(c))
				{
					sb.append(c);
				}
			}
		}
		return (sb.length()==0) ? "" : sb.toString();
	}

	public static String removeNonNumericCharacters(String str, String... exceptions)
	{
		StringBuilder sb = new StringBuilder();
		if (!StringUtils.isBlank(str))
		{
			String temp = str.trim();
			temp=StringUtils.replace(temp, "%20", "");
			temp=StringUtils.replace(temp, " ", "");
			String lexicon="12345674890";
			if (null!=exceptions)
			{
				for (String exception : exceptions)
				{
					lexicon=String.format("%s%s", lexicon, exception);
				}
			}
			String lowered=temp.toLowerCase();
			for (int i=0; i<lowered.length(); i++)
			{
				String c=StringUtils.substring(lowered, i, i+1);
				if (StringUtils.contains(lexicon, c) || StringUtils.isEmpty(c))
				{
					sb.append(c);
				}
			}
		}
		return (sb.length()==0) ? "" : sb.toString();
	}

	public static Long getNumberFromString(String numberString)
	{
		Long result = null;
		try
		{
			String temp= StringX.NUMERIC_PATTERN.matcher(numberString).replaceAll("");
			result=Long.parseLong(temp);
		}
		catch (Exception ignored){}
		return result;
	}

	@SuppressWarnings("OverlyComplexMethod")
	public static String smartAppend(String... valueKeyPair)
	{
		StringBuilder sb = new StringBuilder();
		if ((null!=valueKeyPair) && ((valueKeyPair.length%2)==0))
		{
			int x=valueKeyPair.length;
			for (int i=0; i<x; i+=2)
			{
				String key=valueKeyPair[i];
				String value=valueKeyPair[i+1];

				if (!StringUtils.isBlank(value))
				{
					if (!StringUtils.isBlank(key))
					{
						sb.append(String.format(" %s", key));
					}
					if (sb.length()>0)
					{
						sb.append(" ");
					}
					sb.append(value);
				}
			}
		}
		else if(null!=valueKeyPair){
			int x=valueKeyPair.length;
			for (String value : valueKeyPair)
			{
				if (!StringUtils.isBlank(value))
				{
					if (sb.length()>0)
					{
						sb.append(" ");
					}
					sb.append(value);
				}
			}
		}
		return (sb.length()>0) ? sb.toString() : null;
	}

	public static String smartPrepend(String... keyValuePair)
	{
		StringBuilder sb=new StringBuilder();

		if ((null!=keyValuePair) && ((keyValuePair.length%2)==0))
		{
			int x=keyValuePair.length;
			for (int i=0; i<x; i+=2)
			{
				String key=keyValuePair[i];
				String value=keyValuePair[i+1];
				if (!StringUtils.isBlank(value))
				{
					if (sb.length()>0)
					{
						sb.append(" ");
					}
					if (!StringUtils.isBlank(key))
					{
						sb.append(String.format("%s ", key));
					}
					sb.append(value);
				}
			}
		}
		return (sb.length()>0) ? sb.toString() : null;
	}

	public static void writeFile(File directory, String fileName, String text)
		throws IOException
	{
		if (!StringUtils.isBlank(text) && !StringUtils.isBlank(fileName) && directory.isDirectory())
		{
			File file=new File(String.format("%s/%s", directory.getAbsolutePath(), fileName));
			FileUtils.writeStringToFile(file, text);
		}
	}

	public static String readFile(InputStream inputStream, Charset encoding)
		throws IOException
	{
		byte[] encoded=IOUtils.toByteArray(inputStream);
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}

	public static String readFile(String path, Charset encoding)
		throws IOException
	{
		byte[] encoded=Files.readAllBytes(Paths.get(path));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}

	public static String fromMillis(long millis)
	{
		StringBuffer buf=new StringBuffer();
		buf.append("PT");
		boolean negative=(millis<0);
		FormatUtils.appendUnpaddedInteger(buf, millis);
		while (buf.length()<(negative ? 7 : 6))
		{
			buf.insert(negative ? 3 : 2, '0');
		}
		if (((millis/1000)*1000)==millis)
		{
			buf.setLength(buf.length()-3);
		}
		else
		{
			buf.insert(buf.length()-3, '.');
		}
		buf.append('S');
		return buf.toString();
	}

	public static String withQuotes(String str)
	{
		return String.format("\"%s\"", str);
	}

	public static String specialQuotes(String str)
	{
		return String.format("`%s`", str);
	}

	public static String encode(String str)
	{
		String result=str;
		if (!StringX.isBlank(result))
		{
			byte[] bytesEncoded=Base64.encodeBase64(str.getBytes());
			result=new String(bytesEncoded);
		}
		return result;
	}

	public static String decode(String bytesEncoded)
	{
		String result=null;
		if (!StringX.isBlank(bytesEncoded) && !StringX.contains(bytesEncoded, " "))
		{
			byte[] valueDecoded=Base64.decodeBase64(bytesEncoded);
			result=new String(valueDecoded);
			if (StringUtils.contains(result, "�"))
			{
				// The string was not encoded.
				result=bytesEncoded;
			}
		}
		return result;
	}

	public static String compress(String str)
	{
		byte[] bytesEncoded=Base64.encodeBase64(str.getBytes());

		String result=null;
		try (ByteArrayOutputStream outputStream=new ByteArrayOutputStream(bytesEncoded.length))
		{
			try (GZIPOutputStream zip=new GZIPOutputStream(outputStream))
			{
				zip.write(bytesEncoded);
				byte[] compressed=outputStream.toByteArray();
				result=new String(compressed);
			}
			catch (Exception ignored){}
		}
		catch (Exception ignored){}

		return result;
	}

	@SuppressWarnings("UnusedAssignment")
	public static String decompress(String encoded, int bit)
	{
		byte[] bytes=encoded.getBytes();
		StringBuilder strTemp=new StringBuilder();
		StringBuilder sbBinary=new StringBuilder();
		StringBuilder sbText=new StringBuilder();
		Integer tempInt=0;
		int intTemp=0;
		//noinspection ForLoopReplaceableByForEach
		for (int i=0; i<bytes.length; i++)
		{
			if (bytes[i]<0)
			{
				intTemp=bytes[i]+StringX.BYTE_FACTOR;
			}
			else
			{
				intTemp=bytes[i];
			}
			strTemp=new StringBuilder(Integer.toBinaryString(intTemp));
			while ((strTemp.length()%8)!=0)
			{
				strTemp.insert(0, "0");
			}
			sbBinary.append(strTemp);
		}
		for (int i=0; i<sbBinary.length(); i+=bit)
		{
			tempInt=Integer.valueOf(sbBinary.substring(i, i+bit), 2);
			sbText.append(StringX.toChar(tempInt));
		}
		return (sbText.length()==0) ? "" : sbText.toString();
	}

	@SuppressWarnings({
		"MagicNumber",
		"OverlyComplexMethod",
		"OverlyLongMethod"
	})
	private static char toChar(int val)
	{
		char ch;
		//noinspection SwitchStatementWithTooManyBranches
		switch (val)
		{
			case 0:
				ch=' ';
				break;
			case 1:
				ch='a';
				break;
			case 2:
				ch='b';
				break;
			case 3:
				ch='c';
				break;
			case 4:
				ch='d';
				break;
			case 5:
				ch='e';
				break;
			case 6:
				ch='f';
				break;
			case 7:
				ch='g';
				break;
			case 8:
				ch='h';
				break;
			case 9:
				ch='i';
				break;
			case 10:
				ch='j';
				break;
			case 11:
				ch='k';
				break;
			case 12:
				ch='l';
				break;
			case 13:
				ch='m';
				break;
			case 14:
				ch='n';
				break;
			case 15:
				ch='o';
				break;
			case 16:
				ch='p';
				break;
			case 17:
				ch='q';
				break;
			case 18:
				ch='r';
				break;
			case 19:
				ch='s';
				break;
			case 20:
				ch='t';
				break;
			case 21:
				ch='u';
				break;
			case 22:
				ch='v';
				break;
			case 23:
				ch='w';
				break;
			case 24:
				ch='x';
				break;
			case 25:
				ch='y';
				break;
			case 26:
				ch='z';
				break;
			case 27:
				ch='.';
				break;
			case 28:
				ch='*';
				break;
			case 29:
				ch=',';
				break;
			case 30:
				ch='\\';
				break;
			case 31:
				ch='2';
				break;
			case 32:
				ch='A';
				break;
			case 33:
				ch='B';
				break;
			case 34:
				ch='C';
				break;
			case 35:
				ch='D';
				break;
			case 36:
				ch='E';
				break;
			case 37:
				ch='F';
				break;
			case 38:
				ch='G';
				break;
			case 39:
				ch='H';
				break;
			case 40:
				ch='I';
				break;
			case 41:
				ch='J';
				break;
			case 42:
				ch='K';
				break;
			case 43:
				ch='L';
				break;
			case 44:
				ch='M';
				break;
			case 45:
				ch='N';
				break;
			case 46:
				ch='O';
				break;
			case 47:
				ch='P';
				break;
			case 48:
				ch='Q';
				break;
			case 49:
				ch='R';
				break;
			case 50:
				ch='S';
				break;
			case 51:
				ch='T';
				break;
			case 52:
				ch='U';
				break;
			case 53:
				ch='V';
				break;
			case 54:
				ch='W';
				break;
			case 55:
				ch='0';
				break;
			case 56:
				ch='1';
				break;
			case 57:
				ch='3';
				break;
			case 58:
				ch='4';
				break;
			case 59:
				ch='5';
				break;
			case 60:
				ch='6';
				break;
			case 61:
				ch='7';
				break;
			case 62:
				ch='8';
				break;
			case 63:
				ch='9';
				break;
			default:
				ch=' ';
		}
		return ch;
	}

	@SuppressWarnings("MagicNumber")
	public static String toPhoneNumber(String str)
	{
		String result=StringX.removeNonAlphaNumericCharacters(str);
		if (result.length()==7)
		{
			result=String.format("%s-%s", result.substring(0, 3), result.substring(3, 7));
		}
		else if (result.length()==10)
		{
			result=String.format("(%s)%s-%s", result.substring(0, 3), result.substring(3, 6), result.substring(6, 10));
		}
		else if (result.length()==11)
		{
			result=String.format("%s-(%s)%s-%s", result.substring(0, 1), result.substring(1, 4), result.substring(4, 7), result.substring(7, 11));
		}
		return result;
	}

	public static boolean isDifferent(String original, String value)
	{
		boolean result=false;
		if (StringUtils.isBlank(original) && !StringUtils.isBlank(value))
		{
			result=true;
		}
		else if (!StringUtils.isBlank(original) && !StringUtils.isBlank(value) && !StringUtils.equalsIgnoreCase(original, value))
		{
			result=true;
		}
		return result;
	}

	public static boolean isBase64Encoded(String str)
	{
		boolean result=false;
		if (!StringUtils.isBlank(str))
		{
			String test=StringUtils.trim(str);
			result=((test.length()%4)==0) && RegExHelper.BASE64_ENCODED_PATTERN.matcher(test).matches();
		}
		return result;
	}

	public static boolean isGUID(String str)
	{
		boolean result=false;
		if (!StringUtils.isBlank(str))
		{
			String regex;
			if (StringUtils.contains(str, "{") && StringUtils.contains(str, "}"))
			{
				regex="^\\{?[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\}?$";
			}
			else
			{
				regex="^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
			}
			if (str.matches(regex))
			{
				result=true;
			}
		}
		return result;
	}

	public static boolean startsWith(String str, String prefix)
	{
		return StringUtils.startsWith(str, prefix);
	}

	public static boolean startsWithIgnoreCase(String str, String... prefix)
	{
		boolean result=false;
		if (null!=prefix)
		{
			for (String item : prefix)
			{
				result=StringUtils.startsWithIgnoreCase(str, item);
				if (result)
				{
					break;
				}
			}
		}
		return result;
	}

	public static boolean endsWith(String str, String suffix)
	{
		return StringUtils.endsWith(str, suffix);
	}

	public static boolean endsWithAny(String str, String... suffixes)
	{
		boolean result=false;
		if ((null!=suffixes) && !StringX.isBlank(str))
		{
			for (String suffix : suffixes)
			{
				result=StringUtils.endsWith(str, suffix);
				if (result)
				{
					break;
				}
			}
		}
		return result;
	}

	public static boolean endsWithIgnoreCase(String str, String suffix)
	{
		return StringUtils.endsWithIgnoreCase(str, suffix);
	}

	public static boolean endsWithAnyIgnoreCase(String str, String... suffixes)
	{
		boolean result=false;
		if ((null!=suffixes) && !StringX.isBlank(str))
		{
			for (String suffix : suffixes)
			{
				result=StringUtils.endsWithIgnoreCase(str, suffix);
				if (result)
				{
					break;
				}
			}
		}
		return result;
	}

	public static boolean contains(String seq, int searchChar)
	{
		return StringUtils.contains(seq, searchChar);
	}

	public static String stripStart(String str, String... strippers)
	{
		String result = str;
		for(String strip: strippers){
			result = StringUtils.stripStart(result, strip);
		}
		return result;
	}

	public static String removeAll(String str, String... strippers)
	{
		String result = str;
		for(String strip: strippers){
			result = StringUtils.remove(result, strip);
		}
		return result;
	}

	public static String stripEnd(String str, String stripCharacters)
	{
		return StringUtils.stripEnd(str, stripCharacters);
	}

	public static boolean equalsAnyIgnoreCase(String cs1, String... cs2)
	{
		boolean result=false;
		if (!StringX.isBlank(cs1) && (null!=cs2))
		{
			for (String cs : cs2)
			{
				if (!StringX.isBlank(cs))
				{
					result=StringUtils.equalsIgnoreCase(cs1, cs);
					if (result)
					{
						break;
					}
				}
			}
		}
		return result;
	}

	public static String removeStart(String str, String remove)
	{
		return StringUtils.removeStart(str, remove);
	}

	public static String removeStartIgnoreCase(String str, String remove)
	{
		return StringUtils.removeStartIgnoreCase(str, remove);
	}

	public static String removeEnd(String str, String remove)
	{
		return StringUtils.removeEnd(str, remove);
	}

	public static String removeEndIgnoreCase(String str, String remove)
	{
		return StringUtils.removeEndIgnoreCase(str, remove);
	}

	public static String replace(String text, String replacement, String... phrases)
	{
		String result=text;
		if (null!=phrases)
		{
			for (String phrase : phrases)
			{
				result=StringX.replace(result, phrase, replacement);
			}
		}
		return result;
	}

	public static String replace(String text, String searchString, String replacement, int startAt)
	{
		String result=text;
		String temp=StringX.substring(text, startAt);
		if (StringX.contains(temp, searchString))
		{
			result=StringX.replace(result, temp, "");
			temp=StringX.replace(temp, searchString, replacement);
			result=String.format("%s%s", result, temp);
		}

		return result;
	}

	public static String substring(String str, int start)
	{
		return StringUtils.substring(str, start);
	}

	public static boolean contains(String str, String searchSequence)
	{
		return StringUtils.contains(str, searchSequence);
	}

	public static String replaceOnce(String text, String searchString, String replacement)
	{
		return StringUtils.replaceOnce(text, searchString, replacement);
	}

	public static String substring(String str, int start, int end)
	{
		return StringUtils.substring(str, start, end);
	}

	public static boolean isNotBlank(String str)
	{
		return !StringX.isBlank(str);
	}

	public static String stripAccents(String input)
	{
		return StringUtils.stripAccents(input);
	}

	public static String substringAfter(String str, String separator)
	{
		return StringUtils.substringAfter(str, separator);
	}

	public static String substringBefore(String str, String separator)
	{
		return StringUtils.substringBefore(str, separator);
	}

	public static String leftPad(String str, int size, char padChar)
	{
		return StringUtils.leftPad(str, size, padChar);
	}

	public static String leftPad(String str, int size, String padStr)
	{
		return StringUtils.leftPad(str, size, padStr);
	}

	public static int countMatches(String str, String sub)
	{
		int result = 0;
		// the StringUtils.countMatches is extremely slow
		String[] parts = StringX.split(str, sub);
		if(null!=parts){
			result = (parts.length-1);
		}
		return result;
	}

	public static int countContains(String str, String searchString, boolean ignoreCase)
	{
		int result = 0;
		if(!StringX.isBlank(str) && !StringX.isBlank(searchString))
		{
			String[] parts= RegExHelper.WHITE_SPACE.split(str);
			if (null!=parts)
			{
				for (String part : parts)
				{
					if (!StringX.isBlank(part))
					{
						String a=(ignoreCase) ? part.toLowerCase() : part;
						String b=(ignoreCase) ? searchString.toLowerCase() : searchString;
						if (StringX.contains(a, b))
						{
							result+=1;
						}
					}
				}
			}
		}
		return result;
	}

	public static String trim(String str)
	{
		return StringUtils.trim(str);
	}

	/**
	 * Parse each string with the lastDelimiter and seperate with the delimiter.
	 *
	 * @param items     A collection of string values
	 * @param delimiter The char or characters used to separate each value.
	 * @return A String representation of the collection.
	 */
	public static String getStrings(Collection<String> items, String delimiter)
	{
		StringBuilder sb = new StringBuilder();
		if (null!=items)
		{
			for (String item : items)
			{
				if (sb.length()>0)
				{
					sb.append(delimiter);
				}
				sb.append(item);
			}
		}
		return (sb.length()==0) ? "" : sb.toString();
	}

	/**
	 * Parse each string with the lastDelimiter and seperate with the delimiter.
	 *
	 * @param items         A collection of string values
	 * @param delimiter     The char or characters used to separate each value.
	 * @param lastDelimiter The delimiter to use in order to parse the value.
	 * @return A String representation of the collection.
	 */
	public static String getLastStrings(Collection<String> items, String delimiter, String lastDelimiter)
	{
		StringBuilder sb = new StringBuilder();
		if (null!=items)
		{
			for (String item : items)
			{
				if (sb.length()>0)
				{
					sb.append(delimiter);
				}
				sb.append(StringX.getLast(item, lastDelimiter));
			}
		}
		return (sb.length()==0) ? "" : sb.toString();
	}

	public static String getString(InputStream content)
	{
		String result=null;
		try (InputStream contentStream=content)
		{
			result=IOUtils.toString(contentStream, TextEncoding.UTF8);
		}
		catch (Exception ignored){}
		return result;
	}

	public static int compare(String val1, String val2)
	{
		int result;
		String value1 = StringX.isBlank(val1) ? null : val1.toLowerCase();
		String value2 = StringX.isBlank(val2) ? null : val2.toLowerCase();

		if (StringX.equals(value1, value2))
		{
			result=0;
		}
		else if (StringX.isBlank(value1))
		{
			result=-1;
		}
		else if (StringX.isBlank(value2))
		{
			result=1;
		}
		else
		{
			result=value1.compareTo(value2);
		}
		return result;
	}

	public static String getValue(String working, String delimiter, Integer... indexes)
	{
		StringBuilder result=new StringBuilder();
		if (null!=indexes)
		{
			String[] parts=StringX.split(working, delimiter);
			for (Integer index : indexes)
			{
				if ((null!=index) && (index<parts.length))
				{
					String value=parts[index];
					if (!StringX.isBlank(value) && !StringX.equalsIgnoreCase(value, "null"))
					{
						if (result.length()>0)
						{
							result.append(" ");
						}
						result.append(value.trim());
					}
				}
			}
		}
		return (result.length()>0) ? result.toString() : null;
	}

	public static String[] split(String targetedPath, String delimiter)
	{
		//noinspection SizeReplaceableByIsEmpty
		return ((!StringUtils.isBlank(targetedPath) && ((null!=delimiter) && (delimiter.length()>0))) ? StringUtils.split(targetedPath, delimiter) : null);
	}

	public static boolean equalsIgnoreCase(String str1, String str2)
	{
		return StringUtils.equalsIgnoreCase(str1, str2);
	}

	public static boolean contains(String working, String... anyOf)
	{
		boolean result=false;
		if (null!=anyOf)
		{
			for (String str : anyOf)
			{
				result=StringX.contains(working, str);
				if (result)
				{
					break;
				}
			}
		}
		return result;
	}

	public static boolean containsIgnoreCase(String working, String... anyOf)
	{
		boolean result=false;
		if (null!=anyOf)
		{
			for (String str : anyOf)
			{
				result=StringX.containsIgnoreCase(working, str);
				if (result)
				{
					break;
				}
			}
		}
		return result;
	}

	public static boolean containsIgnoreCase(String str, String searchStr)
	{
		return StringUtils.containsIgnoreCase(str, searchStr);
	}

	public static int indexOf(String seq, String searchSeq)
	{
		return StringUtils.indexOf(seq, searchSeq);
	}

	public static StringBuilder build(String... values)
	{
		StringBuilder result=new StringBuilder();
		if (null!=values)
		{
			for (String value : values)
			{
				result.append(value);
			}
		}
		return result;
	}

	public static String join(String delimiter, String... values)
	{
		return StringUtils.join(values, delimiter);
	}

	@SuppressWarnings({
		"OverlyComplexMethod",
		"OverlyLongMethod",
		"MagicNumber"
	})
	public static String toCryptex(String str, int key){
		String result = str;
		if(!StringX.isBlank(str))
		{
			Collection<String> lexicons=new ArrayList<>();
			lexicons.add("XYZGJKABCOPQRSTDEFHILMNUVW");
			lexicons.add("ghnopiabcdefjkwxylmuvzqrst");
			String lexNum = "3782129312456345678932465";

			for(String lex: lexicons)
			{
				int len=lex.length();
				int next = (key>25) ? 0 : key;
				next = (next<0) ? 0 : next;
				for (int i=0; i<len; i++)
				{
					try
					{
						String find=lex.substring(i, i+1);
						String replace=lex.substring(next, next+1);
						result=StringX.replace(result, find, replace);
						next++;
						if(next>=len){
							next=0;
						}
					}
					catch (Exception ignored){}
				}
			}

			int len=lexNum.length();
			int next = (key>25) ? 0 : key;
			next = (next<0) ? 0 : next;
			for (int i=0; i<len; i++)
			{
				try
				{
					String find=lexNum.substring(i, i+1);
					String replace=lexNum.substring(next, next+1);
					result=StringX.replace(result, find, replace);
					next++;
					if(next>=len){
						next=0;
					}
				}
				catch (Exception ignored){}
			}
		}

		return result;
	}

	@SuppressWarnings({
		"IntegerDivisionInFloatingPointContext",
		"NumericCastThatLosesPrecision"
	})
	private static String cryptexSort(String lexicon, int key)
	{
		StringBuilder result = new StringBuilder();
		String working = lexicon;
		int k = key;
		while (!StringX.isBlank(working)){
			int len = working.length();
			k =(k<=len) ? (int) Math.ceil(MathX.clamp(k/2, 0, (len-1))) : k;
			int d = (int) MathX.clamp(len-k, 0, (len-1));
			String r = working.substring(d, d+1);
			result.append(r);
			working = working.replace(r, "");
		}
		return result.toString();
	}

	@SuppressWarnings({
		"OverlyComplexMethod",
		"OverlyLongMethod",
		"MagicNumber"
	})
	private static int toValue(char ch)
	{
		int chaVal;
		//noinspection SwitchStatementWithTooManyBranches
		switch (ch)
		{
			case ' ':
				chaVal=0;
				break;
			case 'a':
				chaVal=1;
				break;
			case 'b':
				chaVal=2;
				break;
			case 'c':
				chaVal=3;
				break;
			case 'd':
				chaVal=4;
				break;
			case 'e':
				chaVal=5;
				break;
			case 'f':
				chaVal=6;
				break;
			case 'g':
				chaVal=7;
				break;
			case 'h':
				chaVal=8;
				break;
			case 'i':
				chaVal=9;
				break;
			case 'j':
				chaVal=10;
				break;
			case 'k':
				chaVal=11;
				break;
			case 'l':
				chaVal=12;
				break;
			case 'm':
				chaVal=13;
				break;
			case 'n':
				chaVal=14;
				break;
			case 'o':
				chaVal=15;
				break;
			case 'p':
				chaVal=16;
				break;
			case 'q':
				chaVal=17;
				break;
			case 'r':
				chaVal=18;
				break;
			case 's':
				chaVal=19;
				break;
			case 't':
				chaVal=20;
				break;
			case 'u':
				chaVal=21;
				break;
			case 'v':
				chaVal=22;
				break;
			case 'w':
				chaVal=23;
				break;
			case 'x':
				chaVal=24;
				break;
			case 'y':
				chaVal=25;
				break;
			case 'z':
				chaVal=26;
				break;
			case '.':
				chaVal=27;
				break;
			case '*':
				chaVal=28;
				break;
			case ',':
				chaVal=29;
				break;
			case '\\':
				chaVal=30;
				break;
			case '2':
				chaVal=31;
				break;
			case 'A':
				chaVal=32;
				break;
			case 'B':
				chaVal=33;
				break;
			case 'C':
				chaVal=34;
				break;
			case 'D':
				chaVal=35;
				break;
			case 'E':
				chaVal=36;
				break;
			case 'F':
				chaVal=37;
				break;
			case 'G':
				chaVal=38;
				break;
			case 'H':
				chaVal=39;
				break;
			case 'I':
				chaVal=40;
				break;
			case 'J':
				chaVal=41;
				break;
			case 'K':
				chaVal=42;
				break;
			case 'L':
				chaVal=43;
				break;
			case 'M':
				chaVal=44;
				break;
			case 'N':
				chaVal=45;
				break;
			case 'O':
				chaVal=46;
				break;
			case 'P':
				chaVal=47;
				break;
			case 'Q':
				chaVal=48;
				break;
			case 'R':
				chaVal=49;
				break;
			case 'S':
				chaVal=50;
				break;
			case 'T':
				chaVal=51;
				break;
			case 'U':
				chaVal=52;
				break;
			case 'V':
				chaVal=53;
				break;
			case 'W':
				chaVal=54;
				break;
			case '0':
				chaVal=55;
				break;
			case '1':
				chaVal=56;
				break;
			case '3':
				chaVal=57;
				break;
			case '4':
				chaVal=58;
				break;
			case '5':
				chaVal=59;
				break;
			case '6':
				chaVal=60;
				break;
			case '7':
				chaVal=61;
				break;
			case '8':
				chaVal=62;
				break;
			case '9':
				chaVal=63;
				break;
			default:
				chaVal=0;
		}
		return chaVal;
	}

	public static String setValue(String str, String defaultIfNull)
	{
		return StringX.isBlank(str) ? defaultIfNull : str;
	}

	public static String makeKey(String str)
	{
		String result = str;
		if(!StringX.isBlank(str)){
			result = StringX.replace(result, " ", "_");
			result = StringX.removeNonAlphaNumericCharacters(result, "_");
			result = result.toLowerCase();
		}
		return result;
	}

	public static String insertAtStartWithFinalLength(String str, String value, int finalLength)
	{
		String result = StringX.isBlank(str) ? "" : StringX.substring(str, 0, finalLength);
		int len = StringX.isBlank(result) ? 0 : str.length();
		if(len<finalLength)
		{
			int inserts=finalLength-len;
			for (int i=0; i<inserts; i++)
			{
				result = String.format("%s%s", value, result);
			}
		}

		return result;
	}

	public static boolean isGuidEmpty(String value)
	{
		return StringX.isBlank(value) || StringX.equals(value, "00000000000000000000000000000000") || StringX.equals(value, "00000000-0000-0000-0000-000000000000") || StringX.equals(value, "{00000000-0000-0000-0000-000000000000}");
	}

	public static String highlight(String str, String highlight)
	{
		return (StringX.isBlank(str) || StringX.isBlank(highlight)) ? str :
			str.replaceAll("(?i)"+highlight, String.format("<span style=\"padding: 0 0 0 0;background-color: #ff0;font-weight: bold\">%s</span>", highlight));
	}
}
