package com.tabnine.unitTests

import com.google.gson.JsonSyntaxException
import com.tabnineCommon.general.DependencyContainer
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

data class Simple(val test: String, val number: Int)
data class WithOptional(val test: String, val number: Int, val optional: String? = null)
data class WithDouble(val double: Double)

@ExtendWith(MockitoExtension::class)
class GsonDeserializeTests {
    private val ourSingletonGson = DependencyContainer.instanceOfGson()

    @Test
    fun shouldDeserializeCorrectlyWhenJsonIsValid() {
        val json = "{\"test\":\"test\",\"number\":1}"
        val testGson = ourSingletonGson.fromJson(json, Simple::class.java)
        assert(testGson.test == "test" && testGson.number == 1)
    }

    @Test
    fun shouldIgnoreExtraFields() {
        val json = "{\"test\":\"test\",\"number\":1,\"extra\":\"extra\"}"
        val testGson = ourSingletonGson.fromJson(json, Simple::class.java)
        assert(testGson.test == "test" && testGson.number == 1)
    }

    @Test
    fun shouldDeserializeDoubleCorrectly() {
        val json = "{\"double\":1.1}"
        val testGson = ourSingletonGson.fromJson(json, WithDouble::class.java)
        assert(testGson.double == 1.1)

        val json2 = "{\"double\":\"3\"}"
        val testGson2 = ourSingletonGson.fromJson(json2, WithDouble::class.java)
        assert(testGson2.double == 3.0)
    }

    @Test
    fun shouldDeserializeCorrectlyWhenJsonHasOptionalField() {
        val testGson = ourSingletonGson.fromJson("{\"test\":\"test\",\"number\":1}", WithOptional::class.java)
        assert(testGson.test == "test" && testGson.number == 1 && testGson.optional == null)
        val testGson2 = ourSingletonGson.fromJson(
            "{\"test\":\"test\",\"number\":1,\"optional\":\"test\"}",
            WithOptional::class.java
        )
        assert(testGson2.test == "test" && testGson2.number == 1 && testGson2.optional == "test")
    }

    @Test
    fun shouldDeSerializeWithDefaultValuesWhenJsonMissingRequireFields() {
        val json = "{\"number\":1}"
        val testGson = ourSingletonGson.fromJson(json, Simple::class.java)
        assert(testGson.test == null && testGson.number == 1)

        val json2 = "{\"test\":\"test\"}"
        val testGson2 = ourSingletonGson.fromJson(json2, Simple::class.java)
        assert(testGson2.test == "test" && testGson2.number == 0)
    }

    @Test
    @Throws(JsonSyntaxException::class)
    fun shouldThrowExceptionWhenJsonHasInvalidValues() {
        val json = "{\"test\":\"test\",\"number\":\"kaki\"}"
        assertThrows<JsonSyntaxException> {
            ourSingletonGson.fromJson(json, Simple::class.java)
        }
    }

    @Test
    @Throws(Exception::class)
    fun shouldThrowExceptionWhenJsonIsInvalid() {
        val json = "{\"test:\"test\"}"
        assertThrows<JsonSyntaxException> {
            ourSingletonGson.fromJson(json, Simple::class.java)
        }
    }
}

@ExtendWith(MockitoExtension::class)
class GsonSerializeTests {
    private val ourSingletonGson = DependencyContainer.instanceOfGson()

    @Test
    fun shouldSerializeCorrectly() {
        val value = Simple("test", 1)
        val json = ourSingletonGson.toJson(value)
        assert(json == "{\"test\":\"test\",\"number\":1}")
    }

    @Test
    fun shouldSerializeWholeDoubleAsInt() {
        val value = WithDouble(1.1)
        val json = ourSingletonGson.toJson(value)
        assert(json == "{\"double\":1.1}")

        val value2 = WithDouble(3.0)
        val json2 = ourSingletonGson.toJson(value2)

        // note: this is important, we're handling this case specifically by registering
        // a custom type adapter that serializes whole doubles without the `.0` suffix.
        assert(json2 == "{\"double\":3}")
    }

    @Test
    fun shouldSkipSerializingOptionalFieldsWhenNotProvided() {
        val value = WithOptional("test", 1)
        val json = ourSingletonGson.toJson(value)
        assert(json == "{\"test\":\"test\",\"number\":1}")

        val value2 = WithOptional("test", 1, "testOptional")
        val json2 = ourSingletonGson.toJson(value2)
        assert(json2 == "{\"test\":\"test\",\"number\":1,\"optional\":\"testOptional\"}")
    }
}
