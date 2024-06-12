/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.markdown.domain.service


import jp.toastkid.markdown.domain.model.data.HorizontalRule
import jp.toastkid.markdown.domain.model.data.TextBlock
import jp.toastkid.markdown.domain.model.entity.Markdown
import java.util.stream.Stream

class MarkdownParser {

    /** Table builder.  */
    private val tableBuilder = TableBuilder()

    private var codeBlockBuilder = CodeBlockBuilder()

    private val imageExtractor = ImageExtractor()

    private val stringBuilder = StringBuilder()

    private val listLineBuilder = ListLineBuilder()

    private val orderedListPrefixPattern = "^[0-9]+\\.".toRegex()

    private val horizontalRulePattern = "^-{3,}$".toRegex()

    operator fun invoke(content: String, title: String): Markdown {
        return invoke(content.split("\n").stream(), title)
    }

    /**
     * Convert to Slides.
     * @return List&lt;Slide&gt;
     */
    private fun invoke(stream: Stream<String>, title: String): Markdown {
        val markdown = Markdown(title)
        stream.forEach { line ->
            if (line.startsWith("#")) {
                markdown.add(
                    TextBlock(
                        line.substring(line.indexOf(" ") + 1),
                        level = line.split(" ")[0].length
                    )
                )
                return@forEach
            }

            if (line.startsWith("![")) {
                markdown.addAll(imageExtractor.invoke(line))
                return@forEach
            }

            if (line.startsWith("> ")) {
                markdown.add(TextBlock(line.substring(2), quote = true))
                return@forEach
            }
            // Adding code block.
            if (line.startsWith("```")) {
                if (codeBlockBuilder.inCodeBlock()) {
                    codeBlockBuilder.build().let {
                        markdown.add(it)
                        codeBlockBuilder.initialize()
                    }
                    return@forEach
                }

                codeBlockBuilder.startCodeBlock()
                val index = line.indexOf(":")
                val lastIndex = if (index == -1) line.length else index
                codeBlockBuilder.setCodeFormat(line.substring(3, lastIndex))
                return@forEach
            }
            if (codeBlockBuilder.shouldAppend(line)) {
                codeBlockBuilder.append(line)
                return@forEach
            }

            if (TableBuilder.isTableStart(line)) {
                if (!tableBuilder.active()) {
                    tableBuilder.setActive()
                }

                if (TableBuilder.shouldIgnoreLine(line)) {
                    return@forEach
                }

                if (!tableBuilder.hasColumns()) {
                    tableBuilder.setColumns(line)
                    return@forEach
                }

                tableBuilder.addTableLines(line)
                return@forEach
            }

            if (tableBuilder.active()) {
                markdown.add(tableBuilder.build())
                tableBuilder.setInactive()
                tableBuilder.clear()
            }

            if (line.startsWith("- [ ] ") || line.startsWith("- [x] ")) {
                listLineBuilder.setTaskList()
                listLineBuilder.add(line)
                return@forEach
            }

            if (line.startsWith("- ")) {
                listLineBuilder.add(line)
                return@forEach
            }

            if (orderedListPrefixPattern.containsMatchIn(line)) {
                listLineBuilder.setOrdered()
                listLineBuilder.add(line)
                return@forEach
            }

            if (horizontalRulePattern.containsMatchIn(line)) {
                markdown.add(HorizontalRule())
                return@forEach
            }

            if (listLineBuilder.isNotEmpty()) {
                markdown.add(listLineBuilder.build())
                listLineBuilder.clear()
                return@forEach
            }

            // Not code.
            if (line.isNotEmpty()) {
                stringBuilder.append(line)
                return@forEach
            }

            if (stringBuilder.isNotEmpty()) {
                markdown.add(TextBlock(stringBuilder.toString()))
                stringBuilder.setLength(0)
                return@forEach
            }
        }

        if (stringBuilder.isNotEmpty()) {
            markdown.add(TextBlock(stringBuilder.toString()))
        }
        if (listLineBuilder.isNotEmpty()) {
            markdown.add(listLineBuilder.build())
            listLineBuilder.clear()
        }
        if (tableBuilder.active()) {
            markdown.add(tableBuilder.build())
            tableBuilder.setInactive()
            tableBuilder.clear()
        }

        return markdown
    }

}