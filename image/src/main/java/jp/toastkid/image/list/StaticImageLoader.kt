package jp.toastkid.image.list

import android.net.Uri
import jp.toastkid.image.Image

class StaticImageLoader {

    private val staticImageUrls = mutableListOf<Uri>()

    operator fun invoke(sort: Sort): List<Image> {
        return extractImages()
    }

    fun filterBy(name: String?): List<Image> {
        return extractImages()
    }

    private fun extractImages(): MutableList<Image> {
        val images = mutableListOf<Image>()

        staticImageUrls.forEach { uri ->
            images.add(Image(uri.toString(), uri.path ?: ""))
        }
        return images
    }

    fun replace(urls: List<Uri>) {
        staticImageUrls.clear()
        staticImageUrls.addAll(urls)
    }

    fun isNotEmpty() = staticImageUrls.isNotEmpty()

}