package jp.toastkid.chat.domain.model

import org.junit.Assert.assertTrue
import org.junit.Test

class GenerativeAiModelTest {

    @Test
    fun image() {
        assertTrue(GenerativeAiModel.GEMINI_2_0_FLASH_IMAGE.image())

        assertTrue(
            GenerativeAiModel.entries
                .filter { it != GenerativeAiModel.GEMINI_2_0_FLASH_IMAGE }
                .none(GenerativeAiModel::image)
        )
    }

}
