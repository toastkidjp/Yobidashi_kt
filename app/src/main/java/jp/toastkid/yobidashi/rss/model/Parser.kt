/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.rss.model

import java.util.regex.Pattern

/**
 * @author toastkidjp
 */
class Parser {

    /** current extracting item.  */
    private var rss: Rss = Rss()

    /** current extracting item.  */
    private var currentItem: Item? = null

    /** is processing content.  */
    private var isInContent: Boolean = false

    /**
     * Parse RSS.
     * @param rss String.
     * @return Rss items.
     */
    fun parse(rssLines: Iterable<String>): Rss {
        rssLines.forEach(::extractFromLine)
        return this.rss
    }

    private fun extractFromLine(line: String) {
        when {
            line.contains("<item") or line.contains("<entry") -> {
                initializeItem()
            }
            line.contains("<title") -> {
                extractTitle(line)
            }
            line.contains("<link") -> {
                if (line.contains("href=")) {
                    extract(line, LINK_HREF_PATTERN)?.let { currentItem?.link = it }
                } else {
                    extractLink(line)
                }
            }
            line.contains("<description") -> {
                extractDescription(line)
            }
            line.contains("</content") -> {
                isInContent = false
            }
            line.contains("<content") -> {
                isInContent = true
            }
            isInContent -> {
                currentItem?.content?.append(line)
            }
            line.contains("<pubDate") -> {
                extractPubDate(line)
            }
            line.contains("<published") -> {
                extractPublished(line)
            }
            line.contains("<dc:creator>") -> {
                extractCreator(line)
            }
            line.contains("<dc:date>") -> {
                extractDate(line)
            }
            line.contains("<dc:subject>") -> {
                extractSubject(line)
            }
            line.contains("</item>") or line.contains("</entry>") -> {
                currentItem?.let { rss.items.add(it) }
            }
        }
    }

    /**
     * Extract pubData.
     * @param line
     */
    private fun extractPubDate(line: String) {
        rss.date = extract(line, PUBLISH_DATE_PATTERN)
    }

    private fun extractPublished(line: String) {
        rss.date = extract(line, PUBLISHED_PATTERN)
    }

    /**
     * Extract date.
     * @param line
     */
    private fun extractDate(line: String) {
        rss.date = extract(line, DATE_PATTERN)
    }

    /**
     * Extract creator.
     * @param line
     */
    private fun extractCreator(line: String) {
        rss.creator = extract(line, CREATOR_PATTERN)
    }

    /**
     * Extract subject.
     * @param line
     */
    private fun extractSubject(line: String) {
        extract(line, SUBJECT_PATTERN)?.let {
            rss.subjects.add(it)
        }
    }

    /**
     * extract title from html.
     * @param line
     */
    private fun extractTitle(line: String) {
        val title = extract(line, TITLE_PATTERN)
        if (rss.title == null) {
            rss.title = title
            return
        }

        title?.let { currentItem?.title = it }
    }

    /**
     * Extract description.
     * @param line
     */
    private fun extractDescription(line: String) {
        val description = extract(line, DESCRIPTION_PATTERN)
        if (currentItem == null) {
            rss.description = description
            return
        }

        description?.let { currentItem?.description = it }
    }

    /**
     * Extract link.
     * @param line
     */
    private fun extractLink(line: String) {
        val link = extract(line, LINK_PATTERN)
        if (currentItem == null) {
            rss.link = link
            return
        }

        link?.let { currentItem?.link = it }
    }

    /**
     * extract string with passed pattern.
     *
     * @param line string line.
     * @param pattern pattern.
     * @return string
     */
    private fun extract(line: String?, pattern: Pattern?): String? {
        if (line.isNullOrBlank() || pattern == null) {
            return line
        }

        val matcher = pattern.matcher(line)
        return if (matcher.find()) matcher.group(1) else line
    }

    /**
     * init Item.
     */
    private fun initializeItem() {
        currentItem = Item()
    }

    companion object {

        private val TITLE_PATTERN = Pattern.compile("<title.*>(.+?)</title>", Pattern.DOTALL)

        /** pattern of description.  */
        private val DESCRIPTION_PATTERN = Pattern.compile("<description>(.+?)</description>", Pattern.DOTALL)

        /** pattern of link.  */
        private val LINK_PATTERN = Pattern.compile("<link>(.+?)</link>", Pattern.DOTALL)

        /** pattern of link.  */
        private val LINK_HREF_PATTERN = Pattern.compile("href=\"(.+?)\"", Pattern.DOTALL)

        /** pattern of creator.  */
        private val CREATOR_PATTERN = Pattern.compile("<dc:creator>(.+?)</dc:creator>", Pattern.DOTALL)

        /** pattern of date.  */
        private val DATE_PATTERN = Pattern.compile("<dc:date>(.+?)</dc:date>", Pattern.DOTALL)

        /** pattern of subject.  */
        private val SUBJECT_PATTERN = Pattern.compile("<dc:subject>(.+?)</dc:subject>", Pattern.DOTALL)

        /** pattern of pubDate.  */
        private val PUBLISH_DATE_PATTERN = Pattern.compile("<pubDate>(.+?)</pubDate>", Pattern.DOTALL)

        /** pattern of published (for atom).  */
        private val PUBLISHED_PATTERN = Pattern.compile("<published>(.+?)</published>", Pattern.DOTALL)

    }
}