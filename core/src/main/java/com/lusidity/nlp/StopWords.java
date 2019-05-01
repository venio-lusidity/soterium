/*
 * Copyright (c) 2008-2012, Venio, Inc.
 * All Rights Reserved Worldwide.
 *
 * This computer software is protected by copyright law and international treaties.
 * It may not be duplicated, reproduced, distributed, compiled, executed,
 * reverse-engineered, or used in any other way, in whole or in part, without the
 * express written consent of Venio, Inc.
 *
 * Portions of this computer software also embody trade secrets, patents, and other
 * protected intellectual property of Venio, Inc. and third parties and are subject to
 * applicable laws, regulations, treaties, agreements, and other legal mechanisms.
 */

package com.lusidity.nlp;

import com.lusidity.framework.text.StringX;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Pattern;

public class StopWords
{
// Fields
	public static final String DEFAULT_STOP_WORDS_PATH="data/stopwords.csv";
	private static final Pattern PATTERN_COMMAS=Pattern.compile(",");
	private static final Pattern PATTERN_SPACE=Pattern.compile(" ");
	private final Collection<String> words=new HashSet<String>();

// Constructors
	/**
	 * Default constructor, will load stopwords from default stopwords file at ./data/stopwords.csv.
	 *
	 * @throws java.io.FileNotFoundException
	 */
	public StopWords()
		throws FileNotFoundException
	{
		this(null);
	}

	/**
	 * Constructor.
	 *
	 * @param file Stopwords, in comma-separated value format.
	 * @throws java.io.FileNotFoundException
	 */
	public StopWords(File file)
		throws FileNotFoundException
	{
		super();
		File loadFrom=(null==file) ? new File(StopWords.DEFAULT_STOP_WORDS_PATH) : file;
		this.load(loadFrom);
	}

	private void load(File file)
		throws FileNotFoundException
	{
		@SuppressWarnings("IOResourceOpenedButNotSafelyClosed") FileReader fileReader=new FileReader(file);
		try
		{
			LineIterator lines=IOUtils.lineIterator(fileReader);
			while (lines.hasNext())
			{
				String line=lines.nextLine();
				if (!StringX.isBlank(line) && !line.startsWith("#"))
				{
					String[] values=StopWords.PATTERN_COMMAS.split(line);
					for (String value : values)
					{
						value=value.toLowerCase();
						if (!this.words.contains(value))
						{
							this.words.add(value);
						}
					}
				}
			}
			lines.close();
		}
		finally
		{
			IOUtils.closeQuietly(fileReader);
		}
	}

	public String removeStopWords(CharSequence text)
	{
		String[] terms=StopWords.PATTERN_SPACE.split(text);
		StringBuilder resultsBuilder=new StringBuilder();

		for (String term : terms)
		{
			if (!this.isStopWord(term))
			{
				if (resultsBuilder.length()>0)
				{
					resultsBuilder.append(' ');
				}
				resultsBuilder.append(term);
			}
		}

		return resultsBuilder.toString();
	}

	/**
	 * Is the specified word a stop word?
	 *
	 * @param check Word to check. MUST be in lower case.
	 * @return true if the specified word is a stop word.
	 */
	public boolean isStopWord(String check)
	{
		return this.words.contains(check.toLowerCase());
	}

	public String removeStartsWith(CharSequence text)
	{
		String[] terms=StopWords.PATTERN_SPACE.split(text);
		StringBuilder resultsBuilder=new StringBuilder();
		int len=terms.length;
		for (int i=0; i<len; i++)
		{
			String term=terms[i];
			if ((i==0) && this.isStopWord(term))
			{
				continue;
			}
			if (resultsBuilder.length()>0)
			{
				resultsBuilder.append(' ');
			}
			resultsBuilder.append(term);
		}

		return resultsBuilder.toString();
	}
}
