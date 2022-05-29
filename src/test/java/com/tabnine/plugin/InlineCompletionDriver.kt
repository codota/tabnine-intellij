package com.tabnine.plugin

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Condition
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.tabnine.binary.requests.autocomplete.AutocompleteResponse
import com.tabnine.inline.render.InlineElementRenderer
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
    return (
        myFixture.editor.inlayModel.getInlineElementsInRange(
            myFixture.caretOffset - 1,
            myFixture.caretOffset
        ).firstOrNull()?.renderer as InlineElementRenderer?
        )?.getContent()
}

public fun mockedApplicationWhichInvokesImmediately(): Application {
    val application = Mockito.spy(ApplicationManager.getApplication())
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
