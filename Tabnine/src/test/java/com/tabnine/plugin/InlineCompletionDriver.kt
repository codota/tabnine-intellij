package com.tabnine.plugin

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.application.Application
import com.intellij.openapi.util.Condition
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.tabnineCommon.binary.requests.autocomplete.AutocompleteResponse
import com.tabnineCommon.inline.render.BlockElementRenderer
import com.tabnineCommon.inline.render.InlineElementRenderer
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

fun createObjectMapper(): ObjectMapper {
    val objectMapper = ObjectMapper()
    // it fails on `docs` fields which is not presented in `AutocompleteResponse`, but that's
    // (serialization works in production).
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    return objectMapper
}

@Throws(JsonProcessingException::class)
fun setOldPrefixFor(completionMock: String, oldPrefix: String): String {
    val mapper = createObjectMapper()
    val autocompleteResponse = mapper.readValue(
        completionMock,
        AutocompleteResponse::class.java
    )
    autocompleteResponse.old_prefix = oldPrefix
    return mapper.writeValueAsString(autocompleteResponse)
}

fun getTabnineCompletionContent(myFixture: CodeInsightTestFixture): String? {
    val inlineContent = (
        myFixture.editor.inlayModel.getInlineElementsInRange(
            myFixture.caretOffset - 1,
            myFixture.caretOffset
        ).firstOrNull()?.renderer as InlineElementRenderer?
        )?.getContent()

    val blockContent = (
        myFixture.editor.inlayModel.getBlockElementsInRange(
            myFixture.caretOffset - 1,
            myFixture.caretOffset
        ).firstOrNull()?.renderer as BlockElementRenderer?
        )?.getContent()

    return inlineContent?.let { inline -> blockContent?.let { block -> inline + "\n" + block } ?: inline }
}

fun mockedApplicationWhichInvokesImmediately(): Application {
    val application = Mockito.spy(ServiceManager)
    val answer = Answer<Void?> { invocation: InvocationOnMock ->
        invocation.getArgument(0, Runnable::class.java).run()
        null
    }
    Mockito.doAnswer(answer).`when`(application).invokeLater(ArgumentMatchers.any())
    Mockito.doAnswer(answer).`when`(application).invokeLater(
        ArgumentMatchers.any(),
        ArgumentMatchers.any(
            Condition::class.java
        )
    )
    return application
}
