/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds

import android.text.TextUtils
import net.mm2d.log.Logger
import net.mm2d.upnp.util.XmlUtils
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.SAXException
import java.io.IOException
import java.util.*
import javax.xml.parsers.ParserConfigurationException

/**
 * CdsObjectのファクトリークラス。
 *
 * BrowseDirectChildrenの結果及びBrowseMetadataの結果をCdsObjectに変換して返す。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal object CdsObjectFactory {
    /**
     * BrowseDirectChildrenの結果をパースしてCdsObjectのリストとして返す。
     *
     * @param udn MediaServerのUDN
     * @param xml パースするXML
     * @return パース結果
     */
    fun parseDirectChildren(
        udn: String,
        xml: String?
    ): List<CdsObject> {
        val list = ArrayList<CdsObject>()
        if (TextUtils.isEmpty(xml)) {
            return list
        }
        try {
            val document = XmlUtils.newDocument(false, xml!!)
            val rootTag = createRootTag(document)
            var node: Node? = document.documentElement.firstChild
            while (node != null) {
                if (node.nodeType != Node.ELEMENT_NODE) {
                    node = node.nextSibling
                    continue
                }
                val cdsObject = createCdsObject(udn, (node as Element?)!!, rootTag)
                if (cdsObject != null) {
                    list.add(cdsObject)
                }
                node = node.nextSibling
            }
        } catch (e: ParserConfigurationException) {
            Logger.w(e)
        } catch (e: SAXException) {
            Logger.w(e)
        } catch (e: IOException) {
            Logger.w(e)
        }

        return list
    }

    /**
     * BrowseMetadataの結果をパースしてCdsObjectのインスタンスを返す。
     *
     * @param udn MediaServerのUDN
     * @param xml パースするXML
     * @return パース結果、パースに失敗した場合null
     */
    fun parseMetadata(
        udn: String,
        xml: String?
    ): CdsObject? {
        if (TextUtils.isEmpty(xml)) {
            return null
        }
        try {
            val document = XmlUtils.newDocument(false, xml!!)
            val rootTag = createRootTag(document)
            var node: Node? = document.documentElement.firstChild
            while (node != null) {
                if (node.nodeType != Node.ELEMENT_NODE) {
                    node = node.nextSibling
                    continue
                }
                return createCdsObject(udn, (node as Element?)!!, rootTag)
                node = node.nextSibling
            }
        } catch (e: ParserConfigurationException) {
            Logger.w(e)
        } catch (e: SAXException) {
            Logger.w(e)
        } catch (e: IOException) {
            Logger.w(e)
        }

        return null
    }

    private fun createRootTag(doc: Document): Tag {
        return Tag(doc.documentElement, true)
    }

    /**
     * CdsObjectのインスタンスを作成する。
     *
     * @param udn     MediaServerのUDN
     * @param element CdsObjectを指すElement
     * @param rootTag DIDL-Liteノードに記載されたNamespace情報
     * @return CdsObjectのインスタンス、パースに失敗した場合null
     */
    private fun createCdsObject(
        udn: String,
        element: Element,
        rootTag: Tag
    ): CdsObject? {
        try {
            return CdsObject(udn, element, rootTag)
        } catch (e: IllegalArgumentException) {
            Logger.w(e)
        }

        return null
    }
}
